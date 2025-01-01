package cz.meind.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class TestRequest {
    @Test
    public void testRequestInit() {
        new Request(new HashMap<>(), "/index.html");
    }

    @Test
    public void testRequestInitPath() {
        Request request = new Request(new HashMap<>(), "/index.html");
        Assertions.assertNotNull(request.getPath());
    }

    @Test
    public void testRequestInitHeaders() {
        HashMap<String,String> map = new HashMap<>();
        Request request = new Request(map, "/index.html");
        Assertions.assertEquals(map, request.getHeaders());
    }
}
