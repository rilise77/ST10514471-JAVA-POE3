
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class riliseRegClassTest {

    private riliseRegClass user;
    private Messages messages;

    @BeforeClass
    public static void setUpClass() {
        System.out.println("Starting tests...");
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("All tests completed.");
    }

    @Before
    public void setUp() {

        user = new riliseRegClass();

        user.firstName = "Rilise";
        user.surname = "Nedzi";
        user.username = "ri_1";
        user.password = "Password1!";
        user.cellphoneNumber = "+27838968976";

        messages = new Messages();
    }

    @After
    public void tearDown() {
        user = null;
        messages = null;
    }

    // =====================================
    // PART 1: REGISTRATION AND LOGIN TESTS
    // =====================================

    @Test
    public void testCheckUserNameCorrect() {
        assertTrue(riliselogin.checkUserName("ri_1"));
    }

    @Test
    public void testCheckUserNameIncorrect() {
        assertFalse(riliselogin.checkUserName("rilise"));
    }

    @Test
    public void testCheckUserNameTooLong() {
        assertFalse(riliselogin.checkUserName("rilise_1"));
    }

    @Test
    public void testCheckPasswordComplexityCorrect() {
        assertTrue(
                riliselogin.checkPasswordComplexity("Password1!")
        );
    }

    @Test
    public void testCheckPasswordComplexityIncorrect() {
        assertFalse(
                riliselogin.checkPasswordComplexity("password")
        );
    }

    @Test
    public void testCheckPasswordTooShort() {
        assertFalse(
                riliselogin.checkPasswordComplexity("Ab1!")
        );
    }

    @Test
    public void testCheckCellPhoneNumberCorrect() {
        assertTrue(
                riliselogin.checkCellPhoneNumber("+27838968976")
        );
    }

    @Test
    public void testCheckCellPhoneNumberIncorrect() {
        assertFalse(
                riliselogin.checkCellPhoneNumber("0838968976")
        );
    }

    @Test
    public void testCheckCellPhoneNumberWrongCountryCode() {
        assertFalse(
                riliselogin.checkCellPhoneNumber("+19876543210")
        );
    }

    @Test
    public void testRegisterUserSuccessful() {
        String result = riliselogin.registerUser(user);

        assertEquals("Registration was successful!", result);
    }

    @Test
    public void testRegisterUserUnsuccessful() {

        user.username = "rilise";

        String result = riliselogin.registerUser(user);

        assertEquals(
                "Username is not correctly formatted; "
                + "please ensure that your username contains "
                + "an underscore and is no more than five "
                + "characters in length.",
                result
        );
    }

    @Test
    public void testRegisterUserInvalidPassword() {

        user.password = "password";

        String result = riliselogin.registerUser(user);

        assertEquals(
                "Password is not correctly formatted; "
                + "please ensure that the password contains "
                + "at least eight characters, a capital letter, "
                + "a number, and a special character.",
                result
        );
    }

    @Test
    public void testRegisterUserInvalidCellphone() {

        user.cellphoneNumber = "0838968976";

        String result = riliselogin.registerUser(user);

        assertEquals(
                "Cell number is incorrectly formatted or does "
                + "not contain an international code; "
                + "please correct the number and try again.",
                result
        );
    }

    @Test
    public void testReturnLoginStatusSuccessful() {

        String result =
                riliselogin.returnLoginStatus(user, true);

        assertEquals(
                "Welcome Rilise Nedzi, it is great to see you again.",
                result
        );
    }

    @Test
    public void testReturnLoginStatusUnsuccessful() {

        String result =
                riliselogin.returnLoginStatus(user, false);

        assertEquals(
                "Username or password incorrect, please try again.",
                result
        );
    }

    // =====================================
    // PART 2: QUICKCHAT MESSAGE TESTS
    // =====================================

    @Test
    public void testCheckMessageIDCorrect() {

        assertTrue(
                messages.checkMessageID("1234567890")
        );
    }

    @Test
    public void testCheckMessageIDTooShort() {

        assertFalse(
                messages.checkMessageID("12345")
        );
    }

    @Test
    public void testCheckMessageIDContainsLetters() {

        assertFalse(
                messages.checkMessageID("123456789A")
        );
    }

    @Test
    public void testCheckMessageIDNull() {

        assertFalse(
                messages.checkMessageID(null)
        );
    }

    @Test
    public void testCheckRecipientCellCorrect() {

        assertTrue(
                messages.checkRecipientCell("+27838968976")
        );
    }

    @Test
    public void testCheckRecipientCellIncorrect() {

        assertFalse(
                messages.checkRecipientCell("0838968976")
        );
    }

    @Test
    public void testCheckRecipientCellNull() {

        assertFalse(
                messages.checkRecipientCell(null)
        );
    }

    @Test
    public void testCreateMessageHash() {

        String result = messages.createMessageHash(
                "1234567890",
                1,
                "Hi Mike, how are you?"
        );

        assertEquals("12:1:HIYOU?", result);
    }

    @Test
    public void testCreateMessageHashSingleWord() {

        String result = messages.createMessageHash(
                "9876543210",
                2,
                "Hello"
        );

        assertEquals("98:2:HELLOHELLO", result);
    }

    @Test
    public void testSentMessage() {

        Messages.ChatMessage message =
                new Messages.ChatMessage(
                        "1234567890",
                        "+27838968976",
                        "Hello there",
                        "12:1:HELLOTHERE",
                        1
                );

        messages.SentMessage(message);

        assertEquals(
                1,
                messages.returnTotalMessages()
        );
    }

    @Test
    public void testReturnTotalMessagesInitiallyZero() {

        assertEquals(
                0,
                messages.returnTotalMessages()
        );
    }

    @Test
    public void testReturnTotalMessagesAfterSendingTwo() {

        Messages.ChatMessage firstMessage =
                new Messages.ChatMessage(
                        "1234567890",
                        "+27838968976",
                        "Hello",
                        "12:1:HELLOHELLO",
                        1
                );

        Messages.ChatMessage secondMessage =
                new Messages.ChatMessage(
                        "1234567891",
                        "+27838968976",
                        "How are you?",
                        "12:2:HOWYOU?",
                        2
                );

        messages.SentMessage(firstMessage);
        messages.SentMessage(secondMessage);

        assertEquals(
                2,
                messages.returnTotalMessages()
        );
    }

    // =====================================
    // JSON STORAGE TESTS
    // =====================================

    @Test
    public void testStoreMessage() throws Exception {

        File jsonFile = new File("messages.json");

        boolean fileExistedBefore = jsonFile.exists();
        byte[] previousContent = null;

        if (fileExistedBefore) {
            previousContent =
                    Files.readAllBytes(jsonFile.toPath());
        }

        try {

            Messages.ChatMessage message =
                    new Messages.ChatMessage(
                            "1234567890",
                            "+27838968976",
                            "Hello from JUnit",
                            "12:1:HELLOJUNIT",
                            1
                    );

            messages.storeMessage(message);

            assertTrue(
                    "JSON file should exist",
                    jsonFile.exists()
            );

            String json = new String(
                    Files.readAllBytes(jsonFile.toPath()),
                    StandardCharsets.UTF_8
            );

            assertTrue(
                    json.contains("\"messageID\": \"1234567890\"")
            );

            assertTrue(
                    json.contains("\"recipient\": \"+27838968976\"")
            );

            assertTrue(
                    json.contains("\"message\": \"Hello from JUnit\"")
            );

            assertTrue(
                    json.contains("\"messageHash\": \"12:1:HELLOJUNIT\"")
            );

        } finally {

            if (fileExistedBefore) {
                Files.write(jsonFile.toPath(), previousContent);
            } else {
                Files.deleteIfExists(jsonFile.toPath());
            }
        }
    }

    @Test
    public void testStoreMessageWithSpecialCharacters()
            throws Exception {

        File jsonFile = new File("messages.json");

        boolean fileExistedBefore = jsonFile.exists();
        byte[] previousContent = null;

        if (fileExistedBefore) {
            previousContent =
                    Files.readAllBytes(jsonFile.toPath());
        }

        try {

            Messages.ChatMessage message =
                    new Messages.ChatMessage(
                            "1234567890",
                            "+27838968976",
                            "He said \"Hello\" and used a backslash \\",
                            "12:1:TEST",
                            1
                    );

            messages.storeMessage(message);

            String json = new String(
                    Files.readAllBytes(jsonFile.toPath()),
                    StandardCharsets.UTF_8
            );

            assertTrue(
                    "Quotes should be escaped",
                    json.contains("\\\"Hello\\\"")
            );

            assertTrue(
                    "Backslashes should be escaped",
                    json.contains("\\\\")
            );

        } finally {

            if (fileExistedBefore) {
                Files.write(jsonFile.toPath(), previousContent);
            } else {
                Files.deleteIfExists(jsonFile.toPath());
            }
        }
    }
}
