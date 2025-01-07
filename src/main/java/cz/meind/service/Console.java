package cz.meind.service;

import cz.meind.database.entities.Order;

import static cz.meind.application.Application.mapper;

public class Console {
    public static void run(){
        System.out.println(mapper.fetchAll(Order.class));
    }
}
