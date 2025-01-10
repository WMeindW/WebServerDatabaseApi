package cz.meind.service;

import cz.meind.application.Application;

import java.util.*;


public class Console {
    private static final Scanner scanner = new Scanner(System.in);

    private static boolean loggedIn = false;

    public static void run() {
        do {
            if (loggedIn) {

            }else {
                System.out.print(printLogin());
            }
            Integer command = Integer.valueOf(scanner.next().strip());
        } while (true);
    }

    private static String printLogin() {
        return """
                Hello!
                [0] Login
                [1] Register
                [Exit] Exit
                
                Command:""";
    }
}
