package org.example;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry of currently connected users.
 * Backed by ConcurrentHashMap so concurrent registrations from different
 * ClientHandler threads never corrupt the map, and putIfAbsent gives us an
 * atomic "claim this username or fail" check without extra locking.
 */
public class ClientRegistry {

    private final ConcurrentHashMap<String, ClientHandler> usersByName = new ConcurrentHashMap<>();

    /** Returns true if the username was free and is now claimed by handler. */
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
