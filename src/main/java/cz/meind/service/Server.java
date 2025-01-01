package cz.meind.service;

import cz.meind.application.Application;
import cz.meind.service.asynch.ErrorHandler;
import cz.meind.service.asynch.Handler;
import cz.meind.service.asynch.Listener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the server functionality.
 */
public class Server {
    public Thread serverThread;

    public ConcurrentHashMap<String, String> contentTypes = new ConcurrentHashMap<>();

    private ConcurrentHashMap<Integer, Handler> pool;

    private ConcurrentHashMap<Integer, Handler> dispatched;

    /**
     * Returns the server thread.
     *
     * @return the server thread
     */
    public Thread getServerThread() {
        return serverThread;
    }

    /**
     * Returns the map of dispatched handlers.
     *
     * @return the map of dispatched handlers
     */
    public ConcurrentHashMap<Integer, Handler> getDispatched() {
        return dispatched;
    }

    /**
     * Returns the map of available handlers.
     *
     * @return the map of available handlers
     */
    public ConcurrentHashMap<Integer, Handler> getPool() {
        return pool;
    }

    /**
     * Constructor for testing purposes.
     *
     * @param test a test string
     */
    public Server(String test) {
        System.out.println(test);
        loadMimeTypes();
    }

    /**
     * Default constructor.
     * Initializes the server thread, handler pool, and content types.
     */
    public Server() {
        createPool();
        loadMimeTypes();
        serverThread = new Thread(Listener::listen);
        serverThread.setName("server");
        serverThread.start();
    }

    /**
     * Retrieves an available handler from the pool.
     * If the pool is empty, returns an error handler.
     *
     * @return the retrieved handler
     */
    public synchronized Handler getHandler() {
        if (pool.isEmpty()) return new ErrorHandler(new IllegalStateException("Server pool depleted"),"unknown");
        Map.Entry<Integer, Handler> entry = pool.entrySet().iterator().next();
        pool.remove(entry.getKey());
        dispatched.put(entry.getKey(), entry.getValue());
        return entry.getValue();
    }

    /**
     * Releases a handler back to the pool.
     *
     * @param handler the handler to release
     */
    public synchronized void releaseHandler(Handler handler) {
        dispatched.remove(handler.getId());
        pool.put(handler.getId(), handler);
    }

    /**
     * Loads MIME types from a file into the contentTypes map.
     */
    private void loadMimeTypes() {
        try {
            String mimes = Files.readString(Path.of(Application.mimesPath));
            for (String mime : mimes.split("\n")) {
                String[] split = mime.split("=");
                contentTypes.put(split[0].trim(), split[1].trim());
            }
        } catch (IOException e) {
            Application.logger.error(Server.class, e);
        }
    }

    /**
     * Creates the handler pool.
     */
    private void createPool() {
        dispatched = new ConcurrentHashMap<>();
        pool = new ConcurrentHashMap<>();
        for (int i = 0; i < Application.poolSize; i++) {
            pool.put(i, new Handler(i));
        }
    }
}
