package cz.meind.dto;

import cz.meind.application.Application;
import cz.meind.service.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestResponse {
    @Test
    public void testResponseInit() {
        new Response(new File("index.html"), new ByteArrayOutputStream());
    }

    @Test
    public void testResponseRespondCode() throws IOException {
        Application.server = new Server("test");
        Response r = new Response(new File("index.html"), new ByteArrayOutputStream());
        r.respond();
        Assertions.assertEquals("404 Not Found", r.getCode());
        Response r1 = new Response(new File("C:\\Users\\danie\\IdeaProjects\\AsynchronousWebServer\\src\\main\\resources\\public\\index.html"), new ByteArrayOutputStream());
        r1.respond();
        Assertions.assertEquals("200 OK", r1.getCode());
    }

    @Test
    public void testResponseRespondOutStream() throws IOException {
        Application.server = new Server("test");
        Response r = new Response(new File("C:\\Users\\danie\\IdeaProjects\\AsynchronousWebServer\\src\\main\\resources\\public\\index.html"), new ByteArrayOutputStream());
        r.respond();
        ByteArrayOutputStream s = (ByteArrayOutputStream) r.getOut();
        Assertions.assertTrue(s.toString(StandardCharsets.UTF_8).contains(Files.readString(Path.of("C:\\Users\\danie\\IdeaProjects\\AsynchronousWebServer\\src\\main\\resources\\public\\index.html"))));
    }
}
