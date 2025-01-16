package cz.meind.database;

import cz.meind.application.Application;
import cz.meind.interfaces.Entity;
import cz.meind.service.Console;
import org.reflections.Reflections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DatabaseContext {

    public Map<Class<?>, EntityMetadata> entities = new HashMap<>();

    private Connection connection;

    public Connection getConnection() {
        try {
            if (connection == null)
                connection = DriverManager.getConnection(Application.dbUrl, Application.dbUser, Application.dbPassword);
            return connection;
        } catch (SQLException e) {
            Application.logger.error(DatabaseContext.class, e);
        }
        return null;
    }

    public void closeConnection() {
        Application.logger.info(DatabaseContext.class,"Closing connection.");

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            Application.logger.error(DatabaseContext.class, e);
        }
    }

    public DatabaseContext() {
        Application.logger.info(DatabaseContext.class, "Initializing database context.");
        Reflections reflections = new Reflections("cz.meind");
        reflections.getTypesAnnotatedWith(Entity.class).forEach(entity -> entities.put(entity, EntityParser.parseEntity(entity)));
    }
}
