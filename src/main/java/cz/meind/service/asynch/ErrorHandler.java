package cz.meind.service.asynch;

import cz.meind.application.Application;
import cz.meind.dto.MonitoringRecord;


import java.io.*;
import java.net.Socket;

/**
 * ErrorHandler is a subclass of Handler that handles exceptions and sends appropriate error responses.
 */
public class ErrorHandler extends Handler {

    private Socket client;

    private final Exception e;

    private String path;

    /**
     * Constructs a new ErrorHandler with the given exception and path.
     *
     * @param e     the exception to handle
     * @param path  the path of the request that caused the exception
     */
    public ErrorHandler(Exception e, String path) {
        super(-1);
        this.e = e;
        this.path = path;
    }

    /**
     * Returns the ID of the handler.
     *
     * @return the ID of the handler
     */
    public int getId() {
        return super.getId();
    }

    /**
     * Handles the error by sending an appropriate error response to the client.
     *
     * @param c the client socket
     */
    public void handle(Socket c) {
        client = c;
        run();
    }

    /**
     * Runs the error handling process.
     */
    private void run() {
        long start = System.currentTimeMillis();
        try {
            Application.logger.error(Handler.class, "Handling error: " + e);
            PrintWriter out = new PrintWriter(client.getOutputStream());
            out.println("HTTP/1.1 500 Internal Server Error");
            out.println("Content-Type: text/html; charset=UTF-8");
            out.println("Server-Name: " + Application.serverName);
            out.println("");
            out.println("<html><body>");
            out.println("<h1>Server failed with error 500!</h1>");
            out.println("<h2> " + e + "</h2>");
            out.println("</body></html>");
            Application.logger.info(Handler.class, "Handling error response: " + out);
            out.close();
            Application.monitor.addRecord(new MonitoringRecord(true, super.getId(), System.currentTimeMillis() - start, path));
            client.close();
        } catch (Exception e) {
            Application.logger.error(Handler.class, e);
        }
        close();
    }

    /**
     * Closes the handler by interrupting the current thread.
     */
    private void close() {
        Thread.currentThread().interrupt();
    }

}
