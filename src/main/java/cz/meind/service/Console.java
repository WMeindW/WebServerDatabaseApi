package cz.meind.service;

import cz.meind.application.Application;
import cz.meind.database.entities.Customer;
import cz.meind.database.entities.Order;
import cz.meind.database.entities.Payment;
import cz.meind.database.entities.Product;

import java.time.LocalDateTime;
import java.util.*;


public class Console {
    private static final Scanner scanner = new Scanner(System.in);
    private static Customer currentCustomer;
    private static boolean loggedIn = false;
    private static List<Order> cart;
    private static List<Product> products;
    private static boolean inListing;
    private static boolean payment;

    public static void run() {
        products = Actions.getProducts();
        do {
            if (loggedIn) {
                if (inListing) {
                    System.out.print(printProductActions());
                } else if (payment) {
                    System.out.print(printPayment());
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

    private static String printPayment() {
        return """
                [0] Pay using credit card
                [1] Cancel
                [Exit] Exit
                
                Command:""";
    }

    private static String printProductActions() {
        return """
                [0] Cancel
                [Exit] Exit
                
                Enter product id to add to cart:""";
    }


    private static void execute(String command) {
        if (command.equalsIgnoreCase("exit")) exit();
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
                if (choice != 0) addToCart(choice);
                inListing = false;
            } else if (payment) {
                switch (choice) {
                    case 0:
                        payCreditCard();
                        break;
                    case 1:
                        cancelPayment();
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
                        viewCart();
                        break;
                    case 2:
                        System.out.println(printCart());
                        payment = true;
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
        Product p = products.stream().filter(pr -> pr.getId() == productId).findFirst().get();
        Order order = null;
        for (Order o : cart) {
            if (!o.getProducts().contains(p)) order = o;
        }
        if (cart.isEmpty() || order == null) {
            List<Product> products = new ArrayList<>();
            products.add(p);
            order = new Order();
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("new");
            order.setCustomer(currentCustomer);
            order.setTotalPrice(p.getPrice());
            order.setProducts(products);
            order.setPayment(new ArrayList<>());
            cart.add(order);
            Actions.saveOrder(order);
        } else {
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
            name = scanner.nextLine().strip().replace(" ", "");
            if (name.length() < 4 || name.length() > 8 || !name.matches("[a-zA-Z]*")) continue;
            System.out.print("Email: [*@*.*]");
            email = scanner.next().strip().replace(" ", "");
            if (email.length() > 50 || !email.matches("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,6}")) continue;
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
        cart = new ArrayList<>(currentCustomer.getOrders().stream().filter(order -> order.getStatus().equals("new")).toList());
    }

    private static String printCart() {
        return cart.toString();
    }

    private static void payCreditCard() {
        String cardNumber;
        String expiryDate;
        String cvv;
        String cardHolderName;
        float amount;

        do {
            System.out.print("Card Number [16 digits]: ");
            cardNumber = scanner.next().strip().replace(" ", "");
            if (cardNumber.length() != 16 || !cardNumber.matches("\\d{16}")) continue;

            System.out.print("Expiry Date [MM/YY]: ");
            expiryDate = scanner.next().strip().replace(" ", "");
            if (!expiryDate.matches("(0[1-9]|1[0-2])/[0-9]{2}")) continue;

            System.out.print("CVV [3 digits]: ");
            cvv = scanner.next().strip().replace(" ", "");
            if (cvv.length() != 3 || !cvv.matches("\\d{3}")) continue;

            System.out.println("Cardholder Name [Name_Surname]: ");
            cardHolderName = scanner.next().strip().replace(" ", "");
            if (cardHolderName.length() < 4 || cardHolderName.length() > 50 || !cardHolderName.matches("[A-Z][a-z]+_[A-Z][a-z]+"))
                continue;

            break;
        } while (true);
        try {
            System.out.println("Amount: ");
            amount = Integer.parseInt(scanner.next().strip().replace(" ", ""));
        } catch (Exception e) {
            System.err.println("Invalid amount");
            return;
        }
        List<Payment> payments = new ArrayList<>();
        for (Order o : cart) {
            if (amount <= 0) break;
            if (o.getTotalPrice() >= amount) {
                Payment p = new Payment(expiryDate, cardNumber, cvv, cardHolderName, amount);
                p.setOrder(o);
                o.getPayment().add(p);
                payments.add(p);
                amount -= amount;
            } else {
                Payment p = new Payment(expiryDate, cardNumber, cvv, cardHolderName, o.getTotalPrice());
                p.setOrder(o);
                o.getPayment().add(p);
                o.setStatus("completed");
                payments.add(p);
                amount -= o.getTotalPrice();
            }

        }
        System.out.println(payments);
        Actions.savePayments(payments);
        Actions.payTransaction(cart);
    }

    private static void cancelPayment() {
        payment = false;
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

    private static void exit() {
        Application.logger.info(Console.class, "Exit");
        Application.database.closeConnection();
        System.exit(0);
    }
}

