package vn.edu.ueh.socialapplication.chat;

import vn.edu.ueh.socialapplication.data.model.User;

public class ChatContact {
    private User user;
    private String lastMessage;
    private long unreadCount;

    public ChatContact(User user, String lastMessage, long unreadCount) {
        this.user = user;
        this.lastMessage = lastMessage;
        this.unreadCount = unreadCount;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }
}
