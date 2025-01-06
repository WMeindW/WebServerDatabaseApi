package cz.meind.service;

import cz.meind.database.entities.Order;
import cz.meind.database.entities.User;
import cz.meind.database.entities.UserType;

import static cz.meind.application.Application.mapper;

public class Console {
    public static void run(){
        User user = new User();
        user.setUsername("kostěj");
        user.setEmail("koště");
        user.setPassword("koš");
        user.setUserType(mapper.fetchById(UserType.class,1));
        user.setOrders(mapper.fetchAll(Order.class));
        System.out.println(mapper.save(user));
    }
}
