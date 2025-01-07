package cz.meind.service.mapper;

import cz.meind.application.Application;

import java.lang.reflect.Field;

public class Utils {
    public static void set(Field field,Object entity, Object value){
        try {
            field.setAccessible(true);
            field.set(entity, value);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            Application.logger.error(Utils.class,e);
        }
    }
}
