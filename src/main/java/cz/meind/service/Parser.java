package cz.meind.service;

import cz.meind.dto.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Parser {
    /**
     * Parses an HTTP request from an input stream.
     *
     * @param in The input stream containing the HTTP request.
     * @return A Request object representing the parsed HTTP request.
     * @throws IOException If an error occurs while reading from the input stream.
     */
    public static Request parseRequest(InputStream in) throws IOException {
        String body = getBody(in).strip();
        HashMap<String, String> headers = new HashMap<>();
        for (int i = 1; i < body.split("\n").length; i++) {
            String header = body.split("\n")[i];
            headers.put(header.split(":")[0], header.split(":")[1].trim());
        }
        return new Request(headers, body.split(" ")[1].split("\\?")[0]);
    }

    /**
     * Reads the body of an HTTP request from an input stream.
     *
     * @param i The input stream containing the HTTP request.
     * @return The body of the HTTP request as a string.
     * @throws IOException If an error occurs while reading from the input stream.
     */
    private static String getBody(InputStream i) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(i, StandardCharsets.UTF_8));
        String requestLine;
        StringBuilder sb = new StringBuilder();
        while ((requestLine = in.readLine()) != null && !requestLine.isEmpty()) {
            sb.append(requestLine).append("\n");
        }
        return sb.toString();
    }
}
