package cz.meind.database;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class EntityMetadata {
    private String tableName;
    private final Map<String, String> columns = new HashMap<>(); // ColumnName -> FieldName
    private final Map<String, Field> relations = new HashMap<>(); // RelationType -> Field


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, String> getColumns() {
        return columns;
    }

    public void addColumn(String columnName, String fieldName) {
        this.columns.put(columnName, fieldName);
    }

    public Map<String, Field> getRelations() {
        return relations;
    }

    public void addRelation(String relationType, Field field) {
        this.relations.put(relationType, field);
    }
}
