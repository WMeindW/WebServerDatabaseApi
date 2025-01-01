package cz.meind.service.asynch;

import cz.meind.application.Application;
import cz.meind.service.Monitoring;

/**
 * This class represents a daemon that continuously monitors the application.
 * It implements the Runnable interface to be executed in a separate thread.
 */
public class Daemon implements Runnable {

    @Override
    public void run() {
        Monitoring monitor = new Monitoring();
        while (true) {
            try {
                Thread.sleep(1000);
                monitor.run();
            } catch (InterruptedException e) {
                Application.logger.error(Daemon.class, e);
            }
        }
    }

    /**
     * This method is used to gracefully shut down the daemon.
     * It interrupts the server thread and waits for a second to allow the server to stop properly.
     * If the server does not stop within the given time, it logs an error message and exits the application.
     */
    public static void shutdown() {
        // TODO: Implement stopping the daemon logic here
        Application.logger.info(Daemon.class, "Shutting down");
        Application.server.getServerThread().interrupt();
        Application.logger.info(Daemon.class, "Interrupted server");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Application.logger.info(Daemon.class, "Proper shutdown failed");
            System.exit(1);
        }
        Application.logger.info(Daemon.class, "Shutdown completed");
    }
}
