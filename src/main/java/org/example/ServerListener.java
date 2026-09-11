package org.example;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Runs on its own thread so the client can receive broadcasts from the
 * server at any time, while the main thread is blocked waiting for the
 * user to type the next line of keyboard input.
 */
public class ServerListener extends Thread {

    private final BufferedReader serverReader;

    public ServerListener(BufferedReader serverReader) {
        this.serverReader = serverReader;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = serverReader.readLine()) != null) {
                print(MessageParser.parseServerMessage(line));
            }
        } catch (IOException e) {
            System.out.println("Forbindelsen til serveren blev afbrudt.");
        }
    }

    private void print(Message message) {
        switch (message.getType()) {
            case "TEXT" -> System.out.printf("[%s][%s] %s: %s%n",
                    message.getTimestamp(), message.getTarget(), message.getSender(), message.getPayload());
            case "ERROR" -> System.out.printf("[%s] Fejl: %s%n", message.getTimestamp(), message.getPayload());
            default -> System.out.printf("[%s] %s%n", message.getTimestamp(), message.getPayload());
        }
    }
}
