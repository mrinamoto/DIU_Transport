package services;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.Notification;
import util.DatabaseConnection;

/**
 * NotificationService handles system notifications with database integration
 */
public class NotificationService {
    private final Connection connection; // Made final as suggested
    
    public NotificationService() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    /**
     * Add a new notification
     */
    public boolean addNotification(Notification notification) {
        String sql = "INSERT INTO notifications (notification_id, title, message, created_date, type, is_read) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, notification.getNotificationId());
            pstmt.setString(2, notification.getTitle());
            pstmt.setString(3, notification.getMessage());
            pstmt.setString(4, notification.getCreatedDate());
            pstmt.setString(5, notification.getType());
            // Since Notification class doesn't have isRead, default to false
            pstmt.setBoolean(6, false);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding notification: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Add a new notification with all details
     */
    public boolean addNotification(String notificationId, String title, String message, 
                                  String type) {
        String sql = "INSERT INTO notifications (notification_id, title, message, created_date, type, is_read) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, notificationId);
            pstmt.setString(2, title);
            pstmt.setString(3, message);
            
            // Use current date/time
            String currentDate = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(4, currentDate);
            
            pstmt.setString(5, type);
            pstmt.setBoolean(6, false); // Default to unread
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding notification: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all notifications
     */
    public List<Notification> getAllNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Notification notification = createNotificationFromResultSet(rs);
                notifications.add(notification);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all notifications: " + e.getMessage());
        }
        return notifications;
    }
    
