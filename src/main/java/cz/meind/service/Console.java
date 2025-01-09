package cz.meind.service;

import cz.meind.application.Application;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static cz.meind.application.Application.mapper;

public class Console {
    private static final HashMap<Integer, String> classes = new HashMap<>();
    private static final HashMap<Integer, String> actions = new HashMap<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void run() {
        fillClassMap();
        fillActionMap();
        do {
            try {
                System.out.print(print(actions));
                String command = actions.get(Integer.valueOf(scanner.next().strip()));
                if (command != null) command(command);
            } catch (Exception e) {
                Application.logger.error(Console.class, "Exception occurred, wrong input");
            }
        } while (true);
    }

    private static String print(HashMap<Integer, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Map.Entry<Integer, String> entry : map.entrySet())
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        sb.append("Choose: ");
        return sb.toString();
    }

    private static void fillClassMap() {
        for (int i = 0; i < Application.database.entities.size(); i++)
            classes.put(i + 1, Application.database.entities.keySet().stream().toList().get(i).getSimpleName());
    }

    private static void command(String action) {
        Method method = getMethod(action);
        if (method == null){
            Application.logger.error(Console.class, "Action not found: " + action);
            return;
        }
        System.out.println(method);
    }

    private static void fillActionMap() {
        ArrayList<Method> methods = new ArrayList<>();
        for (Method method : mapper.getClass().getDeclaredMethods())
            if (Modifier.isPublic(method.getModifiers())) methods.add(method);
        for (int i = 0; i < methods.size(); i++)
            actions.put(i + 1, methods.get(i).getName());
    }

    private static Method getMethod(String name) {
        for (Method method : mapper.getClass().getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && method.getName().equals(name)) return method;
        }
        return null;
    }
}
