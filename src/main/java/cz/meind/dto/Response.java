package cz.meind.dto;

import cz.meind.application.Application;

import java.io.*;

/**
 * Represents a HTTP response to be sent to the client.
 */
public class Response {
    private final File file;

    private final OutputStream out;

    private String code;

    /**
     * Returns the HTTP response code.
     *
     * @return the HTTP response code
     */
    public String getCode() {
        return code;
    }

    /**
     * Constructs a new Response object.
     *
     * @param file the file to be sent as part of the response
     * @param out  the OutputStream to which the response will be written
     */
    public Response(File file, OutputStream out) {
        this.file = file;
        this.out = out;
    }

    /**
     * Returns the OutputStream to which the response will be written.
     *
     * @return the OutputStream
     */
    public OutputStream getOut() {
        return out;
    }

    /**
     * Returns a string representation of the Response object.
     *
     * @return a string representation of the Response object
     */
    @Override
    public String toString() {
        return "Response{" + "file=" + file + ", out=" + out + '}';
    }

    /**
     * Sends the HTTP response to the client.
     * <p>
     * If the file exists and is not a directory, the response will contain the file's content with appropriate headers.
     * If the file does not exist or is a directory, a 404 Not Found response will be sent.
     *
     * @throws IOException if an I/O error occurs while sending the response
     */
    public void respond() throws IOException {
        if (file.exists() && !file.isDirectory()) {
            FileInputStream fileInputStream = new FileInputStream(file);
            PrintWriter headerWriter = new PrintWriter(out, true);
            headerWriter.println("HTTP/1.1 200 OK");
            headerWriter.println("Content-Type: " + Application.server.contentTypes.get(file.getName().split("\\.")[1]));
            headerWriter.println("Content-Length: " + file.length());
            headerWriter.println("Server-Name: " + Application.serverName);
            headerWriter.println();
            code = "200 OK";
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

        } else {
            PrintWriter headerWriter = new PrintWriter(out, true);
            headerWriter.println("HTTP/1.1 404 Not Found");
            headerWriter.println("Content-Type: text/plain");
            headerWriter.println("Server-Name: " + Application.serverName);
            headerWriter.println();
            headerWriter.println("404 Soubor nenalezen");
            code = "404 Not Found";
        }
    }
}
