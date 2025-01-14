package cz.meind.service;

import cz.meind.application.Application;
import cz.meind.database.entities.Customer;
import cz.meind.database.entities.Order;
import cz.meind.database.entities.Payment;
import cz.meind.database.entities.Product;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static cz.meind.application.Application.mapper;

public class Actions {
    public static Customer signup(String name, String email, String address, String phone) {
        List<Customer> customers = mapper.fetchAll(Customer.class);
        if (!customers.stream().filter(c -> c.getName().equals(name)).toList().isEmpty()) return null;
        Customer c = new Customer();
        c.setEmail(email);
        c.setAddress(address);
        c.setName(name);
        c.setPhone(phone);
        c.setRegistrationDate(new Date());
        mapper.save(c);
        return c;
    }

    public static Customer login(String name) {
        List<Customer> customers = mapper.fetchAll(Customer.class);
        return customers.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    public static void deleteCustomer(Customer customer) {
        mapper.deleteById(Customer.class, customer.getId());
    }

    public static List<Product> getProducts() {
        return mapper.fetchAll(Product.class);
    }

    public static Product getProductById(int id) {
        return mapper.fetchById(Product.class, id);
    }

    public static void editOrder(Order order) {
        try {
            mapper.update(order);
        } catch (Exception e) {
            Application.logger.error(Actions.class, e);
        }
    }

    public static void saveOrder(Order order) {
        mapper.save(order);
    }

    public static void savePayments(List<Payment> p) {
        for (Payment payment : p)
            mapper.save(payment);
    }

    public static void payTransaction(List<Order> future) {
        Customer customer = future.get(0).getCustomer();
        List<Order> current = mapper.fetchAll(Order.class).stream().filter(order -> order.getCustomer().equals(customer)).toList();
        for (Order order : future) {
            try {
                mapper.update(order);
            } catch (Exception e) {
                revert(current);
                return;
            }
        }
    }

    public static Customer getCustomerById(Integer id) {
        return mapper.fetchById(Customer.class, id);
    }

    private static void revert(List<?> objects) {
        for (Object o : objects) {
            try {
                mapper.update(o);
            } catch (Exception e) {
                Application.logger.error(Actions.class, e);
            }
        }
    }
}
