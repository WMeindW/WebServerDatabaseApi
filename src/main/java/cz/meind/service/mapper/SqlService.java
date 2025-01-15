package cz.meind.service.mapper;

import cz.meind.application.Application;
import cz.meind.database.EntityMetadata;
import cz.meind.interfaces.Column;
import org.apache.commons.csv.CSVRecord;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cz.meind.service.mapper.Utils.getField;

public class SqlService {
    private final Connection connection;

    public SqlService(Connection connection) {
        this.connection = connection;
    }

    public void deleteById(String tableName, String idField, Integer id) {
        String sql = "DELETE FROM " + tableName + " WHERE " + idField + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            Application.logger.info(SqlService.class, sql);
            stmt.setObject(1, id);
            stmt.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            Application.logger.error(SqlService.class, new SQLException("Delete relations first, constraint failed"));
        } catch (Exception e) {
            Application.logger.error(SqlService.class, e);
        }
    }

    public void update(EntityMetadata metadata, Class<?> clazz, Field idField, Object entity) {
        try {
            StringBuilder sql = new StringBuilder("UPDATE ").append(metadata.getTableName()).append(" SET ");
            List<Object> params = new ArrayList<>();


            for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
                Field field = getField(clazz, columnEntry.getValue());
                if (field == null || field.getName().equals(idField.getName())) continue; // Skip ID field

                field.setAccessible(true);
                sql.append(columnEntry.getKey()).append(" = ?, ");
                params.add(field.get(entity));
            }

            sql.setLength(sql.length() - 2);
            sql.append(" WHERE ").append(idField.getAnnotation(Column.class).name()).append(" = ?");
            params.add(idField.get(entity));

            try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
                Application.logger.info(SqlService.class, sql.toString());
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                stmt.executeUpdate();
            } catch (SQLException e) {
                Application.logger.error(SqlService.class, e);
            }
        } catch (IllegalAccessException e) {
            Application.logger.error(SqlService.class, e);
        }
    }

    public ResultSet fetchAll(EntityMetadata metadata) {
        String sql = "SELECT * FROM " + metadata.getTableName();
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            Application.logger.info(SqlService.class, sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            Application.logger.error(SqlService.class, e);
        }
        return null;
    }

    public ResultSet fetchById(EntityMetadata metadata, String idColumn, Integer id) {
        String sql = "SELECT * FROM " + metadata.getTableName() + " WHERE " + idColumn + " = ?";
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            Application.logger.info(SqlService.class, sql);
            stmt.setObject(1, id);
            return stmt.executeQuery();
        } catch (SQLException e) {
            Application.logger.error(SqlService.class, e);
        }
        return null;
    }

    public PreparedStatement save(EntityMetadata metadata, Class<?> clazz, Object entity, Map<String, Integer> relationFields) {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(metadata.getTableName()).append(" (");
        StringBuilder values = new StringBuilder(" VALUES (");
        List<Object> params = new ArrayList<>();
        try {
            for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
                sql.append(columnEntry.getKey()).append(",");
                values.append("?,");
                Field field = getField(clazz, columnEntry.getValue());
                if (field == null) continue;
                field.setAccessible(true);
                params.add(field.get(entity));
            }
        } catch (IllegalAccessException e) {
            Application.logger.error(SqlService.class, e);
        }


        for (Map.Entry<String, Integer> rel : relationFields.entrySet()) {
            values.append("?,");
            sql.append(rel.getKey()).append(",");
            params.add(rel.getValue());
        }

        try {
            return executeSaveStatement(sql, values, params);
        } catch (SQLException e) {
            Application.logger.error(SqlService.class, e);
            return null;
        }
    }

    public void save(EntityMetadata metadata, Class<?> clazz, CSVRecord record) throws SQLException {
        StringBuilder sql = new StringBuilder("INSERT INTO ").append(metadata.getTableName()).append(" (");
        StringBuilder values = new StringBuilder(" VALUES (");
        List<Object> params = new ArrayList<>();

        for (Map.Entry<String, String> columnEntry : metadata.getColumns().entrySet()) {
            Field field = getField(clazz, columnEntry.getValue());
            if (field == null || field.getAnnotation(Column.class).id()) continue;
            try {
                String value = record.get(columnEntry.getKey());
                if (value != null) {
                    params.add(value);
                    sql.append(columnEntry.getKey()).append(",");
                    values.append("?,");
                }
            } catch (Exception e) {
                Application.logger.error(SqlService.class, "Wrong mapping for: " + columnEntry.getKey());
            }
        }
        if (params.isEmpty()) {
            Application.logger.error(SqlService.class, new SQLException("No acceptable columns in file"));
            return;
        }
        executeSaveStatement(sql, values, params).close();
    }

    private PreparedStatement executeSaveStatement(StringBuilder sql, StringBuilder values, List<Object> params) throws SQLException {
        sql.setLength(sql.length() - 1);
        values.setLength(values.length() - 1);
        sql.append(")").append(values).append(")");
        PreparedStatement stmt = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
        Application.logger.info(SqlService.class, sql.toString());
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
        stmt.executeUpdate();
        return stmt;
    }
}
