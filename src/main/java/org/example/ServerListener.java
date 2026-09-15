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
    private final String ownUsername;
    private volatile boolean expectedDisconnect = false;

    public ServerListener(BufferedReader serverReader, String ownUsername) {
        this.serverReader = serverReader;
        this.ownUsername = ownUsername;
    }

    /** Call this right before the client closes the connection on purpose (e.g. QUIT), so the
     *  read failure that follows isn't reported as an unexpected error. */
    public void expectDisconnect() {
        expectedDisconnect = true;
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = serverReader.readLine()) != null) {
                print(MessageParser.parseServerMessage(line));
            }
        } catch (IOException e) {
            if (!expectedDisconnect) {
                System.out.println("Forbindelsen til serveren blev afbrudt.");
            }
        }
    }

    private void print(Message message) {
        switch (message.getType()) {
            case "TEXT" -> System.out.printf("[%s][%s] %s: %s%n",
                    message.getTimestamp(), message.getTarget(), message.getSender(), message.getPayload());
            case "ERROR" -> System.out.printf("[%s] Fejl: %s%n", message.getTimestamp(), message.getPayload());
            case "PRIVATE" -> {
                if (message.getSender().equals(ownUsername)) {
                    System.out.printf("[%s] (privat til %s) %s%n",
                            message.getTimestamp(), message.getTarget(), message.getPayload());
                } else {
                    System.out.printf("[%s] (privat fra %s) %s%n",
                            message.getTimestamp(), message.getSender(), message.getPayload());
                }
            }
            default -> System.out.printf("[%s] %s%n", message.getTimestamp(), message.getPayload());
        }
    }
}
