package cz.meind.service.mapper;

import com.mysql.cj.x.protobuf.MysqlxCursor;
import cz.meind.application.Application;
import cz.meind.database.EntityMetadata;
import cz.meind.interfaces.*;
import org.apache.commons.csv.CSVRecord;


import java.lang.reflect.Field;

import java.sql.*;
import java.util.*;

import static cz.meind.service.mapper.Utils.getField;
import static cz.meind.service.mapper.Utils.getIdField;


public class ObjectMapper {
    private final RelationMapper relationMapper;
    private final SqlService sqlService;

    public ObjectMapper(Connection connection) {
        relationMapper = new RelationMapper(connection, this);
        sqlService = new SqlService(connection);
    }

    public void addCsv(List<CSVRecord> records, Class<?> clazz) {
        for (CSVRecord record : records) {
            try {
                sqlService.save(Application.database.entities.get(clazz), clazz, record);
            } catch (SQLException e) {
                Application.logger.error(SqlService.class, "Error saving: " + record.toString());
            }
        }
    }

    public void save(Object o) {
        try {
            completeSave(o);
        } catch (Exception e) {
            Application.logger.error(ObjectMapper.class, e);
        }
    }

    public void update(Object entity) throws IllegalAccessException, SQLException {
        Class<?> clazz = entity.getClass();
        EntityMetadata metadata = Application.database.entities.get(clazz);
        Field idField = getIdField(clazz);

        if (idField == null) {
            Application.logger.error(ObjectMapper.class, new IllegalArgumentException("Entity " + clazz.getName() + " does not have an ID field"));
            return;
        }
        idField.setAccessible(true);
        Object idValue = idField.get(entity);
        if (idValue == null || Integer.parseInt(idValue.toString()) == 0) {
            Application.logger.error(ObjectMapper.class, new IllegalArgumentException("Cannot update entity without a valid ID"));
            return;
        }

        Map<Object, Field> mtm = new HashMap<>();

        for (Map.Entry<String, Field> relations : metadata.getRelations().entrySet()) {
            Field relationField = relations.getValue();
            relationField.setAccessible(true);
            mapRelations(entity, mtm, relationField);
        }

        for (Map.Entry<Object, Field> m : mtm.entrySet()) {
            relationMapper.updateAllRelations(idField.get(entity).toString(), m.getKey(), m.getValue());
        }

        sqlService.update(metadata, clazz, idField, entity);
    }

    public <T> List<T> fetchAll(Class<T> clazz) {
        ResultSet rs = sqlService.fetchAll(Application.database.entities.get(clazz));
        List<T> entities = new ArrayList<>();
        if (rs != null) {
            try {
                while (rs.next()) {
                    T entity = clazz.getDeclaredConstructor().newInstance();
                    mapFields(entity, clazz, rs);
                    entities.add(entity);
                }
            } catch (Exception e) {
                Application.logger.error(ObjectMapper.class, e);
            }
            try {
                rs.close();
            } catch (SQLException e) {
                Application.logger.error(ObjectMapper.class, e);
            }
        }
        return entities;
    }

    public <T> T fetchById(Class<T> clazz, Integer id) {
        EntityMetadata metadata = Application.database.entities.get(clazz);
        Field idColumn = getIdField(clazz);
        if (idColumn == null) {
            Application.logger.error(ObjectMapper.class, new SQLException("No id field found"));
            return null;
        }
        ResultSet rs = sqlService.fetchById(metadata, idColumn.getAnnotation(Column.class).name(), id);
        try {
            T entity = clazz.getDeclaredConstructor().newInstance();
            if (rs.next()) {
                mapFields(entity, clazz, rs);
                return entity;
            } else {
                Application.logger.error(ObjectMapper.class, "No entity with id " + id);
            }
            rs.close();
        } catch (Exception e) {
            Application.logger.error(ObjectMapper.class, e);
        }
        return null;
    }

    public void deleteById(Class<?> clazz, Integer id) {
        EntityMetadata metadata = Application.database.entities.get(clazz);
        Field idField = getIdField(clazz);
        if (idField == null) {
            Application.logger.error(ObjectMapper.class, new SQLException("No id column"));
            return;
        }
        sqlService.deleteById(metadata.getTableName(), idField.getAnnotation(Column.class).name(), id);
    }

