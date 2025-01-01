package cz.meind.database;

import cz.meind.application.Application;
import cz.meind.interfaces.Entity;
import org.reflections.Reflections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class DatabaseContext {

    public ArrayList<Class<?>> entities = new ArrayList<>();

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(Application.dbUrl, Application.dbUser, Application.dbPassword);
    }

    public DatabaseContext() {
        Application.logger.info(DatabaseContext.class, "Initializing database context.");
        Reflections reflections = new Reflections("cz.meind.user");
        entities.addAll(reflections.getTypesAnnotatedWith(Entity.class));
        createNonExisting();
    }

    private void createNonExisting(){

    }
}
