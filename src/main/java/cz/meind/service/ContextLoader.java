package cz.meind.service;

import cz.meind.application.Application;

import java.lang.reflect.Method;
import java.util.HashMap;

public class ContextLoader {
    public HashMap<String, Method> routes = new HashMap<>();

    public ContextLoader() {
        Application.logger.info(ContextLoader.class, "Loading context.");
    }
}
