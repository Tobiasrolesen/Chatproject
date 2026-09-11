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

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run(){

        String workerName = Thread.currentThread().getName();

        System.out.printf(
                "[%s] Begynder at betjene %s%n",
                workerName,
                clientSocket.getRemoteSocketAddress()
        );

        try (
                Socket socket = clientSocket;

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        );

                PrintWriter writer =
                        new PrintWriter(
                                new OutputStreamWriter(
                                        socket.getOutputStream(),
                                        StandardCharsets.UTF_8
                                ),
                                true
                        )
        ) {
            String username;
            while ((username = reader.readLine()) != null) {
                System.out.printf(
                        "[%s] Modtaget: %s%n",
                        workerName,
                        username
                );

                if (username.equalsIgnoreCase("QUIT")) {
                    System.out.printf(
                            "[%s] Kunden går%n",
                            workerName
                    );
                    break;
                }
                writer.println("Velkommen " + username + " til Java Beans");
                while(true){
                    String line = reader.readLine();
                    if(line.equalsIgnoreCase("QUIT")){
                        System.out.printf(
                                "[%s] Kunden går%n",
                                workerName
                        );
                        break;
                    }
                    if(line.isEmpty()){
                        System.out.println("Brugere skriver intet");
                        break;
                    }
                    writer.println(username + " Echo: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
