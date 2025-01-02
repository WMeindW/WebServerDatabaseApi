package cz.meind.database;

import cz.meind.application.Application;
import cz.meind.interfaces.Entity;
import org.reflections.Reflections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseContext {

    public Map<Class<?>, EntityMetadata> entities = new HashMap<>();

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(Application.dbUrl, Application.dbUser, Application.dbPassword);
    }

    public DatabaseContext() {
        Application.logger.info(DatabaseContext.class, "Initializing database context.");
        Reflections reflections = new Reflections("cz.meind.user");
        reflections.getTypesAnnotatedWith(Entity.class).forEach(entity -> entities.put(entity, EntityParser.parseEntity(entity)));
    }
}
