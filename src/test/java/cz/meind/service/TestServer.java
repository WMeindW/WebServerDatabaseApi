package cz.meind.service;

import cz.meind.application.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestServer {
    @Test
    public void testInit() {
        Server server = new Server();
    }

    @Test
    public void testInitDispatched() {
        Server server = new Server();
        Assertions.assertNotNull(server.getDispatched());
    }

    @Test
    public void testInitDispatchedEmpty() {
        Server server = new Server();
        Assertions.assertTrue(server.getDispatched().isEmpty());
    }

    @Test
    public void testInitPool() {
        Server server = new Server();
        Assertions.assertNotNull(server.getPool());
    }

    @Test
    public void testInitPoolNotEmpty() {
        Server server = new Server();
        Assertions.assertFalse(server.getPool().isEmpty());
    }

    @Test
    public void testInitPoolFilled() {
        Server server = new Server();
        Assertions.assertEquals(server.getPool().size(), Application.poolSize);
    }

    @Test
    public void testInitThread() {
        Server server = new Server();
        Assertions.assertNotNull(server.serverThread);
    }

    @Test
    public void testInitThreadInterrupted() {
        Server server = new Server();
        Assertions.assertFalse(server.serverThread.isInterrupted());
    }

    @Test
    public void testInitThreadRunning() {
        Server server = new Server();
        Assertions.assertEquals(Thread.State.RUNNABLE, server.serverThread.getState());
    }

    @Test
    public void testInitThreadName() {
        Server server = new Server();
        Assertions.assertEquals("server", server.serverThread.getName());
    }

    @Test
    public void testInitContentTypes() {
        Server server = new Server();
        Assertions.assertNotNull(server.contentTypes);
    }

    @Test
    public void testInitContentTypesNotEmpty() {
        Server server = new Server();
        Assertions.assertFalse(server.contentTypes.isEmpty());
    }
}
