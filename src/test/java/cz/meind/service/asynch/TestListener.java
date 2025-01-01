package cz.meind.service.asynch;

import cz.meind.application.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestListener {
    @Test
    public void testListenerInit() throws InterruptedException {
        Application.port = 6666;
        Application.run(new String[0]);
        Thread.sleep(1100);
        Assertions.assertNotNull(Listener.getServer());
        Assertions.assertEquals(Listener.getServer().getLocalPort(), Application.port);
        Application.server.serverThread.interrupt();
    }
}
