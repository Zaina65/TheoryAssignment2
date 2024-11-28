import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner sc;

    private String[] clientMessages = new String[100];
    private String[] serverMessages = new String[100];
    private int clientMessageCount = 0;
    private int serverMessageCount = 0;

    public Client(String serverAddress, int port) throws IOException {
        socket = new Socket(serverAddress, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        sc = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Client connected to server!");
        new Thread(this::listenForServerMessages).start();
        while (true) {
            displayMenu();
        }
    }

    private void displayMenu() {
        System.out.println("1. Send Messages");
        System.out.println("2. Display All Messages");
        System.out.println("3. Search Messages");
        System.out.println("4. React to a message");
        System.out.println("5. Edit Message");
        System.out.println("6. Delete Message");
        System.out.println("7. Disconnect");
        System.out.print("Enter your choice: ");
        int choice = sc.nextInt();
        sc.nextLine();
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

    private void listenForServerMessages() {
        try {
            String response;
            while ((response = in.readLine()) != null) {
                if (response.startsWith("DELETE_SUCCESS:")) {
                    String messageId = response.substring("DELETE_SUCCESS:".length());
                    deleteMessageFromList(clientMessages, clientMessageCount, messageId);
                    System.out.println("Message ID " + messageId + " deleted successfully.");
                } else if (response.startsWith("DELETE_FAILED:")) {
                    String messageId = response.substring("DELETE_FAILED:".length());
                    System.out.println("Failed to delete message ID " + messageId + ". It may not exist.");
                } else {
                    serverMessages[serverMessageCount++] = response;
                    System.out.println("\nMessage from Server: " + response);
                }
            }
        } catch (IOException e) {
            System.out.println("Connection closed by server.");
        }
    }

    private void deleteMessageFromList(String[] messages, int messageCount, String messageId) {
        for (int i = 0; i < messageCount; i++) {
            if (messages[i] != null && messages[i].contains(messageId)) {
                messages[i] = null;
                break;
            }
        }
    }


    private void sendMessagesContinuously() {
        System.out.println("Start chatting with the server (type 0 to stop):");
        while (true) {
            System.out.print("\rYou: ");
            String input = sc.nextLine();

            if ("0".equals(input)) {
                System.out.println("Stopped sending messages. Returning to menu...");
                return;
            }
            Message message = new Message("Client", input);
            String formattedMessage = message.toString();
            clientMessages[clientMessageCount++] = formattedMessage;
            out.println(formattedMessage);
        }
    }

    private void displayMessages() {
        System.out.println("\n====== Displaying All Messages ======");
        System.out.println("Messages Sent by Client:");
        for (int i = 0; i < clientMessageCount; i++) {
            System.out.println(clientMessages[i]);
        }
        System.out.println("\nMessages Received from Server:");
        for (int i = 0; i < serverMessageCount; i++) {
            System.out.println(serverMessages[i]);
        }
        System.out.println("========================================");
    }

    private void searchMessages() {
        System.out.println("\n=== Search Messages ===");
        System.out.println("1. Search Messages Sent by Client");
        System.out.println("2. Search Messages Received from Server");
        System.out.print("Enter your choice: ");
        int searchChoice = sc.nextInt();
        sc.nextLine();

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
        String searchQuery = sc.nextLine();
        boolean found = false;
        for (int i = 0; i < messageCount; i++) {
            if (messages[i].contains(searchQuery)) {
                System.out.println("Found message: " + messages[i]);
                found = true;
            }
        } if (!found) {
            System.out.println("No matching message found.");
        }
    }

    private void addReaction() {
        System.out.println("\n=== Add Reaction ===");
        System.out.println("1. React to a Client's Message");
        System.out.println("2. React to a Server's Message");
        System.out.print("Enter your choice: ");
        int choice = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter the message ID to react to (e.g., '001'): ");
        String id = sc.nextLine();  // The ID without the prefix
        System.out.print("Enter the emoji (e.g., 👍, ❤️, 😂): ");
        String reaction = sc.nextLine();

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
                out.println("UPDATED_MESSAGE:" + messages[i]);
                System.out.println("Reaction added and sent.");
                found = true;
                break;}
        }
        if (!found) {
            System.out.println("Message ID not found.");
        }
    }
    private String extractMessageID(String message) {

        String[] parts = message.split("\\|");
        for (String part : parts) {
            if (part.trim().startsWith("Message ID:")) {
                return part.split(":")[1].trim();
            }
        }return "";
    }

    private void editMessage() {
        System.out.println("\n=== Edit a Sent Message ===");
        System.out.print("Enter the Message ID of the message to edit: ");
        String messageId = sc.nextLine();
        System.out.print("Enter the new content: ");
        String newContent = sc.nextLine();
        boolean edited = false;
        for (int i = 0; i < clientMessageCount; i++) {
            if (clientMessages[i] != null && clientMessages[i].contains(messageId)) {
                Message updatedMessage = new Message("Client", newContent);
                clientMessages[i] = updatedMessage.toString();
                edited = true;
                break;
            }
        }
        if (edited) {

            out.println("UPDATED_MESSAGE:" + new Message("Client", newContent));
            System.out.println("Edit request sent to the server.");
        } else {
            System.out.println("Message ID not found.");
        }
    }

    private void deleteMessage() {
        System.out.println("\n=== Delete a Sent Message ===");
        System.out.print("Enter the Message ID of the message to delete: ");
        String messageId = sc.nextLine();
        boolean deleted = false;

        for (int i = 0; i < clientMessageCount; i++) {
            if (clientMessages[i] != null && clientMessages[i].contains(messageId)) {
                System.out.println("Deleted Message: " + clientMessages[i]); // Display the message
                clientMessages[i] = null; // Remove the message
                System.out.println("Message deleted successfully.");
                out.println("DELETE_MESSAGE:" + messageId); // Notify the server
                deleted = true;
                break;
            }
        }

        if (!deleted) {
            System.out.println("Message ID not found.");
        }
    }


    public static void main(String[] args) {
        try {
            System.out.print("Enter server IP address: ");
            Scanner scanner = new Scanner(System.in);
            String serverIp = scanner.nextLine();
            Client client = new Client(serverIp, 7777);
            client.start();
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }
}
