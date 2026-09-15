package org.example;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry of chat rooms and their members.
 * Rooms map is a ConcurrentHashMap and each room's member set is created with
 * ConcurrentHashMap.newKeySet(), so members can join/leave/broadcast from
 * different ClientHandler threads at the same time without external locking.
 */
public class ChatRoomManager {

    public static final String DEFAULT_ROOM = "general";

    private final ConcurrentHashMap<String, Set<ClientHandler>> rooms = new ConcurrentHashMap<>();

    public void join(String room, ClientHandler handler) {
        rooms.computeIfAbsent(room, r -> ConcurrentHashMap.newKeySet()).add(handler);
    }

    /** Atomic claim-or-fail: creates the room only if it doesn't already exist. */
    public boolean create(String room) {
        return rooms.putIfAbsent(room, ConcurrentHashMap.newKeySet()) == null;
    }

    public boolean exists(String room) {
        return rooms.containsKey(room);
    }

    public void leave(String room, ClientHandler handler) {
        Set<ClientHandler> members = rooms.get(room);
        if (members != null) {
            members.remove(handler);
        }
    }

    /**
     * Sends message to every member of message's room.
     * includeSender decides whether the sender's own handler also receives it.
     * Team decision: the sender DOES get their own message echoed back, so
     * everyone in the room - including the sender - sees the same
     * server-timestamped stream of messages. ClientHandler calls this with
     * includeSender = true for TEXT broadcasts.
     */
    public void broadcast(String room, Message message, boolean includeSender) {
        Set<ClientHandler> members = rooms.get(room);
        if (members == null) {
            return;
        }

        String line = MessageParser.formatServerMessage(message);
        for (ClientHandler member : members) {
            if (!includeSender && member.getUsername().equals(message.getSender())) {
                continue;
            }
            member.send(line);
        }
    }
}
