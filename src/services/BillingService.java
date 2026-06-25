package services;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Billing;
import model.enums.PaymentStatus;
import util.DatabaseConnection;

/**
 * BillingService handles payment and billing information with database integration
 */
public class BillingService {
    private final Connection connection;
    
    public BillingService() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    /**
     * Create a new bill
     */
    public boolean createBill(Billing bill) {
        String sql = "INSERT INTO billing (billing_id, user_id, amount, semester, payment_status, due_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, bill.getBillingId());
            pstmt.setInt(2, bill.getUserId());
            pstmt.setDouble(3, bill.getAmount());
            pstmt.setString(4, bill.getSemester());
            pstmt.setString(5, bill.getPaymentStatus().toString());
            pstmt.setString(6, bill.getDueDate());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating bill: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create a new bill with all fields
     */
    public boolean createFullBill(String billingId, int userId, double amount, 
                                 String semester, PaymentStatus paymentStatus, 
                                 String dueDate, String paymentDate, String paymentMethod) {
        String sql = "INSERT INTO billing (billing_id, user_id, amount, semester, payment_status, due_date, payment_date, payment_method) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, billingId);
            pstmt.setInt(2, userId);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, semester);
            pstmt.setString(5, paymentStatus.toString());
            pstmt.setString(6, dueDate);
            pstmt.setString(7, paymentDate);
            pstmt.setString(8, paymentMethod);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating bill: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all bills
     */
    public List<Billing> getAllBills() {
        List<Billing> bills = new ArrayList<>();
        String sql = "SELECT * FROM billing ORDER BY due_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Billing bill = createBillingFromResultSet(rs);
                bills.add(bill);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all bills: " + e.getMessage());
        }
        return bills;
    }
    
    /**
     * Get bills by user ID
     */
    public List<Billing> getBillsByUserId(int userId) {
        List<Billing> userBills = new ArrayList<>();
        String sql = "SELECT * FROM billing WHERE user_id = ? ORDER BY due_date DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Billing bill = createBillingFromResultSet(rs);
                userBills.add(bill);
            }
        } catch (SQLException e) {
            System.err.println("Error getting bills for user " + userId + ": " + e.getMessage());
        }
        return userBills;
    }
    
    /**
     * Get bill by ID
     */
    public Billing getBillById(String billingId) {
        String sql = "SELECT * FROM billing WHERE billing_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, billingId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createBillingFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting bill by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Update payment status
     */
    public boolean updatePaymentStatus(String billingId, PaymentStatus paymentStatus) {
        String sql = "UPDATE billing SET payment_status = ? WHERE billing_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, paymentStatus.toString());
            pstmt.setString(2, billingId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Record payment
     */
    public boolean recordPayment(String billingId, String paymentMethod) {
        String sql = "UPDATE billing SET payment_status = ?, payment_date = ?, payment_method = ? WHERE billing_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, PaymentStatus.PAID.toString());
            pstmt.setString(2, LocalDate.now().toString());
            pstmt.setString(3, paymentMethod);
            pstmt.setString(4, billingId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error recording payment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update payment information
     */
    public boolean updatePayment(String billingId, PaymentStatus paymentStatus, 
                                String paymentDate, String paymentMethod) {
        String sql = "UPDATE billing SET payment_status = ?, payment_date = ?, payment_method = ? WHERE billing_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, paymentStatus.toString());
            pstmt.setString(2, paymentDate);
            pstmt.setString(3, paymentMethod);
            pstmt.setString(4, billingId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating payment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get total outstanding amount for a user
     */
    public double getTotalOutstanding(int userId) {
        String sql = "SELECT SUM(amount) FROM billing WHERE user_id = ? AND payment_status IN ('Due', 'Overdue')";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error calculating outstanding amount: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Get overdue bills
     */
    public List<Billing> getOverdueBills() {
        List<Billing> overdueBills = new ArrayList<>();
        String sql = "SELECT * FROM billing WHERE due_date < ? AND payment_status IN ('Due', 'Partial')";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Billing bill = createBillingFromResultSet(rs);
                overdueBills.add(bill);
            }
        } catch (SQLException e) {
            System.err.println("Error getting overdue bills: " + e.getMessage());
        }
        return overdueBills;
    }
    
    /**
     * Get paid bills
     */
    public List<Billing> getPaidBills() {
        List<Billing> paidBills = new ArrayList<>();
        String sql = "SELECT * FROM billing WHERE payment_status = 'Paid' ORDER BY payment_date DESC";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Billing bill = createBillingFromResultSet(rs);
                paidBills.add(bill);
            }
        } catch (SQLException e) {
            System.err.println("Error getting paid bills: " + e.getMessage());
        }
        return paidBills;
    }
    
    /**
     * Delete a bill (admin only)
     */
    public boolean deleteBill(String billingId) {
        String sql = "DELETE FROM billing WHERE billing_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, billingId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting bill: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get bill statistics
     */
    public java.util.Map<String, Integer> getBillStatistics() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        
        String sql = "SELECT payment_status, COUNT(*) as count FROM billing GROUP BY payment_status";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stats.put(rs.getString("payment_status"), rs.getInt("count"));
            }
            
            // Total bills
            sql = "SELECT COUNT(*) as total FROM billing";
            ResultSet totalRs = stmt.executeQuery(sql);
            if (totalRs.next()) {
                stats.put("TOTAL", totalRs.getInt("total"));
            }
            
            // Total revenue
            sql = "SELECT SUM(amount) as revenue FROM billing WHERE payment_status = 'Paid'";
            ResultSet revenueRs = stmt.executeQuery(sql);
            if (revenueRs.next()) {
                stats.put("REVENUE", (int)revenueRs.getDouble("revenue"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting bill statistics: " + e.getMessage());
        }
        return stats;
    }
    
    /**
     * Get total revenue
     */
    public double getTotalRevenue() {
        String sql = "SELECT SUM(amount) FROM billing WHERE payment_status = 'Paid'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total revenue: " + e.getMessage());
        }
        return 0.0;
    }
    
    /**
     * Generate next billing ID
     */
    public String generateNextBillingId() {
        String sql = "SELECT MAX(billing_id) FROM billing";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith("BILL")) {
                    try {
                        int lastNumber = Integer.parseInt(lastId.substring(4));
                        return String.format("BILL%04d", lastNumber + 1);
                    } catch (NumberFormatException e) {
                        // If parsing fails, start from 1
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next billing ID: " + e.getMessage());
        }
        return "BILL0001";
    }
    
    /**
     * Helper method to create Billing object from ResultSet
     */
    private Billing createBillingFromResultSet(ResultSet rs) throws SQLException {
        String billingId = rs.getString("billing_id");
        int userId = rs.getInt("user_id");
        double amount = rs.getDouble("amount");
        String semester = rs.getString("semester");
        PaymentStatus paymentStatus = PaymentStatus.fromString(rs.getString("payment_status"));
        String dueDate = rs.getString("due_date");
        
        return new Billing(billingId, userId, amount, semester, paymentStatus, dueDate);
    }
}