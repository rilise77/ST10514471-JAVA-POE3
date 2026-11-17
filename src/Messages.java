import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;

/** Runs the QuickChat console and manages its three message categories. */
public class Messages {
    private ChatMessage[] sentMessages = new ChatMessage[0];
    private ChatMessage[] disregardedMessages = new ChatMessage[0];
    private ChatMessage[] storedMessages = new ChatMessage[0];
    private final Set<String> usedMessageIDs = new HashSet<>();
    private final SecureRandom random = new SecureRandom();

    private final MessageStore store;
    private final String sender;
    private IOException storageReadError;
    private int messageCounter = -1;

    public Messages() {
        this(Path.of("messages.json"), "");
    }

    public Messages(Path storageFile, String sender) {
        this.sender = sender;
        store = new MessageStore(storageFile);
        try {
            updateStoredMessages(store.read());
        } catch (IOException e) {
            storageReadError = e;
        }
    }

    public static class ChatMessage {
        private final String messageID;
        private final String recipient;
        private final String message;
        private final String messageHash;
        private final int messageNumber;
        private final String sender;

        public ChatMessage(String messageID, String recipient, String message,
                String messageHash, int messageNumber) {
            this(messageID, recipient, message, messageHash, messageNumber, null);
        }

        public ChatMessage(String messageID, String recipient, String message,
                String messageHash, int messageNumber, String sender) {
            this.messageID = messageID;
            this.recipient = recipient;
            this.message = message;
            this.messageHash = messageHash;
            this.messageNumber = messageNumber;
            this.sender = sender;
        }

        public String getMessageID() {
            return messageID;
        }

        public String getRecipient() {
            return recipient;
        }

        public String getMessage() {
            return message;
        }

        public String getMessageHash() {
            return messageHash;
        }

        public int getMessageNumber() {
            return messageNumber;
        }

        public String getSender() {
            return sender == null ? "" : sender;
        }
    }

    public void startQuickChat(Scanner input) {
        System.out.println("\nWelcome to QuickChat.");
        if (storageReadError != null) {
            System.out.println(storageReadError.getMessage());
        }

        while (true) {
            System.out.println("\n1. Send Messages");
            System.out.println("2. Show Recently Sent Messages");
            System.out.println("3. Quit");
            System.out.println("4. Stored Messages");
            System.out.print("Enter your choice: ");
            if (!input.hasNextLine()) {
                return;
            }

            switch (input.nextLine().trim()) {
                case "1" -> sendMessages(input);
                case "2" -> printMessages();
                case "3" -> {
                    System.out.println("Total messages sent: " + returnTotalMessages());
                    System.out.println("Thank you for using QuickChat.");
                    return;
                }
                case "4" -> showStoredMessagesMenu(input);
                default -> System.out.println("Invalid option. Please choose a number from 1 to 4.");
            }
        }
    }

    private void showStoredMessagesMenu(Scanner input) {
        while (true) {
            System.out.println("\n===== STORED MESSAGES =====");
            System.out.println("1. Show Senders and Recipients");
            System.out.println("2. Show Longest Stored Message");
            System.out.println("3. Find Message by ID");
            System.out.println("4. Find Stored Messages by Recipient");
            System.out.println("5. Delete Stored Message by Hash");
            System.out.println("6. Show Stored Messages Report");
            System.out.println("7. Find Sent and Stored Messages by Recipient");
            System.out.println("8. Back to QuickChat");
            System.out.print("Enter your choice: ");
            if (!input.hasNextLine()) {
                return;
            }

            switch (input.nextLine().trim()) {
                case "1" -> System.out.println(storedSendersAndRecipients());
                case "2" -> System.out.println(longestStoredMessage().map(this::formatDetails)
                        .orElse("No messages have been stored yet."));
                case "3" -> searchById(input);
                case "4" -> searchByRecipient(input, this::findStoredMessagesByRecipient);
                case "5" -> deleteFromMenu(input);
                case "6" -> System.out.println(storedMessagesReport());
                case "7" -> searchByRecipient(input, this::findMessagesByRecipient);
                case "8" -> {
                    return;
                }
                default -> System.out.println("Invalid option. Please choose a number from 1 to 8.");
            }
        }
    }