    void fetchById(Integer id, Class<?> clazz, Object entity) throws Exception {
        EntityMetadata metadata = Application.database.entities.get(clazz);
        Field idColumn = getIdField(clazz);
        if (idColumn == null) throw new SQLException("No id column");

        ResultSet rs = sqlService.fetchById(metadata, idColumn.getAnnotation(Column.class).name(), id);
        if (rs.next()) {
            for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
                String columnName = columnEntry.getKey();
                String fieldName = columnEntry.getValue();
                Field field = getField(clazz, fieldName);
                if (field == null) continue;
                field.setAccessible(true);
                field.set(entity, rs.getObject(columnName));
            }
            for (Map.Entry<String, Field> relationEntry : metadata.getRelations().entrySet()) {
                Field relationField = relationEntry.getValue();
                relationField.setAccessible(true);
                String relationType = relationEntry.getKey();
                if (relationType.equals("OneToMany")) {
                    relationField.set(entity, fetchById(relationField.getType(), (Integer) rs.getObject(relationField.getAnnotation(JoinColumn.class).name())));
                }
            }
        }
        rs.close();
    }

    private Integer completeSave(Object entity) throws IllegalAccessException {
        Class<?> clazz = entity.getClass();
        EntityMetadata metadata = Application.database.entities.get(clazz);

        Field idField = getIdField(clazz);
        if (idField == null) {
            Application.logger.error(ObjectMapper.class, new IllegalArgumentException("Entity " + clazz.getName() + " does not have an ID field"));
            return null;
        }
        idField.setAccessible(true);
        if (idField.get(entity) == null) idField.set(entity, 0);
        if (Integer.parseInt(idField.get(entity).toString()) != 0) return (Integer) idField.get(entity);

        Map<Object, Field> mtm = new HashMap<>();
        Map<String, Integer> relationFields = new HashMap<>();

        for (Map.Entry<String, Field> relations : metadata.getRelations().entrySet()) {
            Field relationField = relations.getValue();
            relationField.setAccessible(true);
            if (relationField.isAnnotationPresent(OneToMany.class)) {
                Integer id = completeSave(relationField.get(entity));
                if (id != null) relationFields.put(relationField.getAnnotation(JoinColumn.class).name(), id);
            } else mapRelations(entity, mtm, relationField);
        }

        saveGeneratedKeys(entity, idField, sqlService.save(metadata, clazz, entity, relationFields));

        for (Map.Entry<Object, Field> m : mtm.entrySet()) {
            relationMapper.saveAllRelations(idField.get(entity).toString(), m.getKey(), m.getValue());
        }
        return (Integer) idField.get(entity);
    }

    private void mapFields(Object entity, Class<?> clazz, ResultSet rs) throws Exception {
        EntityMetadata metadata = Application.database.entities.get(clazz);
        Field idField = getIdField(clazz);
        if (idField == null) return;
        idField.setAccessible(true);
        for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
            String columnName = columnEntry.getKey();
            String fieldName = columnEntry.getValue();
            Field field = getField(clazz, fieldName);
            if (field == null) continue;
            Utils.set(field, entity, rs.getObject(columnName));
        }
        for (Map.Entry<String, Field> relationEntry : metadata.getRelations().entrySet()) {
            Field relationField = relationEntry.getValue();
            relationField.setAccessible(true);
            String relationType = relationEntry.getKey();
            switch (relationType) {
                case "OneToMany" ->
                        Utils.set(relationField, entity, fetchById(relationField.getType(), (Integer) rs.getObject(relationField.getAnnotation(JoinColumn.class).name())));
                case "ManyToMany" ->
                        Utils.set(relationField, entity, relationMapper.fetchAllRelationsManyToMany(idField.get(entity).toString(), relationField));
                case "ManyToOne" ->
                        Utils.set(relationField, entity, relationMapper.fetchAllRelationsManyToOne(idField.get(entity).toString(), relationField));
            }
        }
    }

    private void mapRelations(Object entity, Map<Object, Field> mtm, Field relationField) throws IllegalAccessException {
        if (relationField.isAnnotationPresent(ManyToMany.class)) {
            for (Object o : (Collection<?>) relationField.get(entity)) {
                Field idFieldRelation = getIdField(o.getClass());
                if (idFieldRelation == null) {
                    Application.logger.error(ObjectMapper.class, new IllegalArgumentException("Entity " + o.getClass().getName() + " does not have an ID field"));
                    continue;
                }
                idFieldRelation.setAccessible(true);
                if (idFieldRelation.get(o).toString().equals("0") || idFieldRelation.get(o) == null)
                    Application.logger.error(ObjectMapper.class, new IllegalArgumentException("Save related entities first"));
                mtm.put(o, relationField);
            }
        }
    }

    private void saveGeneratedKeys(Object entity, Field idField, PreparedStatement stmt) {
        if (stmt == null) return;
        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
            if (generatedKeys.next()) {
                idField.setAccessible(true);
                Object generatedKey = generatedKeys.getObject(1);
                Class<?> idFieldType = idField.getType();

                if (Number.class.isAssignableFrom(idFieldType) || idFieldType.isPrimitive()) {
                    Number keyAsNumber = (Number) generatedKey;
                    Object convertedKey = Utils.castNumberToType(keyAsNumber, idFieldType);
                    idField.set(entity, convertedKey);
                } else {
                    Application.logger.error(ObjectMapper.class, new IllegalArgumentException("Unsupported id field type: " + idFieldType));
                }
            }
        } catch (Exception e) {
            Application.logger.error(ObjectMapper.class, e);
        }
    }
}