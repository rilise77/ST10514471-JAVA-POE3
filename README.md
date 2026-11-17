# QuickChat Part 3

QuickChat is a Java console application for PROG5121. The project continues the supplied registration, login, and messaging application from Parts 1 and 2.

## Build and run

Use JDK 17 or newer and Apache Ant. From the project folder, run:

```text
ant clean test jar
java -jar dist/QuickChat.jar
```

You can also open the folder as an existing Java with Ant project in NetBeans. The first build downloads Gson, JUnit, and Hamcrest. Keep the generated `dist/lib` folder beside `QuickChat.jar` when moving the application.

## Use QuickChat

Register and sign in. Choose **1. Send Messages** to send, disregard, or store each message you enter. Choose **2. Show Recently Sent Messages** for the current session's sent report.

Choose **4. Stored Messages** to view senders and recipients, find the longest saved message, search by ID or recipient, delete a saved message by hash, and view the stored report. The ID search includes recorded messages from every category. The submenu also offers a recipient search across sent and stored messages.

Stored messages are saved to `messages.json` in the current working directory and loaded when QuickChat starts. A Part 2 JSON archive can be placed there to continue using it. Its entries display **Unknown** as the sender if the original record did not contain a sender. Sent and disregarded messages are kept for the current session. Sending is simulated in the console.

## Tests

`ant test` runs the registration and login tests, the original messaging tests, and the Part 3 tests. The Part 3 tests use the five assignment fixtures and temporary archives. GitHub Actions runs the same suite on pushes and pull requests.

The fixture table supplies the local number `0838884567` and the text `It is dinner time !`. The unit tests inject that developer fixture directly. The console retains the international `+27` validation and preserves the text entered.

## Walkthrough

1. Register a demo account and sign in. Send one message and open the sent report.
2. Store two messages for the same recipient and disregard another. Open **Stored Messages**, show the report and longest message, and search by ID and recipient.
3. Copy a stored message's hash from the report, delete that message using the hash, and restart QuickChat to show that the deletion persisted.
4. Explain how the five arrays are formed and how `MessageStore` reads and writes the JSON archive. Run `ant test` and explain what the fixtures verify.

## Source and assistance

[1] Google, “Gson User Guide,” Array Examples. [Online]. Available: https://google.github.io/gson/UserGuide.html#array-examples

OpenAI Codex assisted with implementing and testing Part 3.