    private void searchById(Scanner input) {
        System.out.print("Enter the message ID: ");
        if (input.hasNextLine()) {
            Optional<ChatMessage> found = findMessageById(input.nextLine().trim());
            System.out.println(found.map(message -> "Recipient: " + message.getRecipient()
                    + "\nMessage: " + message.getMessage()).orElse("No message found."));
        }
    }

    private void searchByRecipient(Scanner input, Function<String, ChatMessage[]> search) {
        System.out.print("Enter the recipient's cellphone number: ");
        if (!input.hasNextLine()) {
            return;
        }
        ChatMessage[] matches = search.apply(input.nextLine().trim());
        if (matches.length == 0) {
            System.out.println("No messages found for that recipient.");
        }
        for (ChatMessage message : matches) {
            System.out.println("Message: " + message.getMessage());
        }
    }

    private void sendMessages(Scanner input) {
        int numberOfMessages;
        while (true) {
            System.out.print("How many messages would you like to send? ");
            if (!input.hasNextLine()) {
                return;
            }
            try {
                numberOfMessages = Integer.parseInt(input.nextLine().trim());
                if (numberOfMessages > 0) {
                    break;
                }
            } catch (NumberFormatException ignored) {
                // The same prompt handles non-numeric and non-positive input.
            }
            System.out.println("Please enter a positive whole number.");
        }

        for (int i = 0; i < numberOfMessages; i++) {
            System.out.println("\nMessage " + (i + 1) + " of " + numberOfMessages);
            if (!createMessage(input)) {
                return;
            }
        }
        System.out.println("Total messages sent: " + returnTotalMessages());
    }

    private boolean createMessage(Scanner input) {
        String recipient;
        while (true) {
            System.out.print("Enter recipient cellphone number (+27...): ");
            if (!input.hasNextLine()) {
                return false;
            }
            recipient = input.nextLine().trim();
            if (checkRecipientCell(recipient)) {
                System.out.println("Cell phone number successfully captured.");
                break;
            }
            System.out.println("Cell phone number is incorrectly formatted or does not contain an international code. "
                    + "Please correct the number and try again.");
        }

        String messageText;
        while (true) {
            System.out.print("Enter your message (maximum 250 characters): ");
            if (!input.hasNextLine()) {
                return false;
            }
            messageText = input.nextLine();
            if (messageText.isBlank()) {
                System.out.println("Message cannot be empty.");
            } else if (messageText.length() > 250) {
                System.out.println("Message exceeds 250 characters by "
                        + (messageText.length() - 250) + "; please reduce the size.");
            } else {
                System.out.println("Message ready to send.");
                break;
            }
        }

        String messageID = generateMessageID();
        int messageNumber = messageCounter + 1;
        String hash = createMessageHash(messageID, messageNumber, messageText);
        ChatMessage message = new ChatMessage(messageID, recipient, messageText, hash,
                messageNumber, sender);
        System.out.println("Message ID generated: " + messageID);

        while (true) {
            System.out.println("\n1. Send Message\n2. Disregard Message\n3. Store Message");
            System.out.println("Press 0 to delete the message.");
            System.out.print("Your choice: ");
            if (!input.hasNextLine()) {
                return false;
            }
            switch (input.nextLine().trim()) {
                case "1" -> {
                    SentMessage(message);
                    System.out.println("Message successfully sent.");
                    System.out.println(formatDetails(message));
                    return true;
                }
                case "0", "2" -> {
                    disregardMessage(message);
                    System.out.println("Message disregarded.");
                    return true;
                }
                case "3" -> {
                    storeMessage(message);
                    return true;
                }
                default -> System.out.println("Invalid option. Choose 0, 1, 2 or 3.");
            }
        }
    }

