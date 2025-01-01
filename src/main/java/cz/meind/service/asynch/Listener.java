package cz.meind.service.asynch;

import cz.meind.application.Application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * This class represents a socket server listener for an asynchronous web server.
 * It provides methods to start and listen for incoming client connections.
 */
public class Listener {
    private static ServerSocket server;

    /**
     * Returns the server socket instance.
     *
     * @return the server socket
     */
    public static ServerSocket getServer() {
        return server;
    }

    /**
     * Starts the server socket on the specified port.
     * Logs a message indicating the successful start.
     * Throws a RuntimeException if an I/O error occurs.
     */
    private static void start() {
        try {
            server = new ServerSocket(Application.port);
            Application.logger.info(Listener.class, "Socket server started on port " + Application.port + ".");
        } catch (IOException e) {
            Application.logger.error(Listener.class, e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Runs the server socket listener.
     * Accepts incoming client connections and delegates handling to the server's handler.
     *
     * @throws IOException if an I/O error occurs
     */
    private static void run() throws IOException {
        start();
        while (true) {
            Socket clientSocket = server.accept();
            clientSocket.setSoTimeout(10000);
            Application.logger.info(Listener.class, "Accepted client socket");
            Application.server.getHandler().handle(clientSocket);
        }
    }

    /**
     * Listens for incoming client connections.
     * Waits for 1 second before starting the server.
     * Logs any I/O or interruption errors and exits the program with status code 130 if interrupted.
     */
    public static void listen() {
        try {
            Thread.sleep(1000);
            run();
        } catch (IOException e) {
            Application.logger.error(Listener.class, e);
        } catch (InterruptedException e) {
            Application.logger.error(Listener.class, e);
            System.exit(130);
        }
    }
}

