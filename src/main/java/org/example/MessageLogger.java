package org.example;

import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MessageLogger implements Closeable {

    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PrintWriter writer;

    public MessageLogger(String logDirectory) {
        this.writer = openLogFile(logDirectory);
    }

    //Opretter en ny fil hvis der allerede findes en fil med den korrekte dato sætter den et _1 eller _2 efter indtil den finder et ledigt navn
    private PrintWriter openLogFile(String directory) {
        try {
            Path dir = Path.of(directory);
            Files.createDirectories(dir);

            String baseName = "chatlog_" + LocalDate.now().format(FILE_DATE_FORMAT);
            Path file = dir.resolve(baseName + ".txt");
            int suffix = 1;
            while (Files.exists(file)) {
                file = dir.resolve(baseName + "_" + suffix + ".txt");
                suffix++;
            }

            System.out.println("Logger beskeder til " + file);
            return new PrintWriter(new FileWriter(file.toFile(), true), true);
        } catch (IOException e) {
            System.out.println("Kunne ikke oprette logfil - besked-logning er deaktiveret: " + e.getMessage());
            return null;
        }
    }

    //Logger public text
    public synchronized void logText(String sender, String room, String payload) {
        log(sender, room, "public", payload);
    }

    //Logger privat text
    public synchronized void logPrivate(String sender, String receiver, String payload) {
        log(sender, "PRIVATE", receiver, payload);
    }

    //Log formatet
    private void log(String sender, String type, String receiver, String payload) {
        if (writer == null) {
            return;
        }
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        writer.println(String.join("|", timestamp, sender, type, receiver, payload));
    }

    @Override
    public synchronized void close() {
        if (writer != null) {
            writer.close();
        }
    }
}