    /**
     * Get unread notifications
     */
    public List<Notification> getUnreadNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE is_read = 0 ORDER BY created_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Notification notification = createNotificationFromResultSet(rs);
                notifications.add(notification);
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread notifications: " + e.getMessage());
        }
        return notifications;
    }
    
    /**
     * Get read notifications
     */
    public List<Notification> getReadNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE is_read = 1 ORDER BY created_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Notification notification = createNotificationFromResultSet(rs);
                notifications.add(notification);
            }
        } catch (SQLException e) {
            System.err.println("Error getting read notifications: " + e.getMessage());
        }
        return notifications;
    }
    
    /**
     * Get latest notifications (last 10)
     */
    public List<Notification> getLatestNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications ORDER BY created_date DESC LIMIT 10";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Notification notification = createNotificationFromResultSet(rs);
                notifications.add(notification);
            }
        } catch (SQLException e) {
            System.err.println("Error getting latest notifications: " + e.getMessage());
        }
        return notifications;
    }
    
    /**
     * Get notifications by type
     */
    public List<Notification> getNotificationsByType(String type) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE type = ? ORDER BY created_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Notification notification = createNotificationFromResultSet(rs);
                notifications.add(notification);
            }
        } catch (SQLException e) {
            System.err.println("Error getting notifications by type: " + e.getMessage());
        }
        return notifications;
    }
    
    /**
     * Get notification by ID
     */
    public Notification getNotificationById(String notificationId) {
        String sql = "SELECT * FROM notifications WHERE notification_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, notificationId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createNotificationFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting notification by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Mark notification as read
     */
    public boolean markAsRead(String notificationId) {
        String sql = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, notificationId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mark notification as unread
     */
    public boolean markAsUnread(String notificationId) {
        String sql = "UPDATE notifications SET is_read = 0 WHERE notification_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, notificationId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error marking notification as unread: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Mark all notifications as read
     */
    public boolean markAllAsRead() {
        String sql = "UPDATE notifications SET is_read = 1";
        
        try (Statement stmt = connection.createStatement()) {
            int rowsAffected = stmt.executeUpdate(sql);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error marking all notifications as read: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update notification
     */
    public boolean updateNotification(Notification notification) {
        String sql = "UPDATE notifications SET title = ?, message = ?, type = ? WHERE notification_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, notification.getTitle());
            pstmt.setString(2, notification.getMessage());
            pstmt.setString(3, notification.getType());
            pstmt.setString(4, notification.getNotificationId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating notification: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a notification
     */
    public boolean deleteNotification(String notificationId) {
        String sql = "DELETE FROM notifications WHERE notification_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, notificationId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete old notifications (older than 30 days)
     */
    public boolean deleteOldNotifications() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        String thirtyDaysAgoStr = thirtyDaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String sql = "DELETE FROM notifications WHERE DATE(created_date) < ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, thirtyDaysAgoStr);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " old notifications.");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting old notifications: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get notification count
     */
    public int getNotificationCount() {
        String sql = "SELECT COUNT(*) FROM notifications";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting notification count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get unread notification count
     */
    public int getUnreadNotificationCount() {
        String sql = "SELECT COUNT(*) FROM notifications WHERE is_read = 0";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread notification count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Check if notification ID exists
     */
    public boolean notificationIdExists(String notificationId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE notification_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, notificationId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking notification ID: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Generate next notification ID
     */
    public String generateNextNotificationId() {
        String sql = "SELECT MAX(notification_id) FROM notifications";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith("N")) {
                    try {
                        int lastNumber = Integer.parseInt(lastId.substring(1));
                        return String.format("N%03d", lastNumber + 1);
                    } catch (NumberFormatException e) {
                        // If parsing fails, start from 1
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next notification ID: " + e.getMessage());
        }
        return "N001";
    }
    
    /**
     * Get notification statistics
     */
    public java.util.Map<String, Integer> getNotificationStatistics() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        
        String sql = "SELECT type, COUNT(*) as count FROM notifications GROUP BY type";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stats.put(rs.getString("type"), rs.getInt("count"));
            }
            
            // Total notifications
            sql = "SELECT COUNT(*) as total FROM notifications";
            ResultSet totalRs = stmt.executeQuery(sql);
            if (totalRs.next()) {
                stats.put("TOTAL", totalRs.getInt("total"));
            }
            
            // Unread notifications
            sql = "SELECT COUNT(*) as unread FROM notifications WHERE is_read = 0";
            ResultSet unreadRs = stmt.executeQuery(sql);
            if (unreadRs.next()) {
                stats.put("UNREAD", unreadRs.getInt("unread"));
            }
            
            // Today's notifications
            LocalDate today = LocalDate.now();
            sql = "SELECT COUNT(*) as today FROM notifications WHERE DATE(created_date) = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, today.toString());
                ResultSet todayRs = pstmt.executeQuery();
                if (todayRs.next()) {
                    stats.put("TODAY", todayRs.getInt("today"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting notification statistics: " + e.getMessage());
        }
        return stats;
    }
    
    /**
     * Search notifications by title or message
     */
    public List<Notification> searchNotifications(String keyword) {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE title LIKE ? OR message LIKE ? ORDER BY created_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Notification notification = createNotificationFromResultSet(rs);
                notifications.add(notification);
            }
        } catch (SQLException e) {
            System.err.println("Error searching notifications: " + e.getMessage());
        }
        return notifications;
    }
    
    /**
     * Get all notification types (distinct)
     */
    public List<String> getAllNotificationTypes() {
        List<String> types = new ArrayList<>();
        String sql = "SELECT DISTINCT type FROM notifications WHERE type IS NOT NULL AND type != '' ORDER BY type";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                types.add(rs.getString("type"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting notification types: " + e.getMessage());
        }
        return types;
    }
    
    /**
     * Send notification to all users
     */
    public boolean sendNotificationToAll(String title, String message, String type) {
        String notificationId = generateNextNotificationId();
        return addNotification(notificationId, title, message, type);
    }
    
    /**
     * Send urgent notification (system alert)
     */
    public boolean sendUrgentNotification(String title, String message) {
        return sendNotificationToAll(title, message, "System Alert");
    }
    
    /**
     * Send schedule update notification
     */
    public boolean sendScheduleUpdate(String busNumber, String route, String changeDetails) {
        String title = "Schedule Update: Bus " + busNumber;
        String message = "Schedule for Bus " + busNumber + " on route " + route + " has been updated. " + changeDetails;
        return sendNotificationToAll(title, message, "Schedule Update");
    }
    
    /**
     * Send bus cancellation notification
     */
    public boolean sendBusCancellation(String busNumber, String route, String date, String reason) {
        String title = "Bus Cancellation: Bus " + busNumber;
        String message = "Bus " + busNumber + " on route " + route + " for " + date + " has been cancelled. Reason: " + reason;
        return sendNotificationToAll(title, message, "Cancellation");
    }
    
    /**
     * Helper method to create Notification object from ResultSet
     */
    private Notification createNotificationFromResultSet(ResultSet rs) throws SQLException {
        String notificationId = rs.getString("notification_id");
        String title = rs.getString("title");
        String message = rs.getString("message");
        String createdDate = rs.getString("created_date");
        String type = rs.getString("type");
        
        // Simply use the 5-parameter constructor since that's what your Notification class has
        return new Notification(notificationId, title, message, createdDate, type);
    }
}