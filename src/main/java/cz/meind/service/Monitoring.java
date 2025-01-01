package cz.meind.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.meind.application.Application;
import cz.meind.dto.MonitoringRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is responsible for monitoring the performance of the server.
 * It maintains a list of {@link MonitoringRecord} instances and provides methods to add records, clear records, and write records to a file.
 */
public class Monitoring {
    private final List<MonitoringRecord> records = Collections.synchronizedList(new LinkedList<>());

    /**
     * Constructor that initializes the monitoring service.
     * It sets up the monitoring by creating the necessary directories and files.
     * If an error occurs during setup, it logs the error using the {@link Application#logger}.
     */
    public Monitoring() {
        try {
            setup();
        } catch (IOException e) {
            Application.logger.error(Monitoring.class, e);
        }
    }

    /**
     * Private method to setup the monitoring service.
     * It creates the necessary directories and files for monitoring data.
     * If an error occurs during setup, it throws an {@link IOException}.
     * @throws IOException if an error occurs during setup
     */
    private void setup() throws IOException {
        Application.monitor = this;
        if (Files.notExists(Path.of(Application.publicFilePath + "/monitor/data.json"))) {
            Files.createDirectories(Path.of(Application.publicFilePath + "/monitor/"));
            Files.createFile(Path.of(Application.publicFilePath + "/monitor/data.json"));
        }
    }

    /**
     * Private synchronized method to clear the records.
     * It creates a copy of the current records, clears the original records, and returns the copied records.
     * @return a copy of the current records
     */
    private synchronized List<MonitoringRecord> clear() {
        List<MonitoringRecord> list = new LinkedList<>(records);
        records.clear();
        return list;
    }

    /**
     * Public synchronized method to add a monitoring record.
     * It adds the given record to the list of records if the serving time of the record is less than 500 milliseconds.
     * @param record the monitoring record to be added
     */
    public synchronized void addRecord(MonitoringRecord record) {
        if (record.servingTime() < 500) records.add(record);
    }

    /**
     * Public method to run the monitoring service.
     * It clears the records, writes the cleared records to a file using JSON serialization, and logs any errors that occur during writing.
     */
    public void run() {
        List<MonitoringRecord> list = clear();
        ObjectMapper objectMapper = new ObjectMapper();
        if (list.isEmpty()) return;
        try {
            for (MonitoringRecord record : list) {
                Files.writeString(Path.of(Application.publicFilePath + "/monitor/data.json"), objectMapper.writeValueAsString(record) + ",\n", StandardOpenOption.APPEND);
            }
        } catch (IOException e) {
            Application.logger.error(Monitoring.class, e);
        }
    }
}
