package vn.edu.ueh.socialapplication.data.model;

public class Message {
    private String senderId;
    private String receiverId;
    private String message;
    private long timestamp;
    private boolean isRead;

    public Message() {
        // Default constructor required for calls to DataSnapshot.getValue(Message.class)
    }

    public Message(String senderId, String receiverId, String message, long timestamp, boolean isRead) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Getter that follows Java Bean convention for boolean "is" fields
    public boolean isRead() {
        return isRead;
    }

    // Setter that follows Java Bean convention
    public void setRead(boolean read) {
        isRead = read;
    }

    // Explicit getter/setter for Firebase to resolve any ambiguity with the "isRead" property
    public boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
