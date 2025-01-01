package cz.meind.service.asynch;

import cz.meind.application.Application;
import cz.meind.dto.MonitoringRecord;
import cz.meind.dto.Request;
import cz.meind.dto.Response;
import cz.meind.service.Parser;


import java.io.*;
import java.net.Socket;

/**
 * This class represents a handler for incoming client connections.
 * It manages the processing of requests and responses for each client.
 */
public class Handler {

    private final int id;

    private Socket client;

    /**
     * Constructs a new Handler with the given id.
     *
     * @param id The unique identifier for the handler.
     */
    public Handler(int id) {
        this.id = id;
    }

    /**
     * Returns the unique identifier of the handler.
     *
     * @return The id of the handler.
     */
    public int getId() {
        return id;
    }

    /**
     * Handles an incoming client connection by creating a new thread to process the request.
     *
     * @param c The client socket.
     */
    public void handle(Socket c) {
        Thread thread = new Thread(this::run);
        client = c;
        Application.logger.info(Handler.class, "Dispatching thread: " + thread.getClass() + " with id " + id + " and priority " + thread.getPriority());
        thread.start();
    }

    /**
     * Processes the request and sends a response to the client.
     */
    private void run() {
        long start = System.currentTimeMillis();
        try {
            Request request = Parser.parseRequest(client.getInputStream());
            try {
                Application.logger.info(Handler.class, "Handling request: " + request);
                if (request.getPath().endsWith("/")) request.setPath(request.getPath() + "index.html");
                Response response = new Response(request.getPath(), client.getOutputStream());
                Application.logger.info(Handler.class, "Handling response: " + response);
                response.respond();
                Application.monitor.addRecord(new MonitoringRecord(false, id, System.currentTimeMillis() - start, request.getPath()));
                client.close();
            } catch (Exception e) {
                Application.logger.error(Handler.class, e);
                new ErrorHandler(e, request.getPath()).handle(client);
            }
        } catch (IOException e) {
            Application.logger.error(Handler.class, e);
            new ErrorHandler(e, "unknown").handle(client);
        }
        close();
    }

    /**
     * Closes the client connection and releases the handler.
     */
    private void close() {
        client = null;
        Application.server.releaseHandler(this);
        Thread.currentThread().interrupt();
    }

}
