package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessageParserTest {

    @Test
    void parsesLoginCommandWithEmptyTarget() {
        Message message = MessageParser.parseClientMessage("LOGIN||bob");
        assertEquals("LOGIN", message.getType());
        assertEquals("", message.getTarget());
        assertEquals("bob", message.getPayload());
    }

    @Test
    void parsesTextCommandWithRoomTarget() {
        Message message = MessageParser.parseClientMessage("TEXT|general|Hej alle");
        assertEquals("TEXT", message.getType());
        assertEquals("general", message.getTarget());
        assertEquals("Hej alle", message.getPayload());
    }

    @Test
    void keepsExtraPipesInsideClientPayload() {
        Message message = MessageParser.parseClientMessage("TEXT|general|Hej|med|flere pipes");
        assertEquals("Hej|med|flere pipes", message.getPayload());
    }

    @Test
    void uppercasesAndTrimsType() {
        Message message = MessageParser.parseClientMessage("  text |general|hej");
        assertEquals("TEXT", message.getType());
    }

    @Test
    void handlesMissingFieldsWithoutThrowing() {
        Message message = MessageParser.parseClientMessage("QUIT");
        assertEquals("QUIT", message.getType());
        assertEquals("", message.getTarget());
        assertEquals("", message.getPayload());
    }

    @Test
    void parsesServerMessageWithAllFiveFields() {
        Message message = MessageParser.parseServerMessage("2026-09-16 12:00:00|TEXT|alice|general|Hej alle");
        assertEquals("2026-09-16 12:00:00", message.getTimestamp());
        assertEquals("TEXT", message.getType());
        assertEquals("alice", message.getSender());
        assertEquals("general", message.getTarget());
        assertEquals("Hej alle", message.getPayload());
    }

    @Test
    void keepsExtraPipesInsideServerPayload() {
        Message message = MessageParser.parseServerMessage("2026-09-16 12:00:00|TEXT|alice|general|Hej|med|pipes");
        assertEquals("Hej|med|pipes", message.getPayload());
    }

    @Test
    void formatsServerMessageBackToWireFormat() {
        Message message = new Message("2026-09-16 12:00:00", "PRIVATE", "alice", "bob", "Hemmelig besked");
        assertEquals("2026-09-16 12:00:00|PRIVATE|alice|bob|Hemmelig besked",
                MessageParser.formatServerMessage(message));
    }

    @Test
    void formatsClientMessageForTheWire() {
        assertEquals("TEXT|general|Hej", MessageParser.formatClientMessage("TEXT", "general", "Hej"));
    }

    @Test
    void roundTripsClientCommandThroughServerFormatting() {
        Message parsed = MessageParser.parseClientMessage("PRIVATE|bob|Hej Bob");
        Message serverMessage = new Message("PRIVATE", "alice", parsed.getTarget(), parsed.getPayload());

        String wireLine = MessageParser.formatServerMessage(serverMessage);
        Message reparsed = MessageParser.parseServerMessage(wireLine);

        assertEquals("alice", reparsed.getSender());
        assertEquals("bob", reparsed.getTarget());
        assertEquals("Hej Bob", reparsed.getPayload());
    }
}
