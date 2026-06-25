package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import model.enums.CardStatus;
import model.enums.PaymentStatus;

public class TransportCard {
    private String cardId;
    private int userId;
    private String issueDate;
    private String expiryDate;
    private CardStatus status;  // Changed to enum
    private PaymentStatus paymentStatus;  // Changed to enum
    
    public TransportCard(String cardId, int userId, String issueDate, 
                        String expiryDate, CardStatus status, PaymentStatus paymentStatus) {
        this.cardId = cardId;
        this.userId = userId;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.paymentStatus = paymentStatus;
    }
    
    // Constructor with String parameters for loading from storage
    public TransportCard(String cardId, int userId, String issueDate, 
                        String expiryDate, String status, String paymentStatus) {
        this(cardId, userId, issueDate, expiryDate, 
             CardStatus.fromString(status), 
             PaymentStatus.fromString(paymentStatus));
    }
    
    // Getters
    public String getCardId() { return cardId; }
    public int getUserId() { return userId; }
    public String getIssueDate() { return issueDate; }
    public String getExpiryDate() { return expiryDate; }
    public CardStatus getStatus() { return status; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    
    // Getters as String for compatibility
    public String getStatusAsString() { return status.toString(); }
    public String getPaymentStatusAsString() { return paymentStatus.toString(); }
    
    // Check if card is expired
    public boolean isExpired() {
        try {
            LocalDate expiry = LocalDate.parse(expiryDate, DateTimeFormatter.ISO_DATE);
            return LocalDate.now().isAfter(expiry);
        } catch (Exception e) {
            return false;
        }
    }
    
    // Setters with validation
    public void setCardId(String cardId) {
        if (cardId == null || cardId.trim().isEmpty()) {
            throw new IllegalArgumentException("Card ID cannot be empty");
        }
        this.cardId = cardId;
    }
    
    public void setUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        this.userId = userId;
    }
    
    public void setIssueDate(String issueDate) {
        if (issueDate == null || issueDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue date cannot be empty");
        }
        this.issueDate = issueDate;
    }
    
    public void setExpiryDate(String expiryDate) {
        if (expiryDate == null || expiryDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Expiry date cannot be empty");
        }
        this.expiryDate = expiryDate;
    }
    
    public void setStatus(CardStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        if (paymentStatus == null) {
            throw new IllegalArgumentException("Payment status cannot be null");
        }
        this.paymentStatus = paymentStatus;
    }
    
    @Override
    public String toString() {
        return "TransportCard{cardId='" + cardId + "', userId=" + userId + 
               ", status=" + status + ", paymentStatus=" + paymentStatus + "}";
    }
}