package cz.meind.service;

import cz.meind.database.EntityMetadata;
import cz.meind.database.EntityParser;
import cz.meind.interfaces.Column;
import cz.meind.interfaces.OneToMany;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // Save an entity to the database
    public void save(Object entity) throws Exception {
        Class<?> clazz = entity.getClass();
        EntityMetadata metadata = metadataRegistry.get(clazz);

        StringBuilder sql = new StringBuilder("INSERT INTO ").append(metadata.getTableName()).append(" (");
        StringBuilder values = new StringBuilder(" VALUES (");

        List<Object> params = new ArrayList<>();
        for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
            sql.append(columnEntry.getKey()).append(",");
            values.append("?,");
            Field field = getField(clazz, columnEntry.getValue());
            field.setAccessible(true);
            params.add(field.get(entity));
        }

        sql.setLength(sql.length() - 1); // Remove trailing comma
        values.setLength(values.length() - 1); // Remove trailing comma
        sql.append(")").append(values).append(")");

        try (PreparedStatement stmt = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            stmt.executeUpdate();

            // Handle auto-generated keys
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Field idField = getIdField(clazz);
                    if (idField != null) {
                        idField.setAccessible(true);
                        idField.set(entity, generatedKeys.getObject(1));
                    }
                }
            }
        }

        // Handle relationships
        for (Map.Entry<String, Field> relationEntry : metadata.getRelations().entrySet()) {
            Field relationField = relationEntry.getValue();
            relationField.setAccessible(true);
            Object relatedEntity = relationField.get(entity);
            if (relatedEntity != null) {
                save(relatedEntity); // Recursive save
            }
        }
    }

    // Fetch all entities of a given class
    public <T> List<T> fetchAll(Class<T> clazz) throws Exception {
        EntityMetadata metadata = metadataRegistry.get(clazz);
        String sql = "SELECT * FROM " + metadata.getTableName();
        List<T> entities = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                T entity = clazz.getDeclaredConstructor().newInstance();
                for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
                    String columnName = columnEntry.getKey();
                    String fieldName = columnEntry.getValue();
                    Field field = getField(clazz, fieldName);
                    field.setAccessible(true);
                    field.set(entity, rs.getObject(columnName));
                }
                entities.add(entity);

                // Handle relationships
                for (Map.Entry<String, Field> relationEntry : metadata.getRelations().entrySet()) {
                    Field relationField = relationEntry.getValue();
                    relationField.setAccessible(true);
                    String relationType = relationEntry.getKey();
                    if ("OneToMany".equals(relationType)) {
                        List<?> relatedEntities = fetchRelatedEntities(clazz, relationField, entity);
                        relationField.set(entity, relatedEntities);
                    }
                }
            }
        }

        return entities;
    }

    private <T> List<T> fetchRelatedEntities(Class<?> parentClass, Field relationField, Object parentEntity) throws Exception {
        OneToMany oneToMany = relationField.getAnnotation(OneToMany.class);
        String mappedBy = oneToMany.mappedBy();
        Class<?> relatedClass = relationField.getType().getComponentType();

        EntityMetadata relatedMetadata = metadataRegistry.get(relatedClass);
        String relatedTableName = relatedMetadata.getTableName();

        Field parentField = getField(relatedClass, mappedBy);
        String sql = "SELECT * FROM " + relatedTableName + " WHERE " + mappedBy + " = ?";

        List<T> relatedEntities = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Field parentIdField = getIdField(parentClass);
            parentIdField.setAccessible(true);
            stmt.setObject(1, parentIdField.get(parentEntity));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    T relatedEntity = (T) relatedClass.getDeclaredConstructor().newInstance();
                    for (Map.Entry<String, String> columnEntry : relatedMetadata.getColumns().entrySet()) {
                        String columnName = columnEntry.getKey();
                        String fieldName = columnEntry.getValue();
                        Field field = getField(relatedClass, fieldName);
                        field.setAccessible(true);
                        field.set(relatedEntity, rs.getObject(columnName));
                    }
                    relatedEntities.add(relatedEntity);
                }
            }
        }
        return relatedEntities;
    }

    private Field getIdField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).unique()) {
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
}