    private String generateMessageID() {
        String messageID;
        do {
            long number = random.nextLong(10_000_000_000L);
            messageID = String.format(Locale.ROOT, "%010d", number);
        } while (usedMessageIDs.contains(messageID));
        return messageID;
    }

    public boolean checkMessageID(String messageID) {
        return messageID != null && messageID.matches("[0-9]{10}");
    }

    public boolean checkRecipientCell(String recipient) {
        return riliselogin.checkCellPhoneNumber(recipient);
    }

    public String createMessageHash(String messageID, int messageNumber, String messageText) {
        String[] words = messageText.trim().split("\\s+");
        String firstWord = words[0];
        String lastWord = words[words.length - 1];
        return messageID.substring(0, 2) + ":" + messageNumber + ":"
                + (firstWord + lastWord).toUpperCase(Locale.ROOT);
    }

    private void reserveMessage(ChatMessage message) {
        usedMessageIDs.add(message.getMessageID());
        messageCounter = Math.max(messageCounter, message.getMessageNumber());
    }

    private void requireNewMessage(ChatMessage message) {
        if (!checkMessageID(message.getMessageID()) || usedMessageIDs.contains(message.getMessageID())) {
            throw new IllegalArgumentException("Expected a new 10-digit message ID.");
        }
    }

    public void SentMessage(ChatMessage message) {
        requireNewMessage(message);
        sentMessages = append(sentMessages, message);
        reserveMessage(message);
    }

    public void disregardMessage(ChatMessage message) {
        requireNewMessage(message);
        disregardedMessages = append(disregardedMessages, message);
        reserveMessage(message);
    }

    public int returnTotalMessages() {
        return sentMessages.length;
    }

    public void storeMessage(ChatMessage message) {
        requireNewMessage(message);
        try {
            MessageStore.Update update = store.update(saved -> append(saved, message));
            updateStoredMessages(update.after());
            System.out.println("Message successfully stored.");
        } catch (IOException e) {
            System.out.println("Could not save message: " + e.getMessage());
        }
    }

    private void updateStoredMessages(ChatMessage[] updated) {
        storedMessages = updated;
        for (ChatMessage message : storedMessages) {
            reserveMessage(message);
        }
        storageReadError = null;
    }

    private ChatMessage[] append(ChatMessage[] messages, ChatMessage message) {
        ChatMessage[] updated = Arrays.copyOf(messages, messages.length + 1);
        updated[messages.length] = message;
        return updated;
    }

    public ChatMessage[] getSentMessages() {
        return sentMessages.clone();
    }

    public ChatMessage[] getDisregardedMessages() {
        return disregardedMessages.clone();
    }

    public ChatMessage[] getStoredMessages() {
        return storedMessages.clone();
    }

    // IDs and hashes follow the same order as the messages, so deletion cannot
    // leave a stale entry in either array.
    public String[] getMessageHashes() {
        return messageValues(allMessages(), ChatMessage::getMessageHash);
    }

    public String[] getMessageIDs() {
        return messageValues(allMessages(), ChatMessage::getMessageID);
    }

    private String[] messageValues(ChatMessage[] messages, Function<ChatMessage, String> value) {
        return Arrays.stream(messages).map(value).toArray(String[]::new);
    }

    private ChatMessage[] mergeMessages(ChatMessage[]... groups) {
        List<ChatMessage> messages = new ArrayList<>();
        for (ChatMessage[] group : groups) {
            messages.addAll(Arrays.asList(group));
        }
        messages.sort(Comparator.comparingInt(ChatMessage::getMessageNumber));
        return messages.toArray(ChatMessage[]::new);
    }

    private ChatMessage[] allMessages() {
        return mergeMessages(sentMessages, disregardedMessages, storedMessages);
    }

    public Optional<ChatMessage> longestStoredMessage() {
        return Arrays.stream(storedMessages)
                .max(Comparator.comparingInt(message -> message.getMessage().length()));
    }

