package cz.meind.service;

import cz.meind.application.Application;
import cz.meind.database.entities.Customer;
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
        Customer c = mapper.fetchById(Customer.class, 1);
        c.setName("Koště");
        mapper.update(c);
        do {
            try {
                System.out.print(print(actions));
                Integer command = Integer.valueOf(scanner.next().strip());
                System.out.println(actions.get(command));
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

    private static void fillActionMap() {
        ArrayList<Method> methods = new ArrayList<>();
        for (Method method : mapper.getClass().getDeclaredMethods())
            if (Modifier.isPublic(method.getModifiers())) methods.add(method);
        for (int i = 0; i < methods.size(); i++)
            actions.put(i + 1, methods.get(i).getName());
    }
}
