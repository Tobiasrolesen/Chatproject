package org.example;

import java.util.concurrent.ConcurrentHashMap;

public class ClientRegistry {

    private final ConcurrentHashMap<String, ClientHandler> usersByName = new ConcurrentHashMap<>();

    public boolean register(String username, ClientHandler handler) {
        return usersByName.putIfAbsent(username, handler) == null;
    }

    public void unregister(String username) {
        usersByName.remove(username);
    }

    public ClientHandler get(String username) {
        return usersByName.get(username);
    }
}
