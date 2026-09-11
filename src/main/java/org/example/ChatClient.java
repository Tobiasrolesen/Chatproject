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

            System.out.println("Hvad er dit brugernavn?");


            System.out.println(
                    "Skriv QUIT for at gå"
            );


            String line;
            while ((line = keyboard.readLine()) != null) {
                if (line.equalsIgnoreCase("QUIT")) {
                    break;
                }

                else if (line.trim().isEmpty()) {
                    System.out.println("Indtast venligst et navn.");
                    continue;
                }

                String Username = line.trim();
                serverWriter.println(Username);

                String response = serverReader.readLine();
                System.out.printf("Server: %s%n", response);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
