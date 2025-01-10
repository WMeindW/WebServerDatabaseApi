package cz.meind.service;

import cz.meind.database.entities.Customer;

import java.util.*;


public class Console {
    private static final Scanner scanner = new Scanner(System.in);
    private static Customer currentCustomer;
    private static boolean loggedIn = false;

    public static void run() {
        do {
            if (loggedIn) {
                System.out.println("Logged in as: " + currentCustomer.getName());
                System.out.print(printActions());
            } else {
                System.out.print(printLogin());
            }
            execute(scanner.next().strip());
        } while (true);
    }

    private static String printLogin() {
        return """
                
                [0] Login
                [1] Signup
                [Exit] Exit
                
                Command:""";
    }

    private static String printActions() {
        return """
                
                [0] View products
                [1] Add to cart
                [2] Pay
                [3] Logout
                [4] Delete account
                [Exit] Exit
                
                Command:""";
    }

    private static void execute(String command) {
        if (command.equalsIgnoreCase("exit")) System.exit(0);
        int choice;
        try {
            choice = Integer.parseInt(command);
        } catch (Exception e) {
            System.err.println("Invalid command, number expected.");
            return;
        }
        if (!loggedIn) {
            switch (choice) {
                case 0:
                    login();
                    break;
                case 1:
                    signup();
                    break;
                default:
                    System.err.println("Invalid command, number out of range.");
            }
        } else {
            switch (choice) {
                case 0:
                    viewProducts();
                    break;
                case 1:
                    addToCart();
                    break;
                case 2:
                    pay();
                    break;
                case 3:
                    logout();
                    break;
                case 4:
                    deleteAccount();
                    break;
                default:
                    System.err.println("Invalid command, number out of range.");
            }
        }
    }

    private static void login() {
        System.out.print("Name: ");
        Customer c = Actions.login(scanner.next().strip().replace(" ", ""));
        if (c == null) {
            System.err.println("Invalid login.");
            return;
        }
        loggedIn = true;
        currentCustomer = c;
    }

    private static void signup() {

    }

    private static void viewProducts() {

    }

    private static void addToCart() {

    }

    private static void pay() {

    }

    private static void logout() {
        loggedIn = false;
        currentCustomer = null;
    }

    private static void deleteAccount() {
        Actions.deleteCustomer(currentCustomer);
        logout();
        System.out.println("Account deleted.");
    }
}

