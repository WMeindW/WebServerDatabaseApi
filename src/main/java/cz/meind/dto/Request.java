package cz.meind.dto;

import java.util.HashMap;

/**
 * Represents an HTTP request.
 */
public class Request {
    private HashMap<String,String> headers;

    private String path;

    /**
     * Constructs a new Request object.
     *
     * @param headers A HashMap containing the request headers.
     * @param path    The request path.
     */
    public Request(HashMap<String,String> headers, String path) {
        this.headers = headers;
        this.path = path;
    }

    /**
     * Returns the headers of the request.
     *
     * @return A HashMap containing the request headers.
     */
    public HashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * Returns the path of the request.
     *
     * @return The request path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns a string representation of the Request object.
     *
     * @return A string representation of the Request object.
     */
    @Override
    public String toString() {
        return "Request{" + ", headers=" + headers + ", path='" + path + '\'' + '}';
    }

    /**
     * Sets the path of the request.
     *
     * @param s The new request path.
     */
    public void setPath(String s) {
        this.path = s;
    }
}
