import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Comparable {
    private final String sender;
    private String content;
    private final LocalDateTime timestamp;
    private final String messageID;
    private static int counter = 0;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public Message(String sender, String content) {
        this.messageID = String.format("%03d", ++counter);
        this.sender = sender;
        this.content = content;
        this.timestamp = LocalDateTime.now();

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageID() {
        return messageID;
    }

    @Override
    public String toString() {
        return String.format("Content: %s | Message ID: %s | [Time: %s]",
                content,
                messageID,
                timestamp.format(FORMATTER));
    }

    @Override
    public int compareTo(Object o) {
        int compare = 0;
        Message other = (Message) o;
        if (this.timestamp.isEqual(other.timestamp)) {
            compare = 0;
        } else if (this.timestamp.isBefore(other.timestamp)) {
            compare = -1;
        } else {
            compare = 1;
        }
        return compare;
    }
}
