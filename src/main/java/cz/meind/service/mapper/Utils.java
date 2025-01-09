package cz.meind.service.mapper;

import cz.meind.application.Application;
import cz.meind.interfaces.Column;

import java.lang.reflect.Field;

public class Utils {
    static void set(Field field, Object entity, Object value) {
        try {
            field.setAccessible(true);
            field.set(entity, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Application.logger.error(Utils.class, e);
        }
    }

    static Object castNumberToType(Number number, Class<?> targetType) {
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

    static Field getIdField(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).id()) {
                return field;
            }
        }
        return null;
    }

    static Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Application.logger.error(ObjectMapper.class, new RuntimeException("Field not found: " + fieldName, e));
        }
        return null;
    }

}
