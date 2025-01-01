package cz.meind.dto;


/**
 * Represents a record of a monitoring event for a web server.
 *
 * @param error Indicates whether the request resulted in an error.
 * @param id Unique identifier for the request.
 * @param servingTime Time taken to serve the request in milliseconds.
 * @param path The requested path on the server.
 */
public record MonitoringRecord(boolean error, int id, long servingTime, String path) {
}
