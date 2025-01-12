package cz.meind.service;

import cz.meind.database.entities.Customer;
import cz.meind.database.entities.Order;
import cz.meind.database.entities.Product;

import java.time.LocalDateTime;
import java.util.*;


public class Console {
    private static final Scanner scanner = new Scanner(System.in);
    private static Customer currentCustomer;
    private static boolean loggedIn = false;
    private static List<Order> cart;
    private static boolean inListing;
    private static boolean inCart;

    public static void run() {
        do {
            if (loggedIn) {
                if (inListing) {
                    System.out.print(printProductActions());
                } else {
                    System.out.print(printActions());
                }
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
                [1] View cart
                [2] Pay
                [3] Logout
                [4] Delete account
                [Exit] Exit
                
                Command:""";
    }

    private static String printProductActions() {
        return """
                [Exit] Exit
                
                Enter product id to add to cart:""";
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
            if (inListing) {
                addToCart(choice);
                inListing = false;
            } else {
                switch (choice) {
                    case 0:
                        viewProducts();
                        break;
                    case 1:
                        viewCart();
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
    }

    private static void addToCart(int productId) {
        if (cart.isEmpty()) {
            Product p = Actions.getProductById(productId);
            if (p == null) {
                System.err.println("Invalid product id");
                return;
            }
            List<Product> products = new ArrayList<>();
            products.add(p);
            Order order = new Order();
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("new");
            order.setCustomer(currentCustomer);
            order.setTotalPrice(p.getPrice());
            order.setProducts(products);
            cart.add(order);
            Actions.saveOrder(order);
        } else {
            Order order = cart.get(0);
            Product p = Actions.getProductById(productId);
            if (p == null) {
                System.err.println("Invalid product id");
                return;
            }
            order.getProducts().add(p);
            order.setTotalPrice(order.getTotalPrice() + p.getPrice());
            Actions.editOrder(order);
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
        cart = new ArrayList<>();
        initCart();
        System.out.println("Logged in as: " + currentCustomer.getName());
    }

    private static void signup() {
        String name;
        String email;
        String address;
        String phone;
        do {
            System.out.print("Name:");
            name = scanner.next().strip().replace(" ", "");
            if (name.length() < 4 || name.length() > 8 || !name.matches("[a-zA-Z]*")) continue;
            System.out.print("Email: [*@*.*]");
            email = scanner.next().strip().replace(" ", "");
            if (email.length() > 50 || !email.matches("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,6}$")) continue;
            System.out.print("Address: ");
            address = scanner.next().strip().replace(" ", "");
            if (address.length() < 4 || address.length() > 50) continue;
            System.out.print("Phone: ");
            phone = scanner.next().strip().replace(" ", "");
            if (phone.length() < 4 || phone.length() > 50 || !phone.matches("[+]*[0-9]*")) continue;
            break;
        } while (true);
        Customer c = Actions.signup(name, email, address, phone);
        if (c == null) {
            System.err.println("Invalid signup.");
            return;
        }
        currentCustomer = c;
        loggedIn = true;
        cart = new ArrayList<>();
        initCart();
        System.out.println("Logged in as: " + currentCustomer.getName());
    }

    private static void viewProducts() {
        List<Product> products = Actions.getProducts();
        System.out.println("Available products:");
        for (Product product : products) {
            System.out.println("[" + product.getId() + "]" + product.getName() + " - " + product.getPrice() + " Kč");
        }
        inListing = true;
    }

    private static void viewCart() {
        System.out.println(printCart());

    }

    private static void initCart() {
        List<Order> orders = new ArrayList<>(currentCustomer.getOrders());
        for (Order order : orders) {
            if (order.getStatus().equals("new")) cart.add(order);
        }
    }
    private static String printCart() {
        return cart.toString();
    }

    private static void pay() {

    }

    private static void logout() {
        loggedIn = false;
        currentCustomer = null;
        cart = null;
        inListing = false;
    }

    private static void deleteAccount() {
        Actions.deleteCustomer(currentCustomer);
        logout();
        System.out.println("Account deleted.");
    }
}

