
import java.util.Scanner;

public class riliselogin {

    // CHECK USERNAME
    public static boolean checkUserName(String username) {

        return username != null
                && username.contains("_")
                && username.length() <= 5;
    }

    // CHECK PASSWORD COMPLEXITY
    public static boolean checkPasswordComplexity(String password) {

        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasCapital = false;
        boolean hasNumber = false;
        boolean hasSpecialCharacter = false;

        for (int i = 0; i < password.length(); i++) {

            char character = password.charAt(i);

            if (Character.isUpperCase(character)) {
                hasCapital = true;
            }

            if (Character.isDigit(character)) {
                hasNumber = true;
            }

            if (!Character.isLetterOrDigit(character)) {
                hasSpecialCharacter = true;
            }
        }

        return hasCapital
                && hasNumber
                && hasSpecialCharacter;
    }

    // CHECK SOUTH AFRICAN CELLPHONE NUMBER
    public static boolean checkCellPhoneNumber(String cellphoneNumber) {

        if (cellphoneNumber == null) {
            return false;
        }

        // Example: +27838968976
        return cellphoneNumber.matches("^\\+27[6-8][0-9]{8}$");
    }

    // REGISTER USER
    public static String registerUser(riliseRegClass user) {

        if (!checkUserName(user.username)) {

            return "Username is not correctly formatted; "
                    + "please ensure that your username contains "
                    + "an underscore and is no more than five "
                    + "characters in length.";
        }

        if (!checkPasswordComplexity(user.password)) {

            return "Password is not correctly formatted; "
                    + "please ensure that the password contains "
                    + "at least eight characters, a capital letter, "
                    + "a number, and a special character.";
        }

        if (!checkCellPhoneNumber(user.cellphoneNumber)) {

            return "Cell number is incorrectly formatted or does "
                    + "not contain an international code; "
                    + "please correct the number and try again.";
        }

        return "Registration was successful!";
    }

    // LOGIN USER
    public static boolean loginUser(
            riliseRegClass user,
            Scanner input) {

        boolean loginSuccessful = false;

        System.out.println("\nLOGIN STEP");

        // Keep asking until the correct details are entered.
        while (!loginSuccessful) {

            System.out.print("Enter your username: ");
            String enteredUsername = input.nextLine();

            System.out.print("Enter your password: ");
            String enteredPassword = input.nextLine();

            if (enteredUsername.equals(user.username)
                    && enteredPassword.equals(user.password)) {

                loginSuccessful = true;

                System.out.println(
                    "\n" + returnLoginStatus(user, true)
                );

            } else {

                System.out.println(
                    "\n" + returnLoginStatus(user, false)
                );

                System.out.println("Please try again.\n");
            }
        }

        return loginSuccessful;
    }

    // RETURN LOGIN STATUS
    public static String returnLoginStatus(
            riliseRegClass user,
            boolean loginSuccessful) {

        if (loginSuccessful) {

            return "Welcome "
                    + user.firstName
                    + " "
                    + user.surname
                    + ", it is great to see you again.";

        } else {

            return "Username or password incorrect, please try again.";
        }
    }
}
