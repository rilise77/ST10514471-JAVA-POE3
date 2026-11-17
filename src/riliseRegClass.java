
import java.util.Scanner;

public class riliseRegClass {

    // Variables
    String firstName;
    String surname;
    String username;
    String password;
    String cellphoneNumber;

    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);

        riliseRegClass user = new riliseRegClass();

        // FIRST NAME AND SURNAME
        System.out.print("Enter your first name: ");
        user.firstName = input.nextLine();

        System.out.print("Enter your surname: ");
        user.surname = input.nextLine();

        // USERNAME
        while (true) {

            System.out.print("Enter your username: ");
            user.username = input.nextLine();

            if (riliselogin.checkUserName(user.username)) {

                System.out.println("Username successfully captured.");
                break;

            } else {

                System.out.println(
                    "Username is not correctly formatted; "
                    + "please ensure that your username contains "
                    + "an underscore and is no more than five "
                    + "characters in length."
                );
            }
        }

        // PASSWORD
        while (true) {

            System.out.print("Enter your password: ");
            user.password = input.nextLine();

            if (riliselogin.checkPasswordComplexity(user.password)) {

                System.out.println("Password successfully captured.");
                break;

            } else {

                System.out.println(
                    "Password is not correctly formatted; "
                    + "please ensure that the password contains "
                    + "at least eight characters, a capital letter, "
                    + "a number, and a special character."
                );
            }
        }

        // CELLPHONE NUMBER
        while (true) {

            System.out.print("Enter your cellphone number: ");
            user.cellphoneNumber = input.nextLine();

            if (riliselogin.checkCellPhoneNumber(
                    user.cellphoneNumber)) {

                System.out.println("Cell number successfully captured.");
                break;

            } else {

                System.out.println(
                    "Cell number is incorrectly formatted or does "
                    + "not contain an international code; "
                    + "please correct the number and try again."
                );
            }
        }

        // REGISTRATION
        String registrationStatus = riliselogin.registerUser(user);

        System.out.println("\n" + registrationStatus);

        // LOGIN AND LAUNCH PART 2
        if (registrationStatus.equals("Registration was successful!")) {

            boolean loginSuccessful =
                    riliselogin.loginUser(user, input);

            if (loginSuccessful) {

                // PART 2: QUICKCHAT
                Messages quickChat = new Messages();

                System.out.println("\nStarting QuickChat...");

                quickChat.startQuickChat(input);
            }
        }

        input.close();
    }
}
