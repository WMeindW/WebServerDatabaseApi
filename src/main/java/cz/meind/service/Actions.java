package cz.meind.service;

import cz.meind.database.entities.Customer;

import java.util.List;
import java.util.stream.Collectors;

import static cz.meind.application.Application.mapper;

public class Actions {
    public static Customer register(String email, String name, String address) {
        List<Customer> customers = mapper.fetchAll(Customer.class);
        if (!customers.stream().filter(c -> c.getName().equals(name)).toList().isEmpty()) return null;
        Customer c = new Customer();
        c.setEmail(email);
        c.setAddress(address);
        c.setName(name);
        return c;
    }
}
