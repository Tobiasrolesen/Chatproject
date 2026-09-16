package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MessageLoggerTest {

    private static String todayFileName() {
        return "chatlog_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ".txt";
    }

    @Test
    void createsADatedLogFile(@TempDir Path tempDir) {
        try (MessageLogger logger = new MessageLogger(tempDir.toString())) {
            assertTrue(Files.exists(tempDir.resolve(todayFileName())));
        }
    }

    @Test
    void doesNotOverwriteAnExistingLogFile(@TempDir Path tempDir) throws IOException {
        Files.createFile(tempDir.resolve(todayFileName()));

        try (MessageLogger logger = new MessageLogger(tempDir.toString())) {
            assertTrue(Files.exists(tempDir.resolve(todayFileName())));
            assertTrue(Files.exists(tempDir.resolve(todayFileName().replace(".txt", "_1.txt"))));
        }
    }

    @Test
    void logsARoomMessageWithPublicAsReceiver(@TempDir Path tempDir) throws IOException {
        try (MessageLogger logger = new MessageLogger(tempDir.toString())) {
            logger.logText("alice", "general", "Hej alle");

            String[] fields = onlyLogLine(tempDir).split("\\|", 5);
            assertEquals("alice", fields[1]);
            assertEquals("general", fields[2]);
            assertEquals("public", fields[3]);
            assertEquals("Hej alle", fields[4]);
        }
    }

    @Test
    void logsAPrivateMessageWithTheRecipientAsReceiver(@TempDir Path tempDir) throws IOException {
        try (MessageLogger logger = new MessageLogger(tempDir.toString())) {
            logger.logPrivate("alice", "bob", "Hemmelig besked");

            String[] fields = onlyLogLine(tempDir).split("\\|", 5);
            assertEquals("alice", fields[1]);
            assertEquals("PRIVATE", fields[2]);
            assertEquals("bob", fields[3]);
            assertEquals("Hemmelig besked", fields[4]);
        }
    }

    private String onlyLogLine(Path tempDir) throws IOException {
        try (Stream<Path> files = Files.list(tempDir)) {
            Path logFile = files.findFirst().orElseThrow();
            List<String> lines = Files.readAllLines(logFile);
            assertEquals(1, lines.size());
            return lines.get(0);
        }
    }
}
