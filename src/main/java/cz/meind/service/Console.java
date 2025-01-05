package cz.meind.service;

import cz.meind.application.Application;
import cz.meind.database.entities.User;

public class Console {
    public static void run(){
        ObjectMapper mapper = new ObjectMapper(Application.database.getConnection());
        System.out.println(mapper.fetchAll(User.class));
    }
}
