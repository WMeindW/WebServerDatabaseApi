package cz.meind.application;

import cz.meind.database.DatabaseContext;
import cz.meind.logger.Logger;
import cz.meind.service.Console;
import cz.meind.service.mapper.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Application {

    public static DatabaseContext database;

    public static Logger logger;

    public static String configFilePath = "src/main/resources/application.properties";

    public static String logFilePath = "log/log.txt";

    public static String dbUrl = "jdbc:mysql://localhost:3306/andrem";

    public static String dbUser = "root";

    public static String dbPassword = "password";

    public static ObjectMapper mapper;

    /**
     * Initializes and starts the application components including the logger, configuration,
     * daemon thread, and server.
     *
     * @param args Command-line arguments where the first argument can specify the path
     *             to the configuration file. If provided, it overrides the default config file path.
     */
    public static void run(String[] args) {
        initializeLogger();
        initializeConfig(args);
        initializeDatabaseProfile();
        Console.run();
    }


    private static void initializeDatabaseProfile() {
        Application.logger.info(Application.class, "Initializing database profile.");
        database = new DatabaseContext();
        Application.logger.info(Application.class, "Creating connection.");
        mapper = new ObjectMapper(Application.database.getConnection());
    }


    /**
     * Initializes the logger for the application.
     * This method sets up the logger with the specified log file path
     * and logs initial messages including a reference URL and a startup message.
     */
    private static void initializeLogger() {
        logger = new Logger(logFilePath);
        logger.message("https://github.com/WMeindW \n\n\nDaniel Linda, cz.meind.AsynchronousWebServer");
        logger.info(Application.class, "Starting application.");
    }

    /**
     * Initializes the application configuration based on the provided arguments.
     * If no arguments are provided, the default configuration file path is used.
     * The function reads the configuration properties from the file and sets the
     * corresponding application variables.
     *
     * @param args Command-line arguments where the first argument can specify the path
     *             to the configuration file. If provided, it overrides the default config file path.
     */
    private static void initializeConfig(String[] args) {
        if (args.length > 0 && args[0] != null) configFilePath = args[0];
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(configFilePath));
        } catch (IOException e) {
            Application.logger.error(Application.class, e);
        }
        try {
            logFilePath = properties.getProperty("log.file.path");
            dbUrl = properties.getProperty("database.url");
            dbUser = properties.getProperty("database.user");
            dbPassword = properties.getProperty("database.password");
            Application.logger.info(Application.class, "Found config at " + configFilePath);
            Application.logger.info(Application.class, properties.toString());
        } catch (Exception e) {
            Application.logger.error(Application.class, e);
        }
    }
}
