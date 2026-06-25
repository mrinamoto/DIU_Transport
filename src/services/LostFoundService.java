package services;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.LostFound;
import util.DatabaseConnection;

/**
 * LostFoundService handles lost and found items with database integration
 */
public class LostFoundService {
    private final Connection connection; // Made final as suggested
    
    public LostFoundService() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    /**
     * Report a lost/found item
     */
    public boolean reportItem(LostFound item) {
        String sql = "INSERT INTO lost_found (item_id, item_name, description, found_location, found_date, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getItemId());
            pstmt.setString(2, item.getItemName());
            pstmt.setString(3, item.getDescription());
            pstmt.setString(4, item.getFoundLocation());
            pstmt.setString(5, item.getFoundDate());
            pstmt.setString(6, item.getStatus());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error reporting item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Report an item with all details
     */
    public boolean reportItem(String itemId, String itemName, String description, 
                             String foundLocation, String foundDate, String status) {
        String sql = "INSERT INTO lost_found (item_id, item_name, description, found_location, found_date, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            pstmt.setString(2, itemName);
            pstmt.setString(3, description);
            pstmt.setString(4, foundLocation);
            pstmt.setString(5, foundDate);
            pstmt.setString(6, status);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error reporting item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all lost and found items
     */
    public List<LostFound> getAllItems() {
        List<LostFound> items = new ArrayList<>();
        String sql = "SELECT * FROM lost_found ORDER BY found_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                LostFound item = createLostFoundFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all items: " + e.getMessage());
        }
        return items;
    }
    
