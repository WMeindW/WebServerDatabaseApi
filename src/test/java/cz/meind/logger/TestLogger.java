package cz.meind.logger;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestLogger {

    @Test
    public void testLoggerInit() {
        Logger logger = new Logger("C:\\Users\\danie\\IdeaProjects\\AsynchronousWebServer\\test-log.txt");
        Assertions.assertNotNull(logger);
    }

    @Test
    public void testLoggerInitFile() {
        Logger logger = new Logger("C:\\Users\\danie\\IdeaProjects\\AsynchronousWebServer\\test-log.txt");
        Assertions.assertNotNull(logger.getLogFile());
    }

    @Test
    public void testLoggerInitException() {
        Assertions.assertThrows(Exception.class, () -> {
           new Logger("log.txt");
        });
    }

    @Test
    public void testLoggerInitInfo() {
        Logger logger = new Logger("C:\\Users\\danie\\IdeaProjects\\AsynchronousWebServer\\test-log.txt");
        logger.info(TestLogger.class,"Info test");
    }

    @Test
    public void testLoggerInitError() {
        Logger logger = new Logger("C:\\Users\\danie\\IdeaProjects\\AsynchronousWebServer\\test-log.txt");
        logger.error(TestLogger.class,"Error test");
    }

    @Test
    public void testLoggerInitErrorException() {
        Logger logger = new Logger("C:\\Users\\danie\\IdeaProjects\\AsynchronousWebServer\\test-log.txt");
        logger.error(TestLogger.class,new Exception("Error test"));
    }

    @Test
    public void testLoggerInitWarn() {
        Logger logger = new Logger("C:\\Users\\danie\\IdeaProjects\\AsynchronousWebServer\\test-log.txt");
        logger.warn(TestLogger.class,"Warn test");
    }
}
