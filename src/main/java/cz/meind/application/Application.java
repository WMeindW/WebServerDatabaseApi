package cz.meind.application;

import cz.meind.logger.Logger;
import cz.meind.service.Monitoring;
import cz.meind.service.Server;
import cz.meind.service.asynch.Daemon;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class Application {
    public static Logger logger;

    public static Server server;

    public static Monitoring monitor;

    public static Thread daemonThread;

    public static String configFilePath = "src/main/resources/application.properties";

    public static String logFilePath = "log/log.txt";

    public static int port = 8088;

    public static int poolSize = 16;

    public static List<String> defaultHeaders;

    public static String publicFilePath = "src/main/resources/public";

    public static String serverName = "thread-test";

    public static String mimesPath = "src/main/resources/mimes.properties";

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
        initializeDaemon();
        initializeServer();
    }

        /**
     * Initializes the daemon thread for the application.
     * This method sets up a shutdown hook to ensure the daemon
     * is properly shut down when the application exits. It then
     * creates and starts a new daemon thread.
     */
    private static void initializeDaemon() {
        Runtime.getRuntime().addShutdownHook(new Thread(Daemon::shutdown));
        daemonThread = new Thread(new Daemon());
        daemonThread.setDaemon(true);
        daemonThread.start();
        Application.logger.info(Daemon.class, "Starting daemon.");
    }

        /**
     * Initializes the server component of the application.
     * This method logs the start of the server initialization process
     * and creates a new instance of the Server class.
     */
    private static void initializeServer() {
        Application.logger.info(Server.class, "Starting server.");
        server = new Server();
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
        port = Integer.parseInt(properties.getProperty("server.port"));
        poolSize = Integer.parseInt(properties.getProperty("server.thread.pool.size"));
        defaultHeaders = List.of(properties.getProperty("server.default.headers").split(", "));
        publicFilePath = properties.getProperty("server.public.file.path");
        serverName = properties.getProperty("server.name");
        mimesPath = properties.getProperty("server.mimes.path");
        Application.logger.info(Application.class,"Found config at " + configFilePath);
        Application.logger.info(Application.class,properties.toString());
    } catch (Exception e) {
        Application.logger.error(Application.class, e);
    }
}
}
