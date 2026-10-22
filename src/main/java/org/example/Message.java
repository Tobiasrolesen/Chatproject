package org.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String timestamp;
    private final String type;
    private final String sender;
    private final String target;
    private final String payload;

    public Message(String type, String sender, String target, String payload) {
        this(LocalDateTime.now().format(TIMESTAMP_FORMAT), type, sender, target, payload);
    }

    public Message(String timestamp, String type, String sender, String target, String payload) {
        this.timestamp = timestamp;
        this.type = type;
        this.sender = sender;
        this.target = target;
        this.payload = payload;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getTarget() {
        return target;
    }

    public String getPayload() {
        return payload;
    }
}
