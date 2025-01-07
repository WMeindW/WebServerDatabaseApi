package cz.meind.service;

import cz.meind.database.entities.Customer;
import cz.meind.database.entities.Order;
import cz.meind.database.entities.Payment;
import cz.meind.database.entities.Product;

import static cz.meind.application.Application.mapper;

public class Console {
    public static void run(){
        System.out.println(mapper.fetchAll(Order.class));
    }
}
