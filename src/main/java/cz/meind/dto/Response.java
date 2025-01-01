package cz.meind.dto;

import cz.meind.application.Application;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Represents a HTTP response to be sent to the client.
 */
public class Response {
    private final OutputStream out;

    private String path;
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
     * @param out the OutputStream to which the response will be written
     */
    public Response(String path, OutputStream out) {
        this.out = out;
        this.path = path;
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
        return "Response{" +
                "out=" + out +
                ", path='" + path + '\'' +
                ", code='" + code + '\'' +
                '}';
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
        File file = new File(Application.publicFilePath + "/" + path);

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

        } else if (Application.context.getRoute(path) != null) {
            Method method = Application.context.getRoute(path);
            String html;
            try {
                html = method.invoke(method.getDeclaringClass().getDeclaredConstructor().newInstance()).toString();
                code = "200 OK";
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException |
                     NoSuchMethodException e) {
                Application.logger.error(Response.class, e);
                html = e.toString();
                code = "500 Internal Server Error";
            }
            PrintWriter headerWriter = new PrintWriter(out, true);
            headerWriter.println("HTTP/1.1 " + code);
            headerWriter.println("Content-Type: text/plain");
            headerWriter.println("Server-Name: " + Application.serverName);
            headerWriter.println();
            headerWriter.println(html);
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
