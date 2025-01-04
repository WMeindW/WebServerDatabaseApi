package cz.meind.service;

import cz.meind.application.Application;
import cz.meind.interfaces.Api;
import cz.meind.interfaces.GetMapping;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;

public class ContextLoader {
    private final HashMap<String, Method> routes = new HashMap<>();

    public Method getRoute(String path) {
        return routes.get(path);
    }

    public ContextLoader() {
        Application.logger.info(ContextLoader.class, "Loading context.");
        Reflections reflections = new Reflections("cz.meind");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Api.class);
        for (Class<?> clazz : classes) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(GetMapping.class)) {
                    String path = clazz.getAnnotation(Api.class).value().strip();
                    if (path.endsWith("/"))
                        path = path.substring(0, path.length() - 1);
                    routes.put(path + method.getAnnotation(GetMapping.class).value().strip(), method);
                }
            }
        }
    }
}
