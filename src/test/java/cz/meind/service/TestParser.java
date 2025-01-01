package cz.meind.service;

import cz.meind.dto.Request;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;

public class TestParser {
    @Test
    public void testParseInit() {
        Assertions.assertThrows(Exception.class, () -> Parser.parseRequest(new ByteArrayInputStream(new byte[4096])));
    }

    @Test
    public void testParseInitRequest() throws IOException {
        String s = """
                GET / 200 OK
                Content-Length: 0
                Content-Type: text/html
                Host: localhost
                """;
        Request r = Parser.parseRequest(new ByteArrayInputStream(s.getBytes()));
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Length", "0");
        headers.put("Content-Type", "text/html");
        headers.put("Host", "localhost");
        Assertions.assertEquals("/", r.getPath());
        Assertions.assertEquals(headers, r.getHeaders());
    }
}
