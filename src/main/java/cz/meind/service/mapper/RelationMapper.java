package cz.meind.service.mapper;

import cz.meind.application.Application;
import cz.meind.database.EntityMetadata;
import cz.meind.interfaces.JoinColumn;
import cz.meind.interfaces.ManyToMany;
import cz.meind.interfaces.ManyToOne;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cz.meind.service.mapper.Utils.getField;
import static cz.meind.service.mapper.Utils.getIdField;

public class RelationMapper {

    private final Connection connection;

    private final ObjectMapper mapper;

    public RelationMapper(Connection connection, ObjectMapper mapper) {
        this.connection = connection;
        this.mapper = mapper;
    }

    public void deleteAllManyToManyById(Integer id, Field relationField) throws IllegalAccessException {
        String tableName = relationField.getAnnotation(ManyToMany.class).joinTable();
        String idColumn = relationField.getAnnotation(ManyToMany.class).mappedBy();
        String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Application.logger.info(RelationMapper.class, sql);
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            Application.logger.error(RelationMapper.class, e);
        }
    }

    public <T> List<T> fetchAllRelationsManyToMany(String id, Field relationField) {
        List<T> entities = new ArrayList<>();
        String tableName = relationField.getAnnotation(ManyToMany.class).joinTable();
        String idColumn = relationField.getAnnotation(ManyToMany.class).mappedBy();
        String sql = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Application.logger.info(RelationMapper.class, sql);
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                ParameterizedType type = (ParameterizedType) relationField.getGenericType();
                Class<T> clazz = (Class<T>) type.getActualTypeArguments()[0];
                while (rs.next()) {
                    T entity = clazz.getDeclaredConstructor().newInstance();
                    mapper.fetchById(Integer.valueOf(rs.getObject(relationField.getAnnotation(ManyToMany.class).targetColumn()).toString()), clazz, entity);
                    entities.add(entity);
                }
            } catch (Exception e) {
                Application.logger.error(RelationMapper.class, e);
            }
        } catch (SQLException e) {
            Application.logger.error(RelationMapper.class, e);
        }
        return entities;

    }

    public <T> List<T> fetchAllRelationsManyToOne(String id, Field relationField) {
        List<T> entities = new ArrayList<>();
        String tableName = relationField.getAnnotation(ManyToOne.class).tableName();
        String idColumn = relationField.getAnnotation(JoinColumn.class).name();
        String sql = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Application.logger.info(RelationMapper.class, sql);
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                ParameterizedType type = (ParameterizedType) relationField.getGenericType();
                Class<T> clazz = (Class<T>) type.getActualTypeArguments()[0];
                Field idField = getIdField(clazz);
                if (idField == null) return new ArrayList<>();
                idField.setAccessible(true);
                EntityMetadata metadata = Application.database.entities.get(clazz);
                while (rs.next()) {
                    T entity = clazz.getDeclaredConstructor().newInstance();
                    for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
                        String columnName = columnEntry.getKey();
                        String fieldName = columnEntry.getValue();
                        Field field = getField(clazz, fieldName);
                        if (field == null) continue;
                        field.setAccessible(true);
                        Utils.set(field, entity, rs.getObject(columnName));
                    }
                    for (Map.Entry<String, Field> relationEntry : metadata.getRelations().entrySet()) {
                        Field field = relationEntry.getValue();
                        field.setAccessible(true);
                        String relationType = relationEntry.getKey();
                        if (relationType.equals("ManyToMany")) {
                            Utils.set(field, entity, fetchAllRelationsManyToMany(idField.get(entity).toString(), field));
                        } else if (relationType.equals("ManyToOne")) {
                            Utils.set(field, entity, fetchAllRelationsManyToOne(idField.get(entity).toString(), field));
                        }
                    }
                    entities.add(entity);
                }
            } catch (Exception e) {
                Application.logger.error(RelationMapper.class, e);
            }
        } catch (SQLException e) {
            Application.logger.error(RelationMapper.class, e);
        }

        return entities;
    }

    public void saveAllRelations(String id, Object o, Field relationField) {
        String tableName = relationField.getAnnotation(ManyToMany.class).joinTable();
        String idColumn = relationField.getAnnotation(ManyToMany.class).mappedBy();
        String sql = "INSERT INTO " + tableName + " (" + idColumn + ", " + relationField.getAnnotation(ManyToMany.class).targetColumn() + ") VALUES (?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Application.logger.info(RelationMapper.class, sql);
            Field idFieldRelation = getIdField(o.getClass());
            if (idFieldRelation == null) return;
            idFieldRelation.setAccessible(true);
            stmt.setObject(1, id);
            stmt.setObject(2, idFieldRelation.get(o));
            stmt.executeUpdate();

        } catch (SQLException | IllegalAccessException e) {
            Application.logger.error(RelationMapper.class, e);
        }
    }

    public void updateAllRelations(String id, Object o, Field relationField) {
        String tableName = relationField.getAnnotation(ManyToMany.class).joinTable();
        String idColumn = relationField.getAnnotation(ManyToMany.class).mappedBy();
        String sql = "INSERT INTO " + tableName + " (" + idColumn + ", " + relationField.getAnnotation(ManyToMany.class).targetColumn() + ") VALUES (?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Application.logger.info(RelationMapper.class, sql);
            Field idFieldRelation = getIdField(o.getClass());
            if (idFieldRelation == null) return;
            idFieldRelation.setAccessible(true);
            stmt.setObject(1, id);
            stmt.setObject(2, idFieldRelation.get(o));
            stmt.executeUpdate();

        } catch (SQLIntegrityConstraintViolationException ignored) {

        } catch (SQLException | IllegalAccessException e) {
            Application.logger.error(RelationMapper.class, e);
        }
    }
}
