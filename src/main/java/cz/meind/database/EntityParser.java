package cz.meind.database;

import cz.meind.interfaces.*;

import java.lang.reflect.Field;

public class EntityParser {
    public static EntityMetadata parseEntity(Class<?> clazz) {
        EntityMetadata metadata = new EntityMetadata();

        if (clazz.isAnnotationPresent(Entity.class)) {
            Entity entity = clazz.getAnnotation(Entity.class);
            metadata.setTableName(entity.tableName().isEmpty() ? clazz.getSimpleName() : entity.tableName());
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column column = field.getAnnotation(Column.class);
                String columnName = column.name().isEmpty() ? field.getName() : column.name();
                metadata.addColumn(columnName, field.getName());
            }

            if (field.isAnnotationPresent(OneToMany.class)) {
                metadata.addRelation("OneToMany", field);
            }

            if (field.isAnnotationPresent(ManyToMany.class)) {
                metadata.addRelation("ManyToMany", field);
            }

            if (field.isAnnotationPresent(ManyToOne.class)) {
                metadata.addRelation("ManyToOne", field);
            }
        }

        return metadata;
    }
}
