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
    private static List<Order> cart;
    private static List<Product> products;
    private static boolean loggedIn;
    private static boolean inListing;
    private static boolean payment;

    /**
     * Runs the main console application loop.
     * This method initializes the product list and continuously prompts the user for input
     * based on their current state (logged in, in product listing, or in payment process).
     * It then executes the appropriate action based on the user's input.
     * <p>
     * The loop continues indefinitely until the user chooses to exit the application.
     * <p>
     * No parameters are required for this method.
     * <p>
     * This method does not return any value as it runs continuously until program termination.
     */
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
                [2] Import products (.csv)
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

    private static String printCart() {
        StringBuilder sb = new StringBuilder();
        for (Order order : cart) {
            sb.append("Order #").append(order.getId()).append(" - ").append(order.getOrderDate()).append(" - Payments: ").append(order.getPayments().size()).append("\n");
            sb.append("Products:\n");
            for (Product product : order.getProducts()) {
                sb.append("[").append(product.getId()).append("]").append(product.getName()).append(" - ").append(product.getPrice()).append(" Kč\n");
            }
            sb.append("Remaining Price: ").append(order.getTotalPrice()).append(" Kč\n");
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Executes the appropriate action based on the user's input.
     * This method handles different states of the application (logged in, in product listing, or in payment process)
     * and performs the corresponding actions based on the user's choice.
     *
     * @param command The user's input command.
     */
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
                case 2:
                    importData();
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
                        payment = false;
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
                        System.out.println(printCart());
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

    /**
     * Adds a product to the shopping cart.
     * If the product is not already in the cart, a new order is created and the product is added to it.
     * If the product is already in the cart, it is added to the existing order.
     *
     * @param productId The unique identifier of the product to be added to the cart.
     * @throws NullPointerException  If the product with the given productId does not exist in the product list.
     * @throws IllegalStateException If the cart is null or empty.
     */
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

    /**
     * Handles the login process for the console application.
     * Prompts the user to enter their name, attempts to authenticate the user,
     * and initializes the shopping cart for the logged-in customer.
     *
     * @throws NullPointerException If the customer with the given name does not exist in the database.
     */
    private static void login() {
        System.out.print("Name: ");
        Customer c = Actions.login(scanner.next().strip().replace(" ", ""));
        if (c == null) {
            System.err.println("Invalid login.");
            return;
        }
        loggedIn = true;
        currentCustomer = c;
        initCart();
        System.out.println("Logged in as: " + currentCustomer.getName());
    }

    /**
     * Handles the signup process for the console application.
     * Prompts the user to enter their name, email, address, and phone number,
     * validates the input, and attempts to create a new customer in the database.
     * If the signup is successful, the customer is logged in, and the shopping cart is initialized.
     *
     * @throws NullPointerException If the customer with the given name does not exist in the database.
     */
    private static void signup() {
        String name;
        String email;
        String address;
        String phone;

        // Loop until valid signup information is provided
        do {
            System.out.print("Name:");
            name = scanner.nextLine().strip().replace(" ", "");

            // Validate name length and format
            if (name.length() < 4 || name.length() > 8 || !name.matches("[a-zA-Z]*")) continue;

            System.out.print("Email: [*@*.*]");
            email = scanner.next().strip().replace(" ", "");

            // Validate email length and format
            if (email.length() > 50 || !email.matches("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z]{2,6}")) continue;

            System.out.print("Address: ");
            address = scanner.next().strip().replace(" ", "");

            // Validate address length
            if (address.length() < 4 || address.length() > 50) continue;

            System.out.print("Phone: ");
            phone = scanner.next().strip().replace(" ", "");

            // Validate phone length and format
            if (phone.length() < 4 || phone.length() > 50 || !phone.matches("[+]*[0-9]*")) continue;

            break;
        } while (true);

        // Attempt to create a new customer in the database
        Customer c = Actions.signup(name, email, address, phone);

        // Handle invalid signup
        if (c == null) {
            System.err.println("Invalid signup.");
            return;
        }

        // Set the current customer and update the login status
        currentCustomer = c;
        loggedIn = true;

        // Initialize the shopping cart for the logged-in customer
        initCart();

        // Display successful login message
        System.out.println("Logged in as: " + currentCustomer.getName());
    }

        /**
     * Displays the list of available products to the user.
     * This function iterates through the product list and prints each product's ID, name, and price.
     * It also sets the 'inListing' flag to true, indicating that the user is currently browsing the product list.
     *
     * @return void
     */
    private static void viewProducts() {
        System.out.println("Available products:");
        for (Product product : products) {
            System.out.println("[" + product.getId() + "]" + product.getName() + " - " + product.getPrice() + " Kč");
        }
        inListing = true;
    }

    private static void initCart() {
        cart = new ArrayList<>(currentCustomer.getOrders().stream().filter(order -> order.getStatus().equals("new")).toList());
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
            amount = Float.parseFloat(scanner.next().strip().replace(" ", ""));
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
                o.getPayments().add(p);
                o.setTotalPrice(o.getTotalPrice() - amount);
                payments.add(p);
                amount = 0f;
            } else {
                Payment p = new Payment(expiryDate, cardNumber, cvv, cardHolderName, o.getTotalPrice());
                amount = amount - o.getTotalPrice();
                p.setOrder(o);
                o.getPayments().add(p);
                o.setStatus("completed");
                payments.add(p);
                o.setTotalPrice(0);
            }
        }
        System.out.println("Change: " + amount);
        Actions.savePayments(payments);
        Actions.payTransaction(cart);
        System.out.println("Payment successful!");
        cart = cart.stream().filter(order -> order.getStatus().equals("new")).toList();
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

    private static void importData() {
        System.out.println("Set file path: ");
        Actions.importFile(scanner.next().strip());
    }

    public static void exit() {
        scanner.close();
        Application.database.closeConnection();
        Application.logger.info(Console.class, "Shutting down.");
        System.exit(0);
    }
}