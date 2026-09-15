package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ChatClient {
    private static final String HOST = "localhost";

    private static final int PORT = 5001;

    public static void main(String[] args) {

        try (
                Socket socket =
                        new Socket(HOST, PORT);

                BufferedReader serverReader =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        );

                PrintWriter serverWriter =
                        new PrintWriter(
                                new OutputStreamWriter(
                                        socket.getOutputStream(),
                                        StandardCharsets.UTF_8
                                ),
                                true
                        );

                BufferedReader keyboard =
                        new BufferedReader(
                                new InputStreamReader(
                                        System.in,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            System.out.println(
                    "Velkommen til Java Beans"
            );

            String username = login(keyboard, serverReader, serverWriter);
            if (username == null) {
                return;
            }

            ServerListener serverListener = new ServerListener(serverReader, username);
            serverListener.setDaemon(true);
            serverListener.start();

            System.out.println("Du er i rummet '" + ChatRoomManager.DEFAULT_ROOM + "'. Skriv QUIT for at gå.");
            System.out.println("Skriv /private <navn> <besked> for at sende en privat besked.");
            System.out.println("Skriv /join <rumnavn> for at skifte rum, eller /create <rumnavn> for at oprette et nyt.");

            String currentRoom = ChatRoomManager.DEFAULT_ROOM;
            String line;
            while ((line = keyboard.readLine()) != null) {
                if (line.equalsIgnoreCase("QUIT")) {
                    serverListener.expectDisconnect();
                    serverWriter.println(MessageParser.formatClientMessage("QUIT", "", ""));
                    break;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                if (line.startsWith("/private ")) {
                    String rest = line.substring("/private ".length()).trim();
                    int spaceIndex = rest.indexOf(' ');
                    if (spaceIndex <= 0 || rest.substring(spaceIndex + 1).trim().isEmpty()) {
                        System.out.println("Brug: /private <navn> <besked>");
                        continue;
                    }
                    String target = rest.substring(0, spaceIndex);
                    String payload = rest.substring(spaceIndex + 1).trim();
                    serverWriter.println(MessageParser.formatClientMessage("PRIVATE", target, payload));
                    continue;
                }

                if (line.startsWith("/join ")) {
                    String room = line.substring("/join ".length()).trim();
                    if (room.isEmpty()) {
                        System.out.println("Brug: /join <rumnavn>");
                        continue;
                    }
                    currentRoom = room;
                    serverWriter.println(MessageParser.formatClientMessage("JOIN_ROOM", room, ""));
                    continue;
                }

                if (line.startsWith("/create ")) {
                    String room = line.substring("/create ".length()).trim();
                    if (room.isEmpty()) {
                        System.out.println("Brug: /create <rumnavn>");
                        continue;
                    }
                    currentRoom = room;
                    serverWriter.println(MessageParser.formatClientMessage("CREATE_ROOM", room, ""));
                    continue;
                }

                serverWriter.println(
                        MessageParser.formatClientMessage("TEXT", currentRoom, line));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String login(BufferedReader keyboard, BufferedReader serverReader, PrintWriter serverWriter)
            throws IOException {
        while (true) {
            System.out.println("Hvad er dit brugernavn?");
            String username = keyboard.readLine();
            if (username == null) {
                return null;
            }

            username = username.trim();
            if (username.isEmpty()) {
                System.out.println("Indtast venligst et navn.");
                continue;
            }

            serverWriter.println(MessageParser.formatClientMessage("LOGIN", "", username));

            String response = serverReader.readLine();
            if (response == null) {
                System.out.println("Mistede forbindelsen til serveren.");
                return null;
            }

            Message message = MessageParser.parseServerMessage(response);
            if ("ERROR".equals(message.getType())) {
                System.out.println("Fejl: " + message.getPayload());
                continue;
            }

            System.out.println(message.getPayload());
            return username;
        }
    }
}
