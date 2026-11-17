import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.function.UnaryOperator;

/** Reads and updates the JSON archive using the Gson API described in [1] in README.md. */
final class MessageStore {
    private final Path file;
    private final Gson json = new GsonBuilder().setPrettyPrinting()
            .disableHtmlEscaping().setStrictness(Strictness.STRICT).create();

    MessageStore(Path file) {
        this.file = file.toAbsolutePath().normalize();
    }

    Messages.ChatMessage[] read() throws IOException {
        try (Reader input = Files.newBufferedReader(file)) {
            Messages.ChatMessage[] messages = json.fromJson(input, Messages.ChatMessage[].class);
            validate(messages);
            return messages;
        } catch (NoSuchFileException e) {
            return new Messages.ChatMessage[0];
        } catch (JsonParseException e) {
            throw new IOException("Could not read stored messages from " + file, e);
        }
    }

    record Update(Messages.ChatMessage[] before, Messages.ChatMessage[] after) {
    }

    Update update(UnaryOperator<Messages.ChatMessage[]> change) throws IOException {
        // Lock a separate file so the archive itself can be replaced safely.
        Path lockFile = file.resolveSibling(file.getFileName() + ".lock");
        try (FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE,
                StandardOpenOption.WRITE); FileLock lock = channel.tryLock()) {
            if (lock == null) {
                throw new IOException("Another QuickChat session is saving messages. Try again.");
            }
            Messages.ChatMessage[] before = read();
            Messages.ChatMessage[] after = change.apply(before);
            if (before != after) {
                validate(after);
                write(after);
            }
            return new Update(before, after);
        } catch (OverlappingFileLockException e) {
            throw new IOException("Another QuickChat session is saving messages. Try again.", e);
        }
    }

    private void validate(Messages.ChatMessage[] messages) throws IOException {
        if (messages == null) {
            throw new IOException("Expected a JSON array of stored messages in " + file);
        }
        Set<String> ids = new HashSet<>();
        for (Messages.ChatMessage message : messages) {
            if (message == null || message.getMessageID() == null
                    || !message.getMessageID().matches("[0-9]{10}")
                    || message.getRecipient() == null || message.getMessage() == null
                    || message.getMessageHash() == null || message.getMessageNumber() < 0
                    || !ids.add(message.getMessageID())) {
                throw new IOException("Invalid or duplicate stored message in " + file);
            }
        }
    }

    private void write(Messages.ChatMessage[] messages) throws IOException {
        Path temporaryFile = Files.createTempFile(file.getParent(), "quickchat-", ".tmp");
        try {
            Files.writeString(temporaryFile, json.toJson(messages) + System.lineSeparator());
            try {
                Files.move(temporaryFile, file, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }
}
