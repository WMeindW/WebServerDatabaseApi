package cz.meind.service.mapper;

import cz.meind.application.Application;
import cz.meind.database.EntityMetadata;
import cz.meind.interfaces.Column;
import cz.meind.interfaces.JoinColumn;
import cz.meind.interfaces.ManyToMany;
import cz.meind.interfaces.ManyToOne;


import java.lang.reflect.Field;

import java.sql.*;
import java.util.*;


public class ObjectMapper {
    private final Connection connection;
    private final RelationMapper relationMapper;

    public ObjectMapper(Connection connection) {
        this.connection = connection;
        relationMapper = new RelationMapper(connection, this);
    }

    public Integer save(Object o) {
        try {
            return completeSave(o);
        } catch (Exception e) {
            Application.logger.error(ObjectMapper.class, e);
        }
        return null;
    }

    private Integer completeSave(Object entity) throws IllegalAccessException, SQLException {
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

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(metadata.getTableName()).append(" (");
        StringBuilder values = new StringBuilder(" VALUES (");
        List<Object> params = new ArrayList<>();
        Map<Object, Field> mtm = new HashMap<>();
        Map<String, Integer> relationFields = new HashMap<>();
        for (Map.Entry<String, Field> relations : metadata.getRelations().entrySet()) {
            Field relationField = relations.getValue();
            relationField.setAccessible(true);
            if (relationField.isAnnotationPresent(ManyToOne.class)) {
                Integer id = completeSave(relationField.get(entity));
                if (id != null) relationFields.put(relationField.getAnnotation(JoinColumn.class).name(), id);
            } else if (relationField.isAnnotationPresent(ManyToMany.class)) {
                for (Object o : (Collection<?>) relationField.get(entity)) {
                    Field idFieldRelation = getIdField(o.getClass());
                    if (idFieldRelation == null) {
                        Application.logger.error(ObjectMapper.class, new IllegalArgumentException("Entity " + o.getClass().getName() + " does not have an ID field"));
                        continue;
                    }
                    idFieldRelation.setAccessible(true);
                    if (idFieldRelation.get(o).toString().equals("0"))
                        Application.logger.error(ObjectMapper.class, new IllegalArgumentException("Save related entities first"));
                    mtm.put(o, relationField);
                }
            }
        }

        for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
            sql.append(columnEntry.getKey()).append(",");
            values.append("?,");
            Field field = getField(clazz, columnEntry.getValue());
            if (field == null) continue;
            field.setAccessible(true);
            params.add(field.get(entity));
        }

        for (Map.Entry<String, Integer> rel : relationFields.entrySet()) {
            values.append("?,");
            sql.append(rel.getKey()).append(",");
            params.add(rel.getValue());
        }

        sql.setLength(sql.length() - 1); // Remove trailing comma
        values.setLength(values.length() - 1); // Remove trailing comma
        sql.append(")").append(values).append(")");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            Application.logger.info(ObjectMapper.class, sql.toString());
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idField.setAccessible(true);

                    Object generatedKey = generatedKeys.getObject(1); // Get the generated key
                    Class<?> idFieldType = idField.getType();         // Determine the field's type

                    if (Number.class.isAssignableFrom(idFieldType) || idFieldType.isPrimitive()) {
                        Number keyAsNumber = (Number) generatedKey; // Ensure it's a Number
                        Object convertedKey = castNumberToType(keyAsNumber, idFieldType);
                        idField.set(entity, convertedKey);
                    } else {
                        Application.logger.error(ObjectMapper.class, new IllegalArgumentException("Unsupported id field type: " + idFieldType));
                    }
                }
            }
        }
        for (Map.Entry<Object, Field> m : mtm.entrySet()) {
            relationMapper.saveAllRelations(idField.get(entity).toString(), m.getKey(), m.getValue());
        }
        return (Integer) idField.get(entity);
    }

    public <T> List<T> fetchAll(Class<T> clazz) {
        EntityMetadata metadata = Application.database.entities.get(clazz);
        String sql = "SELECT * FROM " + metadata.getTableName();
        List<T> entities = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Application.logger.info(ObjectMapper.class, sql);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    T entity = clazz.getDeclaredConstructor().newInstance();
                    mapFields(entity, clazz, rs);
                    entities.add(entity);
                }
            } catch (Exception e) {
                Application.logger.error(ObjectMapper.class, e);
            }
        } catch (SQLException e) {
            Application.logger.error(ObjectMapper.class, e);
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
        String sql = "SELECT * FROM " + metadata.getTableName() + " WHERE " + idColumn.getAnnotation(Column.class).name() + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Application.logger.info(ObjectMapper.class, sql);
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                T entity = clazz.getDeclaredConstructor().newInstance();
                if (rs.next()) {
                    mapFields(entity, clazz, rs);
                    return entity;
                }
            } catch (Exception e) {
                Application.logger.error(ObjectMapper.class, e);
            }
        } catch (SQLException e) {
            Application.logger.error(ObjectMapper.class, e);
        }
        return null;
    }


    void fetchById(String id, Class<?> clazz, Object entity) throws Exception {
        EntityMetadata metadata = Application.database.entities.get(clazz);
        Field idColumn = getIdField(clazz);
        if (idColumn == null) throw new SQLException("No id column");
        String sql = "SELECT * FROM " + metadata.getTableName() + " WHERE " + idColumn.getAnnotation(Column.class).name() + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Application.logger.info(ObjectMapper.class, sql);
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
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
                        if (relationType.equals("ManyToOne")) {
                            relationField.set(entity, fetchById(relationField.getType(), (Integer) rs.getObject(relationField.getAnnotation(JoinColumn.class).name())));
                        }
                    }
                }
            }
        }
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
            if (relationType.equals("ManyToOne")) {
                Utils.set(relationField, entity, fetchById(relationField.getType(), (Integer) rs.getObject(relationField.getAnnotation(JoinColumn.class).name())));
            } else if (relationType.equals("ManyToMany")) {
                Utils.set(relationField, entity, relationMapper.fetchAllRelations(idField.get(entity).toString(), relationField));
            }
        }
    }

    Field getIdField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).id()) {
                return field;
            }
        }
        return null;
    }

    private Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Application.logger.error(ObjectMapper.class, new RuntimeException("Field not found: " + fieldName, e));
        }
        return null;
    }

    private Object castNumberToType(Number number, Class<?> targetType) {
        if (targetType == int.class || targetType == Integer.class) {
            return number.intValue();
        } else if (targetType == long.class || targetType == Long.class) {
            return number.longValue();
        } else if (targetType == double.class || targetType == Double.class) {
            return number.doubleValue();
        } else if (targetType == float.class || targetType == Float.class) {
            return number.floatValue();
        } else if (targetType == short.class || targetType == Short.class) {
            return number.shortValue();
        } else if (targetType == byte.class || targetType == Byte.class) {
            return number.byteValue();
        }
        throw new IllegalArgumentException("Unsupported number type: " + targetType);
    }
}