package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ClientHandler instances here are never started/run - they're only used as
 * distinct map values, so a bare 4-null-arg construction is safe.
 */
class ClientRegistryTest {

    @Test
    void registersAFreeUsername() {
        ClientRegistry registry = new ClientRegistry();
        ClientHandler handler = new ClientHandler(null, null, null, null);

        assertTrue(registry.register("alice", handler));
        assertSame(handler, registry.get("alice"));
    }

    @Test
    void rejectsADuplicateUsername() {
        ClientRegistry registry = new ClientRegistry();
        ClientHandler first = new ClientHandler(null, null, null, null);
        ClientHandler second = new ClientHandler(null, null, null, null);

        registry.register("bob", first);

        assertFalse(registry.register("bob", second));
        assertSame(first, registry.get("bob"));
    }

    @Test
    void freesTheUsernameAfterUnregister() {
        ClientRegistry registry = new ClientRegistry();
        ClientHandler first = new ClientHandler(null, null, null, null);
        ClientHandler second = new ClientHandler(null, null, null, null);

        registry.register("carol", first);
        registry.unregister("carol");

        assertTrue(registry.register("carol", second));
        assertSame(second, registry.get("carol"));
    }

    @Test
    void unknownUsernameReturnsNull() {
        ClientRegistry registry = new ClientRegistry();
        assertNull(registry.get("ghost"));
    }
}
