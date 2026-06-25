// Example Notification class that would work with the original code
package model;

public class Notification {
    private String notificationId;
    private String title;
    private String message;
    private String createdDate;
    private String type;
    private boolean isRead;
    
    // Constructor with read status
    public Notification(String notificationId, String title, String message, 
                       String createdDate, String type, boolean isRead) {
        this.notificationId = notificationId;
        this.title = title;
        this.message = message;
        this.createdDate = createdDate;
        this.type = type;
        this.isRead = isRead;
    }
    
    // Constructor without read status (for backward compatibility)
    public Notification(String notificationId, String title, String message, 
                       String createdDate, String type) {
        this(notificationId, title, message, createdDate, type, false);
    }
    
    // Getters and setters
    public String getNotificationId() { return notificationId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getCreatedDate() { return createdDate; }
    public String getType() { return type; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }
    public void markAsRead() { this.isRead = true; }
    public void markAsUnread() { this.isRead = false; }
}