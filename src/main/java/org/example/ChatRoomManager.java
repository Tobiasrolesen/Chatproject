package org.example;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChatRoomManager {

    public static final String DEFAULT_ROOM = "general";

    //Concurrent hashmap oprettes så flere tråde kan agere i samme map. Rooms er rummet
    private final ConcurrentHashMap<String, Set<ClientHandler>> rooms = new ConcurrentHashMap<>();

    //ComputeIfAbsent bruges til at tjekke om rummet eksitere, hvis det ikke gør opretter vi et nyt og tilføjer et nyt keySet (handler) som er den nuværende klient med (navn)
    public void join(String room, ClientHandler handler) {
        rooms.computeIfAbsent(room, r -> ConcurrentHashMap.newKeySet()).add(handler);
    }

    //Retunere true eller false, hvis true oprettes rummet hvis false sender clienthandler en error til brugeren.
    public boolean create(String room) {
        return rooms.putIfAbsent(room, ConcurrentHashMap.newKeySet()) == null;
    }

    //Tjekker om rummet eksitere
    public boolean exists(String room) {
        return rooms.containsKey(room);
    }

    //Fjerner en handler fra det nuværende rum hvis man vil forlade det.
    public void leave(String room, ClientHandler handler) {
        Set<ClientHandler> members = rooms.get(room);
        if (members != null) {
            members.remove(handler);
        }
    }

    //Vi gennemgår rooms og finder det konkrete rum som brugeren skriver fra. Vi sender derefter en besked til det konkrete keysets values.
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