    /**
     * Get unclaimed items only
     */
    public List<LostFound> getUnclaimedItems() {
        List<LostFound> items = new ArrayList<>();
        String sql = "SELECT * FROM lost_found WHERE status = 'Unclaimed' ORDER BY found_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                LostFound item = createLostFoundFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting unclaimed items: " + e.getMessage());
        }
        return items;
    }
    
    /**
     * Get claimed items
     */
    public List<LostFound> getClaimedItems() {
        List<LostFound> items = new ArrayList<>();
        String sql = "SELECT * FROM lost_found WHERE status = 'Claimed' ORDER BY found_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                LostFound item = createLostFoundFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting claimed items: " + e.getMessage());
        }
        return items;
    }
    
    /**
     * Get item by ID
     */
    public LostFound getItemById(String itemId) {
        String sql = "SELECT * FROM lost_found WHERE item_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createLostFoundFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting item by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Update item status (when claimed)
     */
    public boolean claimItem(String itemId, String claimantName, String contactInfo) {
        String sql = "UPDATE lost_found SET status = 'Claimed', claimed_by = ?, contact_info = ? WHERE item_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, claimantName);
            pstmt.setString(2, contactInfo);
            pstmt.setString(3, itemId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error claiming item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update item information
     */
    public boolean updateItem(LostFound item) {
        String sql = "UPDATE lost_found SET item_name = ?, description = ?, found_location = ?, " +
                    "found_date = ?, status = ? WHERE item_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getItemName());
            pstmt.setString(2, item.getDescription());
            pstmt.setString(3, item.getFoundLocation());
            pstmt.setString(4, item.getFoundDate());
            pstmt.setString(5, item.getStatus());
            pstmt.setString(6, item.getItemId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update item with claim information
     */
    public boolean updateItemWithClaim(String itemId, String status, String claimedBy, String contactInfo) {
        String sql = "UPDATE lost_found SET status = ?, claimed_by = ?, contact_info = ? WHERE item_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, claimedBy);
            pstmt.setString(3, contactInfo);
            pstmt.setString(4, itemId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating item with claim: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete an item
     */
    public boolean deleteItem(String itemId) {
        String sql = "DELETE FROM lost_found WHERE item_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting item: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get items found in the last 7 days
     */
    public List<LostFound> getRecentItems() {
        List<LostFound> items = new ArrayList<>();
        LocalDate weekAgo = LocalDate.now().minusDays(7);
        String sql = "SELECT * FROM lost_found WHERE found_date >= ? ORDER BY found_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, weekAgo.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LostFound item = createLostFoundFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting recent items: " + e.getMessage());
        }
        return items;
    }
    
    /**
     * Get old items (found more than 30 days ago)
     */
    public List<LostFound> getOldItems() {
        List<LostFound> items = new ArrayList<>();
        LocalDate monthAgo = LocalDate.now().minusDays(30);
        String sql = "SELECT * FROM lost_found WHERE found_date < ? ORDER BY found_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, monthAgo.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LostFound item = createLostFoundFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting old items: " + e.getMessage());
        }
        return items;
    }
    
    /**
     * Search items by name or description
     */
    public List<LostFound> searchItems(String keyword) {
        List<LostFound> items = new ArrayList<>();
        String sql = "SELECT * FROM lost_found WHERE item_name LIKE ? OR description LIKE ? ORDER BY found_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LostFound item = createLostFoundFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error searching items: " + e.getMessage());
        }
        return items;
    }
    
    /**
     * Search items by location
     */
    public List<LostFound> searchItemsByLocation(String location) {
        List<LostFound> items = new ArrayList<>();
        String sql = "SELECT * FROM lost_found WHERE found_location LIKE ? ORDER BY found_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + location + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LostFound item = createLostFoundFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error searching items by location: " + e.getMessage());
        }
        return items;
    }
    
    /**
     * Get total item count
     */
    public int getTotalItemCount() {
        String sql = "SELECT COUNT(*) FROM lost_found";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting item count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get unclaimed item count
     */
    public int getUnclaimedItemCount() {
        String sql = "SELECT COUNT(*) FROM lost_found WHERE status = 'Unclaimed'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting unclaimed item count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Check if item ID exists
     */
    public boolean itemIdExists(String itemId) {
        String sql = "SELECT COUNT(*) FROM lost_found WHERE item_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking item ID: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Generate next item ID
     */
    public String generateNextItemId() {
        String sql = "SELECT MAX(item_id) FROM lost_found";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith("LF")) {
                    try {
                        int lastNumber = Integer.parseInt(lastId.substring(2));
                        return String.format("LF%03d", lastNumber + 1);
                    } catch (NumberFormatException e) {
                        // If parsing fails, start from 1
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next item ID: " + e.getMessage());
        }
        return "LF001";
    }
    
    /**
     * Get item statistics
     */
    public java.util.Map<String, Integer> getItemStatistics() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        
        String sql = "SELECT status, COUNT(*) as count FROM lost_found GROUP BY status";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getInt("count"));
            }
            
            // Total items
            sql = "SELECT COUNT(*) as total FROM lost_found";
            ResultSet totalRs = stmt.executeQuery(sql);
            if (totalRs.next()) {
                stats.put("TOTAL", totalRs.getInt("total"));
            }
            
            // Items found this month
            LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
            sql = "SELECT COUNT(*) as this_month FROM lost_found WHERE found_date >= ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, monthStart.toString());
                ResultSet monthRs = pstmt.executeQuery();
                if (monthRs.next()) {
                    stats.put("THIS_MONTH", monthRs.getInt("this_month"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting item statistics: " + e.getMessage());
        }
        return stats;
    }
    
    /**
     * Get items by status
     */
    public List<LostFound> getItemsByStatus(String status) {
        List<LostFound> items = new ArrayList<>();
        String sql = "SELECT * FROM lost_found WHERE status = ? ORDER BY found_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LostFound item = createLostFoundFromResultSet(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting items by status: " + e.getMessage());
        }
        return items;
    }
    
    /**
     * Archive old claimed items (more than 90 days)
     */
    public boolean archiveOldItems() {
        LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
        String sql = "DELETE FROM lost_found WHERE status = 'Claimed' AND found_date < ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ninetyDaysAgo.toString());
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Archived " + rowsAffected + " old claimed items.");
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error archiving old items: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper method to create LostFound object from ResultSet
     */
    private LostFound createLostFoundFromResultSet(ResultSet rs) throws SQLException {
        String itemId = rs.getString("item_id");
        String itemName = rs.getString("item_name");
        String description = rs.getString("description");
        String foundLocation = rs.getString("found_location");
        String foundDate = rs.getString("found_date");
        String status = rs.getString("status");
        
        return new LostFound(itemId, itemName, description, foundLocation, foundDate, status);
    }
}