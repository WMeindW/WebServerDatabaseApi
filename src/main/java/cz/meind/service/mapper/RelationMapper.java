package cz.meind.service.mapper;

import cz.meind.application.Application;
import cz.meind.interfaces.ManyToMany;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RelationMapper {

    private final Connection connection;

    private final ObjectMapper mapper;

    public RelationMapper(Connection connection, ObjectMapper mapper) {
        this.connection = connection;
        this.mapper = mapper;
    }

    public <T> Collection<T> fetchAllRelations(String id, Field relationField) {
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
                    mapper.fetchById(rs.getObject(relationField.getAnnotation(ManyToMany.class).targetColumn()).toString(), clazz, entity);
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
            Field idFieldRelation = mapper.getIdField(o.getClass());
            idFieldRelation.setAccessible(true);
            stmt.setObject(1, id);
            stmt.setObject(2, idFieldRelation.get(o));
            stmt.executeUpdate();
        } catch (SQLException | IllegalAccessException e) {
            Application.logger.error(RelationMapper.class, e);
        }
    }

}
