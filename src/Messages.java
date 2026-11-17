
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Messages {

    private final List<ChatMessage> sentMessages = new ArrayList<>();
    private final List<ChatMessage> storedMessages = new ArrayList<>();

    private final Set<String> usedMessageIDs = new HashSet<>();
    private final SecureRandom random = new SecureRandom();

    private int messageCounter = 0;

    // Message class
    public static class ChatMessage {

        private final String messageID;
        private final String recipient;
        private final String message;
        private final String messageHash;
        private final int messageNumber;

        public ChatMessage(
                String messageID,
                String recipient,
                String message,
                String messageHash,
                int messageNumber) {

            this.messageID = messageID;
            this.recipient = recipient;
            this.message = message;
            this.messageHash = messageHash;
            this.messageNumber = messageNumber;
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
    }

    // QUICKCHAT MENU
    public void startQuickChat(Scanner input) {

        boolean running = true;

        System.out.println("\n================================");
        System.out.println("       WELCOME TO QUICKCHAT");
        System.out.println("================================");

        while (running) {

            System.out.println("\nPlease choose an option:");
            System.out.println("1. Send Messages");
            System.out.println("2. Show Recently Sent Messages");
            System.out.println("3. Quit");
            System.out.print("Enter your choice: ");

            String choice = input.nextLine().trim();

            switch (choice) {

                case "1":
                    sendMessages(input);
                    break;

                case "2":
                    printMessages();
                    break;

                case "3":
                    System.out.println(
                            "Total messages sent: "
                            + returnTotalMessages()
                    );

                    System.out.println("Thank you for using QuickChat.");
                    running = false;
                    break;

                default:
                    System.out.println(
                            "Invalid option. Please select 1, 2 or 3."
                    );
            }
        }
    }

    // SEND MULTIPLE MESSAGES
    private void sendMessages(Scanner input) {

        int numberOfMessages;

        while (true) {

            System.out.print(
                    "How many messages would you like to send? "
            );

            String enteredNumber = input.nextLine().trim();

            try {

                numberOfMessages = Integer.parseInt(enteredNumber);

                if (numberOfMessages > 0) {
                    break;
                }

                System.out.println(
                        "Please enter a number greater than zero."
                );

            } catch (NumberFormatException e) {

                System.out.println(
                        "Please enter a valid whole number."
                );
            }
        }

        for (int i = 0; i < numberOfMessages; i++) {

            System.out.println(
                    "\nMessage " + (i + 1)
                    + " of " + numberOfMessages
            );

            createMessage(input);
        }
    }

    // CREATE A MESSAGE
    private void createMessage(Scanner input) {

        String recipient;

        System.out.print(
                "Enter recipient cellphone number (+27...): "
        );

        recipient = input.nextLine().trim();

        while (!checkRecipientCell(recipient)) {

            System.out.println(
                    "Invalid cellphone number. "
                    + "Use the international format, for example "
                    + "+27838968976."
            );

            System.out.print("Enter recipient cellphone number: ");
            recipient = input.nextLine().trim();
        }

        String messageText;

        while (true) {

            System.out.print(
                    "Enter your message (maximum 250 characters): "
            );

            messageText = input.nextLine();

            if (messageText.trim().isEmpty()) {

                System.out.println(
                        "Message cannot be empty."
                );

            } else if (messageText.length() > 250) {

                System.out.println(
                        "Message exceeds 250 characters. "
                        + "Please shorten it."
                );

            } else {
                break;
            }
        }

        String messageID = generateMessageID();

        messageCounter++;

        String messageHash = createMessageHash(
                messageID,
                messageCounter,
                messageText
        );

        ChatMessage newMessage = new ChatMessage(
                messageID,
                recipient,
                messageText,
                messageHash,
                messageCounter
        );

        System.out.println("\n----- MESSAGE DETAILS -----");
        System.out.println("Message ID: " + messageID);
        System.out.println("Message Hash: " + messageHash);
        System.out.println("Recipient: " + recipient);
        System.out.println("Message: " + messageText);

        boolean choosing = true;

        while (choosing) {

            System.out.println("\nChoose an action:");
            System.out.println("1. Send Message");
            System.out.println("2. Disregard Message");
            System.out.println("3. Store Message");
            System.out.print("Your choice: ");

            String choice = input.nextLine().trim();

            switch (choice) {

                case "1":
                    SentMessage(newMessage);

                    System.out.println(
                            "Message successfully sent."
                    );

                    choosing = false;
                    break;

                case "2":
                    System.out.println(
                            "Message disregarded."
                    );

                    choosing = false;
                    break;

                case "3":
                    storeMessage(newMessage);
                    choosing = false;
                    break;

                default:
                    System.out.println(
                            "Invalid option. Choose 1, 2 or 3."
                    );
            }
        }
    }

    // GENERATE A UNIQUE 10-DIGIT MESSAGE ID
    private String generateMessageID() {

        String messageID;

        do {

            long number = Math.floorMod(
                    random.nextLong(),
                    10000000000L
            );

            messageID = String.format("%010d", number);

        } while (usedMessageIDs.contains(messageID));

        usedMessageIDs.add(messageID);

        return messageID;
    }

    // VALIDATE MESSAGE ID
    public boolean checkMessageID(String messageID) {

        return messageID != null
                && messageID.matches("\\d{10}");
    }

    // VALIDATE RECIPIENT CELLPHONE NUMBER
    public boolean checkRecipientCell(String recipient) {

        return recipient != null
                && recipient.matches("^\\+27[6-8][0-9]{8}$");
    }

    // CREATE MESSAGE HASH
    public String createMessageHash(
            String messageID,
            int messageNumber,
            String messageText) {

        String[] words = messageText.trim().split("\\s+");

        String firstWord = words[0];
        String lastWord = words[words.length - 1];

        return messageID.substring(0, 2)
                + ":" + messageNumber
                + ":" + firstWord.toUpperCase()
                + lastWord.toUpperCase();
    }

    // RECORD A SENT MESSAGE
    public void SentMessage(ChatMessage message) {

        sentMessages.add(message);
    }

    // DISPLAY SENT MESSAGES
    public void printMessages() {

        if (sentMessages.isEmpty()) {

            System.out.println(
                    "No messages have been sent yet."
            );

            return;
        }

        System.out.println("\n===== RECENTLY SENT MESSAGES =====");

        for (ChatMessage message : sentMessages) {

            System.out.println("-------------------------------");
            System.out.println(
                    "Message ID: " + message.getMessageID()
            );
            System.out.println(
                    "Message Hash: " + message.getMessageHash()
            );
            System.out.println(
                    "Recipient: " + message.getRecipient()
            );
            System.out.println(
                    "Message: " + message.getMessage()
            );
        }

        System.out.println("-------------------------------");
        System.out.println(
                "Total messages sent: " + returnTotalMessages()
        );
    }

    // RETURN TOTAL SENT MESSAGES
    public int returnTotalMessages() {

        return sentMessages.size();
    }

    // STORE MESSAGE IN JSON
    public void storeMessage(ChatMessage message) {

        storedMessages.add(message);

        try {

            saveMessagesToJSON();

            System.out.println(
                    "Message successfully stored in messages.json."
            );

        } catch (IOException e) {

            storedMessages.remove(message);

            System.out.println(
                    "Could not save message: " + e.getMessage()
            );
        }
    }

    // WRITE STORED MESSAGES TO JSON FILE
    private void saveMessagesToJSON() throws IOException {

        StringBuilder json = new StringBuilder();

        json.append("[\n");

        for (int i = 0; i < storedMessages.size(); i++) {

            ChatMessage message = storedMessages.get(i);

            json.append("  {\n");

            json.append("    \"messageID\": \"")
                    .append(escapeJSON(message.getMessageID()))
                    .append("\",\n");

            json.append("    \"messageNumber\": ")
                    .append(message.getMessageNumber())
                    .append(",\n");

            json.append("    \"recipient\": \"")
                    .append(escapeJSON(message.getRecipient()))
                    .append("\",\n");

            json.append("    \"message\": \"")
                    .append(escapeJSON(message.getMessage()))
                    .append("\",\n");

            json.append("    \"messageHash\": \"")
                    .append(escapeJSON(message.getMessageHash()))
                    .append("\"\n");

            json.append("  }");

            if (i < storedMessages.size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("]\n");

        Files.write(
                Paths.get("messages.json"),
                json.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    // ESCAPE JSON SPECIAL CHARACTERS
    private String escapeJSON(String value) {

        StringBuilder escaped = new StringBuilder();

        for (int i = 0; i < value.length(); i++) {

            char c = value.charAt(i);

            switch (c) {

                case '"':
                    escaped.append("\\\"");
                    break;

                case '\\':
                    escaped.append("\\\\");
                    break;

                case '\b':
                    escaped.append("\\b");
                    break;

                case '\f':
                    escaped.append("\\f");
                    break;

                case '\n':
                    escaped.append("\\n");
                    break;

                case '\r':
                    escaped.append("\\r");
                    break;

                case '\t':
                    escaped.append("\\t");
                    break;

                default:

                    if (c < 0x20) {

                        escaped.append(
                                String.format("\\u%04x", (int) c)
                        );

                    } else {

                        escaped.append(c);
                    }

                    break;
            }
        }

        return escaped.toString();
    }
}
