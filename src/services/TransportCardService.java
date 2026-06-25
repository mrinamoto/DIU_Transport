package services;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.TransportCard;
import model.enums.CardStatus;
import model.enums.PaymentStatus;
import util.DatabaseConnection;

/**
 * TransportCardService handles transport card operations with database integration
 */
public class TransportCardService {
    private final Connection connection; // Made final as suggested
   
    public TransportCardService() {
        this.connection = DatabaseConnection.getConnection();
    }
   
    /**
     * Create a new transport card
     */
    public boolean createTransportCard(TransportCard card) {
        String sql = "INSERT INTO transport_cards (card_id, user_id, issue_date, expiry_date, status, payment_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, card.getCardId());
            pstmt.setInt(2, card.getUserId());
            pstmt.setString(3, card.getIssueDate());
            pstmt.setString(4, card.getExpiryDate());
            pstmt.setString(5, card.getStatusAsString());
            pstmt.setString(6, card.getPaymentStatusAsString());
           
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating transport card: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Issue a new transport card to user
     */
    public boolean issueNewCard(int userId, String cardId, String semester) {
        // Calculate dates based on semester
        LocalDate issueDate = LocalDate.now();
        LocalDate expiryDate = calculateExpiryDate(semester);
       
        String sql = "INSERT INTO transport_cards (card_id, user_id, issue_date, expiry_date, status, payment_status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            pstmt.setInt(2, userId);
            pstmt.setString(3, issueDate.toString());
            pstmt.setString(4, expiryDate.toString());
            pstmt.setString(5, CardStatus.PENDING.toString());
            pstmt.setString(6, PaymentStatus.DUE.toString());
           
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error issuing new card: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Get transport card by user ID
     */
    public TransportCard getCardByUserId(int userId) {
        String sql = "SELECT * FROM transport_cards WHERE user_id = ? ORDER BY issue_date DESC LIMIT 1";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()) {
                return createCardFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting transport card for user " + userId + ": " + e.getMessage());
        }
        return null;
    }
   
    /**
     * Get transport card by card ID
     */
    public TransportCard getCardById(String cardId) {
        String sql = "SELECT * FROM transport_cards WHERE card_id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()) {
                return createCardFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting card by ID: " + e.getMessage());
        }
        return null;
    }
   