    private Optional<ChatMessage> findByValue(ChatMessage[] messages,
            Function<ChatMessage, String> field, String value) {
        int index = Arrays.asList(messageValues(messages, field)).indexOf(value);
        return index < 0 ? Optional.empty() : Optional.of(messages[index]);
    }

    public Optional<ChatMessage> findMessageById(String id) {
        return findByValue(allMessages(), ChatMessage::getMessageID, id);
    }

    public Optional<ChatMessage> findStoredMessageById(String id) {
        return findByValue(storedMessages, ChatMessage::getMessageID, id);
    }

    public ChatMessage[] findMessagesByRecipient(String recipient) {
        return matchesForRecipient(mergeMessages(sentMessages, storedMessages), recipient);
    }

    public ChatMessage[] findStoredMessagesByRecipient(String recipient) {
        return matchesForRecipient(storedMessages, recipient);
    }

    private ChatMessage[] matchesForRecipient(ChatMessage[] messages, String recipient) {
        return Arrays.stream(messages).filter(message -> message.getRecipient().equals(recipient))
                .toArray(ChatMessage[]::new);
    }

    public Optional<ChatMessage> deleteStoredMessageByHash(String hash) throws IOException {
        MessageStore.Update update = store.update(saved -> findByValue(saved, ChatMessage::getMessageHash, hash)
                .map(message -> withoutMessage(saved, message)).orElse(saved));
        updateStoredMessages(update.after());
        return findByValue(update.before(), ChatMessage::getMessageHash, hash);
    }

    private ChatMessage[] withoutMessage(ChatMessage[] messages, ChatMessage removed) {
        return Arrays.stream(messages).filter(message -> message != removed).toArray(ChatMessage[]::new);
    }

    private void deleteFromMenu(Scanner input) {
        System.out.print("Enter the message hash: ");
        if (!input.hasNextLine()) {
            return;
        }
        try {
            Optional<ChatMessage> deleted = deleteStoredMessageByHash(input.nextLine().trim());
            System.out.println(deleted.map(message -> "Message: \"" + message.getMessage()
                    + "\" successfully deleted.").orElse("No message found for that hash."));
        } catch (IOException e) {
            System.out.println("Could not delete message: " + e.getMessage());
        }
    }

    public String storedSendersAndRecipients() {
        if (storedMessages.length == 0) {
            return "No messages have been stored yet.";
        }
        List<String> rows = new ArrayList<>();
        for (ChatMessage message : storedMessages) {
            rows.add("Sender: " + displaySender(message) + " | Recipient: " + message.getRecipient());
        }
        return String.join("\n", rows);
    }

    public String storedMessagesReport() {
        return formatMessages(storedMessages, "No messages have been stored yet.");
    }

    public String sentMessagesReport() {
        return formatMessages(sentMessages, "No messages have been sent yet.");
    }

    public void printMessages() {
        System.out.println("\n===== SENT MESSAGES REPORT =====");
        System.out.println(sentMessagesReport());
        System.out.println("Total messages sent: " + returnTotalMessages());
    }

    private String formatMessages(ChatMessage[] messages, String emptyMessage) {
        if (messages.length == 0) {
            return emptyMessage;
        }
        List<String> details = new ArrayList<>();
        for (ChatMessage message : messages) {
            details.add(formatDetails(message));
        }
        return String.join("\n\n", details);
    }

    private String displaySender(ChatMessage message) {
        return message.getSender().isBlank() ? "Unknown" : message.getSender();
    }

    private String formatDetails(ChatMessage message) {
        return "Message ID: " + message.getMessageID()
                + "\nMessage Hash: " + message.getMessageHash()
                + "\nRecipient: " + message.getRecipient()
                + "\nMessage: " + message.getMessage()
                + "\nSender: " + displaySender(message)
                + "\nMessage Number: " + message.getMessageNumber();
    }
}
