package cz.meind.service;

import cz.meind.database.EntityMetadata;
import cz.meind.database.EntityParser;
import cz.meind.interfaces.Column;
import cz.meind.interfaces.JoinColumn;
import cz.meind.interfaces.ManyToMany;
import cz.meind.interfaces.ManyToOne;


import java.lang.reflect.Field;

import java.lang.reflect.ParameterizedType;
import java.sql.*;
import java.util.*;


public class ObjectMapper {
    private final Map<Class<?>, EntityMetadata> metadataRegistry = new HashMap<>();
    private final Connection connection;

    public ObjectMapper(Connection connection) {
        this.connection = connection;
    }

    public void registerEntity(Class<?> clazz) {
        EntityMetadata metadata = EntityParser.parseEntity(clazz);
        metadataRegistry.put(clazz, metadata);
    }

    public Integer save(Object entity) throws Exception {
        Class<?> clazz = entity.getClass();
        EntityMetadata metadata = metadataRegistry.get(clazz);

        Field idField = getIdField(clazz);
        if (idField == null) {
            throw new IllegalArgumentException("Entity " + clazz.getName() + " does not have an ID field");
        }
        idField.setAccessible(true);
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
                Integer id = save(relationField.get(entity));
                if (id != null) relationFields.put(relationField.getAnnotation(JoinColumn.class).name(), id);
            } else if (relationField.isAnnotationPresent(ManyToMany.class)) {
                for (Object o : (Collection<?>) relationField.get(entity)) {
                    Field idFieldRelation = getIdField(o.getClass());
                    if (idFieldRelation == null)
                        throw new IllegalArgumentException("Entity " + o.getClass().getName() + " does not have an ID field");
                    idFieldRelation.setAccessible(true);
                    if (idFieldRelation.get(o).toString().equals("0"))
                        throw new IllegalArgumentException("Save related entities first.");
                    mtm.put(o, relationField);
                }
            }
        }

        for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
            sql.append(columnEntry.getKey()).append(",");
            values.append("?,");
            Field field = getField(clazz, columnEntry.getValue());
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
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    idField.setAccessible(true);

                    Object generatedKey = generatedKeys.getObject(1); // Get the generated key
                    Class<?> idFieldType = idField.getType();         // Determine the field's type

                    // Convert to the appropriate type if necessary
                    if (Number.class.isAssignableFrom(idFieldType) || idFieldType.isPrimitive()) {
                        Number keyAsNumber = (Number) generatedKey; // Ensure it's a Number
                        Object convertedKey = castNumberToType(keyAsNumber, idFieldType);
                        idField.set(entity, convertedKey);
                    } else {
                        throw new IllegalArgumentException("Unsupported ID field type: " + idFieldType);
                    }
                }
            }
        }
        for (Map.Entry<Object, Field> m : mtm.entrySet()) {
            saveAllRelations(idField.get(entity).toString(), m.getKey(), m.getValue());
        }
        return (Integer) idField.get(entity);
    }

    public <T> Collection<T> fetchAll(Class<T> clazz) throws Exception {
        EntityMetadata metadata = metadataRegistry.get(clazz);
        String sql = "SELECT * FROM " + metadata.getTableName();
        List<T> entities = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                T entity = clazz.getDeclaredConstructor().newInstance();
                mapFields(entity, clazz, rs);
                entities.add(entity);
            }
        }

        return entities;
    }

    public <T> T fetchById(Class<T> clazz, Number id) throws Exception {
        EntityMetadata metadata = metadataRegistry.get(clazz);
        Field idColumn = getIdField(clazz);
        if (idColumn == null) throw new SQLException("No id column");
        String sql = "SELECT * FROM " + metadata.getTableName() + " WHERE " + idColumn.getAnnotation(Column.class).name() + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                T entity = clazz.getDeclaredConstructor().newInstance();
                if (rs.next()) {
                    mapFields(entity, clazz, rs);
                    return entity;
                }
            }
        }
        return null;
    }

    public <T> Collection<T> fetchAllRelations(String id, Field relationField) throws Exception {
        List<T> entities = new ArrayList<>();
        String tableName = relationField.getAnnotation(ManyToMany.class).joinTable();
        String idColumn = relationField.getAnnotation(ManyToMany.class).mappedBy();
        String sql = "SELECT * FROM " + tableName + " WHERE " + idColumn + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                ParameterizedType type = (ParameterizedType) relationField.getGenericType();
                Class<T> clazz = (Class<T>) type.getActualTypeArguments()[0];
                while (rs.next()) {
                    T entity = clazz.getDeclaredConstructor().newInstance();
                    fetchById(rs.getObject(relationField.getAnnotation(ManyToMany.class).targetColumn()).toString(), clazz, entity);
                    entities.add(entity);
                }
            }
        }
        return entities;

    }

    private void saveAllRelations(String id, Object o, Field relationField) throws Exception {
        String tableName = relationField.getAnnotation(ManyToMany.class).joinTable();
        String idColumn = relationField.getAnnotation(ManyToMany.class).mappedBy();
        String sql = "INSERT INTO " + tableName + " (" + idColumn + ", " + relationField.getAnnotation(ManyToMany.class).targetColumn() + ") VALUES (?,?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Field idFieldRelation = getIdField(o.getClass());
            idFieldRelation.setAccessible(true);
            stmt.setObject(1, id);
            stmt.setObject(2, idFieldRelation.get(o));
            stmt.executeUpdate();
        }
    }


    private void fetchById(String id, Class<?> clazz, Object entity) throws Exception {
        EntityMetadata metadata = metadataRegistry.get(clazz);
        Field idColumn = getIdField(clazz);
        if (idColumn == null) throw new SQLException("No id column");
        String sql = "SELECT * FROM " + metadata.getTableName() + " WHERE " + idColumn.getAnnotation(Column.class).name() + " = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
                        String columnName = columnEntry.getKey();
                        String fieldName = columnEntry.getValue();
                        Field field = getField(clazz, fieldName);
                        field.setAccessible(true);
                        field.set(entity, rs.getObject(columnName));
                    }
                    for (Map.Entry<String, Field> relationEntry : metadata.getRelations().entrySet()) {
                        Field relationField = relationEntry.getValue();
                        relationField.setAccessible(true);
                        String relationType = relationEntry.getKey();
                        if (relationType.equals("ManyToOne")) {
                            relationField.set(entity, fetchById(relationField.getType(), (Number) rs.getObject(relationField.getAnnotation(JoinColumn.class).name())));
                        }
                    }
                }
            }
        }
    }

    private void mapFields(Object entity, Class<?> clazz, ResultSet rs) throws Exception {
        EntityMetadata metadata = metadataRegistry.get(clazz);
        Field idField = getIdField(clazz);
        if (idField == null) return;
        idField.setAccessible(true);
        for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
            String columnName = columnEntry.getKey();
            String fieldName = columnEntry.getValue();
            Field field = getField(clazz, fieldName);
            field.setAccessible(true);
            field.set(entity, rs.getObject(columnName));
        }
        for (Map.Entry<String, Field> relationEntry : metadata.getRelations().entrySet()) {
            Field relationField = relationEntry.getValue();
            relationField.setAccessible(true);
            String relationType = relationEntry.getKey();
            if (relationType.equals("ManyToOne")) {
                relationField.set(entity, fetchById(relationField.getType(), (Number) rs.getObject(relationField.getAnnotation(JoinColumn.class).name())));
            } else if (relationType.equals("ManyToMany")) {
                relationField.set(entity, fetchAllRelations(idField.get(entity).toString(), relationField));
            }
        }
    }

    private Field getIdField(Class<?> clazz) {
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
            throw new RuntimeException("Field not found: " + fieldName, e);
        }
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