    /**
     * Update transport card status
     */
    public boolean updateCardStatus(String cardId, CardStatus status) {
        String sql = "UPDATE transport_cards SET status = ? WHERE card_id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.toString());
            pstmt.setString(2, cardId);
           
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating card status: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Update payment status
     */
    public boolean updatePaymentStatus(String cardId, PaymentStatus paymentStatus) {
        String sql = "UPDATE transport_cards SET payment_status = ? WHERE card_id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, paymentStatus.toString());
            pstmt.setString(2, cardId);
           
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Activate card (after payment)
     */
    public boolean activateCard(String cardId) {
        String sql = "UPDATE transport_cards SET status = ?, payment_status = ? WHERE card_id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, CardStatus.ACTIVE.toString());
            pstmt.setString(2, PaymentStatus.PAID.toString());
            pstmt.setString(3, cardId);
           
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error activating card: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Renew expired card
     */
    public boolean renewCard(String cardId, String newExpiryDate) {
        String sql = "UPDATE transport_cards SET expiry_date = ?, status = ?, payment_status = ? WHERE card_id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newExpiryDate);
            pstmt.setString(2, CardStatus.PENDING.toString());
            pstmt.setString(3, PaymentStatus.DUE.toString());
            pstmt.setString(4, cardId);
           
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error renewing card: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Get all transport cards (for admin)
     */
    public List<TransportCard> getAllCards() {
        List<TransportCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM transport_cards ORDER BY issue_date DESC";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
                cards.add(createCardFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all cards: " + e.getMessage());
        }
        return cards;
    }
   
    /**
     * Get cards by status
     */
    public List<TransportCard> getCardsByStatus(CardStatus status) {
        List<TransportCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM transport_cards WHERE status = ? ORDER BY issue_date DESC";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status.toString());
            ResultSet rs = pstmt.executeQuery();
           
            while (rs.next()) {
                cards.add(createCardFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting cards by status: " + e.getMessage());
        }
        return cards;
    }
   
    /**
     * Get expired cards
     */
    public List<TransportCard> getExpiredCards() {
        List<TransportCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM transport_cards WHERE expiry_date < ? AND status != 'EXPIRED'";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();
           
            while (rs.next()) {
                TransportCard card = createCardFromResultSet(rs);
                if (card.isExpired()) {
                    cards.add(card);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting expired cards: " + e.getMessage());
        }
        return cards;
    }
   
    /**
     * Check if user already has active card
     */
    public boolean hasActiveCard(int userId) {
        String sql = "SELECT COUNT(*) FROM transport_cards WHERE user_id = ? AND status = 'ACTIVE'";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking active card: " + e.getMessage());
        }
        return false;
    }
   
    /**
     * Delete a transport card
     */
    public boolean deleteCard(String cardId) {
        String sql = "DELETE FROM transport_cards WHERE card_id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting card: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Get card statistics
     */
    public java.util.Map<String, Integer> getCardStatistics() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
       
        String sql = "SELECT status, COUNT(*) as count FROM transport_cards GROUP BY status";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getInt("count"));
            }
           
            // Total cards
            sql = "SELECT COUNT(*) as total FROM transport_cards";
            ResultSet totalRs = stmt.executeQuery(sql);
            if (totalRs.next()) {
                stats.put("TOTAL", totalRs.getInt("total"));
            }
           
        } catch (SQLException e) {
            System.err.println("Error getting card statistics: " + e.getMessage());
        }
        return stats;
    }
    
    /**
     * Check if card ID exists
     */
    public boolean cardIdExists(String cardId) {
        String sql = "SELECT COUNT(*) FROM transport_cards WHERE card_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cardId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking card ID: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Generate next card ID
     */
    public String generateNextCardId() {
        String sql = "SELECT MAX(card_id) FROM transport_cards";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith("TC")) {
                    try {
                        int lastNumber = Integer.parseInt(lastId.substring(2));
                        return String.format("TC%03d", lastNumber + 1);
                    } catch (NumberFormatException e) {
                        // If parsing fails, start from 1
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next card ID: " + e.getMessage());
        }
        return "TC001";
    }
    
    /**
     * Get cards with due payment
     */
    public List<TransportCard> getCardsWithDuePayment() {
        List<TransportCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM transport_cards WHERE payment_status = 'DUE' ORDER BY issue_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                cards.add(createCardFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting cards with due payment: " + e.getMessage());
        }
        return cards;
    }
    
    /**
     * Mark expired cards in database
     */
    public boolean markExpiredCards() {
        String sql = "UPDATE transport_cards SET status = 'EXPIRED' WHERE expiry_date < ? AND status != 'EXPIRED'";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().toString());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Marked " + rowsAffected + " cards as expired.");
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error marking expired cards: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Search cards by user ID or card ID
     */
    public List<TransportCard> searchCards(String keyword) {
        List<TransportCard> cards = new ArrayList<>();
        String sql = "SELECT * FROM transport_cards WHERE card_id LIKE ? ORDER BY issue_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                cards.add(createCardFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching cards: " + e.getMessage());
        }
        return cards;
    }
    
    /**
     * Get cards that need renewal (expiring within 30 days)
     */
    public List<TransportCard> getCardsNeedingRenewal() {
        List<TransportCard> cards = new ArrayList<>();
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        
        String sql = "SELECT * FROM transport_cards WHERE expiry_date BETWEEN ? AND ? AND status = 'ACTIVE'";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setString(2, thirtyDaysFromNow.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                cards.add(createCardFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting cards needing renewal: " + e.getMessage());
        }
        return cards;
    }
   
    /**
     * Helper method to create card from ResultSet
     */
    private TransportCard createCardFromResultSet(ResultSet rs) throws SQLException {
        return new TransportCard(
            rs.getString("card_id"),
            rs.getInt("user_id"),
            rs.getString("issue_date"),
            rs.getString("expiry_date"),
            rs.getString("status"),
            rs.getString("payment_status")
        );
    }
   
    /**
     * Helper method to calculate expiry date based on semester
     */
    private LocalDate calculateExpiryDate(String semester) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
       
        // Parse semester like "Spring-2024"
        String[] parts = semester.split("-");
        if (parts.length == 2) {
            try {
                year = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                // Use current year
            }
        }
       
        // Set expiry to end of semester based on parsed year
        return LocalDate.of(year, 12, 31);
    }
}