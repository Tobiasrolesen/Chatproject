package org.example;

/**
 * Builds and parses protocol lines.
 * Client -> server:  TYPE|TARGET|PAYLOAD
 * Server -> client:  TIMESTAMP|TYPE|SENDER|TARGET|PAYLOAD
 */
public class MessageParser {

    public static Message parseClientMessage(String line) {
        String[] fields = line.split("\\|", 3);
        String type = fields[0].trim().toUpperCase();
        String target = fields.length > 1 ? fields[1] : "";
        String payload = fields.length > 2 ? fields[2] : "";
        return new Message(type, null, target, payload);
    }

    public static Message parseServerMessage(String line) {
        String[] fields = line.split("\\|", 5);
        String timestamp = fields[0];
        String type = fields.length > 1 ? fields[1] : "";
        String sender = fields.length > 2 ? fields[2] : "";
        String target = fields.length > 3 ? fields[3] : "";
        String payload = fields.length > 4 ? fields[4] : "";
        return new Message(timestamp, type, sender, target, payload);
    }

    public static String formatServerMessage(Message message) {
        return String.join("|",
                message.getTimestamp(),
                message.getType(),
                message.getSender(),
                message.getTarget(),
                message.getPayload());
    }

    public static String formatClientMessage(String type, String target, String payload) {
        return String.join("|", type, target, payload);
    }
}
