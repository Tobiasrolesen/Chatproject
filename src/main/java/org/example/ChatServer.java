package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 5001;

    public static final int MAX_CLIENTS = 10;

    public static void main(String[] args) {
        System.out.printf(
                "Java Beans åbner på port %d med max %d kunder%n",
                PORT,
                MAX_CLIENTS
        );

        ExecutorService ThreadPool =
                Executors.newFixedThreadPool(
                        MAX_CLIENTS
                );

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Ny kunde: " + clientSocket.getRemoteSocketAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                ThreadPool.execute(clientHandler);


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
