package org.example;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatRoomManagerTest {

    /** Test double: records everything "sent" to it instead of writing to a real socket. */
    private static class FakeClientHandler extends ClientHandler {
        private final String username;
        final List<String> received = new ArrayList<>();

        FakeClientHandler(String username) {
            super(null, null, null, null);
            this.username = username;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public void send(String line) {
            received.add(line);
        }
    }

    @Test
    void createSucceedsOnlyOnce() {
        ChatRoomManager rooms = new ChatRoomManager();
        assertTrue(rooms.create("rum1"));
        assertFalse(rooms.create("rum1"));
    }

    @Test
    void existsReflectsCreatedRooms() {
        ChatRoomManager rooms = new ChatRoomManager();
        assertFalse(rooms.exists("rum1"));
        rooms.create("rum1");
        assertTrue(rooms.exists("rum1"));
    }

    @Test
    void broadcastDeliversOnlyToRoomMembers() {
        ChatRoomManager rooms = new ChatRoomManager();
        FakeClientHandler alice = new FakeClientHandler("alice");
        FakeClientHandler bob = new FakeClientHandler("bob");
        FakeClientHandler carol = new FakeClientHandler("carol");

        rooms.join("general", alice);
        rooms.join("general", bob);
        rooms.join("rum2", carol);

        rooms.broadcast("general", new Message("TEXT", "alice", "general", "Hej alle"), true);

        assertEquals(1, alice.received.size());
        assertEquals(1, bob.received.size());
        assertTrue(carol.received.isEmpty());
    }

    @Test
    void broadcastCanExcludeSender() {
        ChatRoomManager rooms = new ChatRoomManager();
        FakeClientHandler alice = new FakeClientHandler("alice");
        FakeClientHandler bob = new FakeClientHandler("bob");
        rooms.join("general", alice);
        rooms.join("general", bob);

        rooms.broadcast("general", new Message("TEXT", "alice", "general", "Hej"), false);

        assertTrue(alice.received.isEmpty());
        assertEquals(1, bob.received.size());
    }

    @Test
    void leaveRemovesMemberFromFutureBroadcasts() {
        ChatRoomManager rooms = new ChatRoomManager();
        FakeClientHandler alice = new FakeClientHandler("alice");
        rooms.join("general", alice);
        rooms.leave("general", alice);

        rooms.broadcast("general", new Message("TEXT", "someone", "general", "Hej"), true);

        assertTrue(alice.received.isEmpty());
    }

    @Test
    void broadcastToUnknownRoomDoesNothing() {
        ChatRoomManager rooms = new ChatRoomManager();
        assertDoesNotThrow(() ->
                rooms.broadcast("nonexistent", new Message("TEXT", "x", "nonexistent", "hej"), true));
    }
}
