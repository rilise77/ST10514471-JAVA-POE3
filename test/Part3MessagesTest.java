import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class Part3MessagesTest {

    private static final String SENDER = "+27838968976";
    private static final String STORED_RECIPIENT = "+27838884567";
    private static final String LATE_MESSAGE = "Where are you? You are late! I have asked you to be on time.";
    private static final String LEAVING_MESSAGE = "Ok, I am leaving without you.";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path storageFile;
    private Messages messages;
    private Messages.ChatMessage cake;
    private Messages.ChatMessage late;
    private Messages.ChatMessage gate;
    private Messages.ChatMessage dinner;
    private Messages.ChatMessage leaving;

    @Before
    public void populateMessageArrays() throws Exception {
        storageFile = folder.newFolder("quickchat").toPath().resolve("messages.json");
        messages = new Messages(storageFile, SENDER);

        cake = message(1, "+27834557896", "Did you get the cake?");
        late = message(2, STORED_RECIPIENT, LATE_MESSAGE);
        gate = message(3, "+27834484567", "Yohoooo, I am at your gate.");
        // The fixture table supplies a local number and a space before the exclamation mark.
        dinner = message(4, "0838884567", "It is dinner time !");
        leaving = message(5, STORED_RECIPIENT, LEAVING_MESSAGE);

        messages.SentMessage(cake);
        messages.storeMessage(late);
        messages.disregardMessage(gate);
        messages.SentMessage(dinner);
        messages.storeMessage(leaving);
    }

    @Test
    public void populatesAllFiveMessageArrays() {
        assertArrayEquals(new String[]{"Did you get the cake?", "It is dinner time !"},
                texts(messages.getSentMessages()));
        assertArrayEquals(new String[]{"Yohoooo, I am at your gate."},
                texts(messages.getDisregardedMessages()));
        assertArrayEquals(new String[]{LATE_MESSAGE, LEAVING_MESSAGE}, texts(messages.getStoredMessages()));

        Messages.ChatMessage[] fixtures = {cake, late, gate, dinner, leaving};
        assertArrayEquals(Arrays.stream(fixtures).map(Messages.ChatMessage::getMessageHash).toArray(String[]::new),
                messages.getMessageHashes());
        assertArrayEquals(Arrays.stream(fixtures).map(Messages.ChatMessage::getMessageID).toArray(String[]::new),
                messages.getMessageIDs());
    }

    @Test
    public void displaysLongestStoredMessage() {
        assertEquals(LATE_MESSAGE, messages.longestStoredMessage().orElseThrow().getMessage());
    }

    @Test
    public void findsTheRecipientAndTextForTheRequestedMessageId() {
        Messages.ChatMessage result = messages.findMessageById(dinner.getMessageID()).orElseThrow();

        assertEquals("0838884567", result.getRecipient());
        assertEquals("It is dinner time !", result.getMessage());
        assertEquals(LATE_MESSAGE, messages.findStoredMessageById(late.getMessageID()).orElseThrow().getMessage());
        assertFalse(messages.findStoredMessageById(dinner.getMessageID()).isPresent());
        assertFalse(messages.findMessageById("9999999999").isPresent());
    }

    @Test
    public void searchesSentAndStoredMessagesForTheRequestedRecipient() {
        assertArrayEquals(new String[]{LATE_MESSAGE, LEAVING_MESSAGE},
                texts(messages.findMessagesByRecipient(STORED_RECIPIENT)));
        assertArrayEquals(new String[]{LATE_MESSAGE, LEAVING_MESSAGE},
                texts(messages.findStoredMessagesByRecipient(STORED_RECIPIENT)));
        assertArrayEquals(new String[]{cake.getMessage()}, texts(messages.findMessagesByRecipient(cake.getRecipient())));
        assertEquals(0, messages.findStoredMessagesByRecipient(cake.getRecipient()).length);
        assertEquals(0, messages.findMessagesByRecipient(gate.getRecipient()).length);
    }

    @Test
    public void deletesMessageByHashAndKeepsTheDeletionOnDisk() throws Exception {
        assertEquals(LATE_MESSAGE, messages.deleteStoredMessageByHash(late.getMessageHash()).orElseThrow().getMessage());
        assertFalse(messages.findMessageById(late.getMessageID()).isPresent());
        assertFalse(Arrays.asList(messages.getMessageHashes()).contains(late.getMessageHash()));
        assertFalse(Arrays.asList(messages.getMessageIDs()).contains(late.getMessageID()));
        assertFalse(messages.deleteStoredMessageByHash(late.getMessageHash()).isPresent());

        Messages reloaded = new Messages(storageFile, SENDER);
        assertArrayEquals(new String[]{LEAVING_MESSAGE}, texts(reloaded.getStoredMessages()));
    }

    @Test
    public void includesEachSentMessageInTheSentReport() {
        String report = messages.sentMessagesReport();

        assertReportDetails(report, cake);
        assertReportDetails(report, dinner);
        assertFalse(report.contains(late.getMessageHash()));
        assertFalse(report.contains(gate.getMessageHash()));
    }

    @Test
    public void restoresStoredMessagesIncludingSenderAndReportDetailsFromJson() {
        Messages reloaded = new Messages(storageFile, SENDER);
        String directory = reloaded.storedSendersAndRecipients();
        String report = reloaded.storedMessagesReport();

        assertArrayEquals(new String[]{LATE_MESSAGE, LEAVING_MESSAGE}, texts(reloaded.getStoredMessages()));
        assertEquals(SENDER, reloaded.getStoredMessages()[0].getSender());
        assertEquals(SENDER, reloaded.getStoredMessages()[1].getSender());
        assertTrue(directory.contains("Sender: " + SENDER));
        assertTrue(directory.contains("Recipient: " + STORED_RECIPIENT));
        assertReportDetails(report, late);
        assertReportDetails(report, leaving);
        assertFalse(report.contains(cake.getMessageHash()));
    }

    @Test
    public void loadsAStoredMessageFromThePart2JsonFormat() throws Exception {
        Path legacyFile = storageFile.resolveSibling("legacy.json");
        Files.writeString(legacyFile, """
                [
                  {
                    "messageID": "1234567890",
                    "recipient": "+27838884567",
                    "message": "A message with an escaped quote: \\"Hi\\"",
                    "messageHash": "12:1:AHI",
                    "messageNumber": 1
                  }
                ]
                """);

        Messages legacy = new Messages(legacyFile, SENDER);
        Messages.ChatMessage message = legacy.findStoredMessageById("1234567890").orElseThrow();

        assertEquals("A message with an escaped quote: \"Hi\"", message.getMessage());
        assertEquals(STORED_RECIPIENT, message.getRecipient());
        assertEquals("12:1:AHI", message.getMessageHash());
    }

    @Test
    public void twoSessionsKeepEachOthersStoredUpdates() throws Exception {
        Messages otherSession = new Messages(storageFile, SENDER);

        messages.deleteStoredMessageByHash(late.getMessageHash());
        Messages.ChatMessage newMessage = message(6, STORED_RECIPIENT, "I am on my way.");
        otherSession.storeMessage(newMessage);

        Messages reloaded = new Messages(storageFile, SENDER);
        assertArrayEquals(new String[]{LEAVING_MESSAGE, newMessage.getMessage()},
                texts(reloaded.getStoredMessages()));
    }

    @Test
    public void leavesAMalformedArchiveIntactWhenAWriteIsAttempted() throws Exception {
        String damagedJson = "[incomplete";
        Files.writeString(storageFile, damagedJson);

        Messages reloaded = new Messages(storageFile, SENDER);
        reloaded.storeMessage(message(6, STORED_RECIPIENT, "This message could not be saved."));
        assertThrows(IOException.class, () -> reloaded.deleteStoredMessageByHash(late.getMessageHash()));

        assertEquals(damagedJson, Files.readString(storageFile));
        assertEquals(0, reloaded.getStoredMessages().length);
    }

    private Messages.ChatMessage message(int number, String recipient, String content) {
        String id = "123456789" + number;
        String hash = messages.createMessageHash(id, number, content);
        return new Messages.ChatMessage(id, recipient, content, hash, number, SENDER);
    }

    private static String[] texts(Messages.ChatMessage[] messages) {
        return Arrays.stream(messages).map(Messages.ChatMessage::getMessage).toArray(String[]::new);
    }

    private static void assertReportDetails(String report, Messages.ChatMessage message) {
        assertTrue(report.contains("Message Hash: " + message.getMessageHash()));
        assertTrue(report.contains("Recipient: " + message.getRecipient()));
        assertTrue(report.contains("Message: " + message.getMessage()));
    }
}
