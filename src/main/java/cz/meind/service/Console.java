package cz.meind.service;

import cz.meind.application.Application;
import cz.meind.database.entities.Order;
import cz.meind.database.entities.Payment;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static cz.meind.application.Application.mapper;

public class Console {
    private static final HashMap<Integer, String> classes = new HashMap<>();
    private static final HashMap<Integer, String> actions = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void run() {
        fillClassMap();
        fillActionMap();
        mapper.deleteById(Order.class,1);
    }

    private static String print(HashMap<Integer,String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, String> entry : map.entrySet())
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        return sb.toString();
    }

    private static void fillClassMap() {
        for (int i = 0; i < Application.database.entities.size(); i++)
            classes.put(i + 1, Application.database.entities.keySet().stream().toList().get(i).getSimpleName());
    }

    private static void fillActionMap() {
        ArrayList<Method> methods = new ArrayList<>();
        for (Method method : mapper.getClass().getDeclaredMethods())
            if (Modifier.isPublic(method.getModifiers()))
                methods.add(method);
        for (int i = 0; i < methods.size(); i++)
            actions.put(i + 1, methods.get(i).getName());
    }
}
