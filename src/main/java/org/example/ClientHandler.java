package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientHandler extends Thread {

    private final Socket clientSocket;
    private final ClientRegistry clientRegistry;
    private final ChatRoomManager chatRoomManager;

    private PrintWriter writer;
    private String username;
    private String currentRoom;

    public ClientHandler(Socket clientSocket, ClientRegistry clientRegistry, ChatRoomManager chatRoomManager) {
        this.clientSocket = clientSocket;
        this.clientRegistry = clientRegistry;
        this.chatRoomManager = chatRoomManager;
    }

    public String getUsername() {
        return username;
    }

    /** Called by ChatRoomManager from other clients' threads to deliver a line to this client. */
    public void send(String line) {
        writer.println(line);
    }

    @Override
    public void run() {
        String workerName = Thread.currentThread().getName();

        try (
                Socket socket = clientSocket;
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(
                        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            this.writer = out;

            if (!login(reader)) {
                return;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                Message message = MessageParser.parseClientMessage(line);

                switch (message.getType()) {
                    case "TEXT" -> handleText(message);
                    case "QUIT" -> {
                        System.out.printf("[%s] %s afbryder%n", workerName, username);
                        return;
                    }
                    default -> System.out.printf(
                            "[%s] Ukendt beskedtype fra %s: %s%n", workerName, username, line);
                }
            }
        } catch (IOException e) {
            System.out.printf("[%s] Forbindelsen til %s blev afbrudt uventet%n", workerName, username);
        } finally {
            disconnect();
        }
    }

    /**
     * Keeps reading LOGIN attempts on this connection until one succeeds.
     * A taken/invalid username only rejects that attempt (ERROR reply) - it
     * must not close the connection, otherwise the client can never retry
     * with a different name.
     */
    private boolean login(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            Message loginMessage = MessageParser.parseClientMessage(line);
            String requestedName = loginMessage.getPayload().trim();

            if (!"LOGIN".equals(loginMessage.getType()) || requestedName.isEmpty()) {
                send(MessageParser.formatServerMessage(
                        new Message("ERROR", "server", "", "Du skal logge ind med et brugernavn")));
                continue;
            }

            if (!clientRegistry.register(requestedName, this)) {
                send(MessageParser.formatServerMessage(
                        new Message("ERROR", "server", requestedName, "Brugernavnet er optaget")));
                continue;
            }

            this.username = requestedName;
            this.currentRoom = ChatRoomManager.DEFAULT_ROOM;
            chatRoomManager.join(currentRoom, this);

            send(MessageParser.formatServerMessage(
                    new Message("LOGIN_OK", "server", currentRoom, "Velkommen " + username)));

            return true;
        }
        return false;
    }

    private void handleText(Message message) {
        Message broadcastMessage = new Message("TEXT", username, currentRoom, message.getPayload());
        chatRoomManager.broadcast(currentRoom, broadcastMessage, true);
    }

    private void disconnect() {
        if (username != null) {
            chatRoomManager.leave(currentRoom, this);
            clientRegistry.unregister(username);
        }
    }
}
