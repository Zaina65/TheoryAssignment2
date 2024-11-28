import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server {
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private BufferedReader input;
    private PrintWriter output;
    private Scanner scanner;

    private String[] clientMessages = new String[100];
    private String[] serverMessages = new String[100];
    private int clientMessageCount = 0;
    private int serverMessageCount = 0;

    public Server(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Server started on port " + serverSocket.getLocalPort());
        System.out.println("Waiting for a client...");

        try {
            clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            output = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);

            new Thread(this::listenForClientMessages).start();
            while (true) {
                displayMenu();
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void displayMenu() {
        System.out.println("\n===== Server Menu =====");
        System.out.println("1. Send Messages");
        System.out.println("2. Display All Messages");
        System.out.println("3. Search Messages");
        System.out.println("4. React to a message");
        System.out.println("5. Edit Message");
        System.out.println("6. Delete Message");
        System.out.println("7. Disconnect");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();
        switch (choice) {
            case 1:
                sendMessagesContinuously();
                break;
            case 2:
                displayMessages();
                break;
            case 3:
                searchMessages();
                break;
            case 4:
                addReaction();
                break;
            case 5:
                editMessage();
                break;
            case 6:
                deleteMessage();
                break;
            case 7:
                System.exit(0);
                break;
            default:
                System.out.println("Invalid choice! Please try again.");
        }
    }

    private void listenForClientMessages() {
        try {
            String message;
            while ((message = input.readLine()) != null) {
                if (message.startsWith("DELETE:")) {
                    String messageId = message.substring("DELETE:".length());
                    boolean deleted = deleteMessageFromList(clientMessages, clientMessageCount, messageId);
                    if (deleted) {
                        output.println("DELETE_SUCCESS:" + messageId);
                    } else {
                        output.println("DELETE_FAILED:" + messageId);
                    }
                } else {
                    clientMessages[clientMessageCount++] = message;
                    System.out.println("\nMessage from Client: " + message);
                }
            }
        } catch (IOException e) {
            System.out.println("Connection closed by client.");
        }
    }

    private boolean deleteMessageFromList(String[] messages, int messageCount, String messageId) {
        for (int i = 0; i < messageCount; i++) {
            if (messages[i] != null && messages[i].contains(messageId)) {
                messages[i] = null;
                return true;
            }
        }
        return false;
    }

    private void sendMessagesContinuously() {
        System.out.println("Start chatting with the client (type 0 to stop):");
        while (true) {
            System.out.print("\rYou: ");
            String input = scanner.nextLine();

            if ("0".equals(input)) {
                System.out.println("Stopped sending messages. Returning to menu...");
                return;
            }
            Message message = new Message("Server", input);
            String formattedMessage = message.toString();
            serverMessages[serverMessageCount++] = formattedMessage;
            output.println(formattedMessage);
        }
    }

    private void displayMessages() {
        System.out.println("\n=== Displaying All Messages ===");
        System.out.println("Messages Received from Client:");
        for (int i = 0; i < clientMessageCount; i++) {
            System.out.println(clientMessages[i]);
        }

        System.out.println("\nMessages Sent by Server:");
        for (int i = 0; i < serverMessageCount; i++) {
            System.out.println(serverMessages[i]);
        }
    }

    private void searchMessages() {
        System.out.println("\n=== Search Messages ===");
        System.out.println("1. Search Messages Sent by Client");
        System.out.println("2. Search Messages Received from Server");
        System.out.print("Enter your choice: ");
        int searchChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        String[] messages;
        int messageCount;

        switch (searchChoice) {
            case 1:
                messages = clientMessages;
                messageCount = clientMessageCount;
                break;
            case 2:
                messages = serverMessages;
                messageCount = serverMessageCount;
                break;
            default:
                System.out.println("Invalid choice! Returning to menu.");
                return;
        }
        System.out.print("Enter message ID or content to search: ");
        String searchQuery = scanner.nextLine();

        boolean found = false;
        for (int i = 0; i < messageCount; i++) {
            if (messages[i].contains(searchQuery)) {
                System.out.println("Found message: " + messages[i]);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No matching message found.");
        }
    }

    private void editMessage() {
        System.out.print("Enter message ID to edit: ");
        String messageID = scanner.nextLine();
        boolean edited = false;

        for (int i = 0; i < serverMessageCount; i++) {
            if (serverMessages[i] != null && serverMessages[i].contains(messageID)) {
                System.out.println("Original Message: " + serverMessages[i]);
                System.out.print("Enter new content: ");
                String newContent = scanner.nextLine();

                Message updatedMessage = new Message("Server", newContent);
                serverMessages[i] = updatedMessage.toString();
                System.out.println("Message updated successfully.");
                output.println("UPDATED_MESSAGE:" + serverMessages[i]);
                edited = true;
                break;
            }
        }
        if (!edited) {
            System.out.println("Message ID not found.");
        }
    }

    private void deleteMessage() {
        System.out.println("\n=== Delete a Received Message ===");
        System.out.print("Enter the Message ID of the message to delete: ");
        String messageID = scanner.nextLine();
        boolean deleted = false;

        for (int i = 0; i < serverMessageCount; i++) {
            if (serverMessages[i] != null && serverMessages[i].contains(messageID)) {
                System.out.println("Deleted Message: " + serverMessages[i]); // Display the message
                serverMessages[i] = null; // Remove the message
                System.out.println("Message deleted successfully.");
                output.println("DELETE_MESSAGE:" + messageID); // Notify the client
                deleted = true;
                break;
            }
        }

        if (!deleted) {
            System.out.println("Message ID not found.");
        }
    }



    private void addReaction() {
        System.out.println("\n=== Add Reaction ===");
        System.out.println("1. React to a Client's Message");
        System.out.println("2. React to a Server's Message");
        System.out.print("Enter your choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter the message ID to react to: ");
        String id = scanner.nextLine();
        System.out.print("Enter the emoji (e.g., 👍, ❤️, 😂): ");
        String reaction = scanner.nextLine();

        boolean found = false;
        String[] messages;
        int messageCount;

        switch (choice) {
            case 1:
                messages = clientMessages;
                messageCount = clientMessageCount;
                break;
            case 2:
                messages = serverMessages;
                messageCount = serverMessageCount;
                break;
            default:
                System.out.println("Invalid choice! Returning to menu.");
                return;
        }
        for (int i = 0; i < messageCount; i++) {
            if (messages[i] != null && extractMessageID(messages[i]).equals(id)) {
                messages[i] += " | Reaction: " + reaction;
                output.println("UPDATED_MESSAGE:" + messages[i]);
                System.out.println("Reaction added and sent.");
                found = true;
                break;
            }
        }if (!found) {
            System.out.println("Message ID not found.");
        }
    }

    private String extractMessageID(String message) {
        String[] parts = message.split("\\|");
        for (String part : parts) {
            if (part.trim().startsWith("Message ID:")) {
                return part.split(":")[1].trim(); // Extract the ID number
            }
        }return ""; // Return an empty string if no valid ID is found
    }

    public static void main(String[] args) {
        try {
            Server server = new Server(7777);
            server.start();
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }
}
