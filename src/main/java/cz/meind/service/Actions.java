package cz.meind.service;

import cz.meind.database.entities.Customer;

import java.util.List;

import static cz.meind.application.Application.mapper;

public class Actions {
    public static Customer signup(String email, String name, String address) {
        List<Customer> customers = mapper.fetchAll(Customer.class);
        if (!customers.stream().filter(c -> c.getName().equals(name)).toList().isEmpty()) return null;
        Customer c = new Customer();
        c.setEmail(email);
        c.setAddress(address);
        c.setName(name);
        return c;
    }

    public static Customer login(String name) {
        List<Customer> customers = mapper.fetchAll(Customer.class);
        return customers.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    public static void deleteCustomer(Customer customer) {
        mapper.deleteById(Customer.class, customer.getId());
    }
}
