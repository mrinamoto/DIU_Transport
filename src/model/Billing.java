package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import model.enums.PaymentStatus;

/**
 * Billing class represents payment information
 */
public class Billing {
    private String billingId;
    private int userId;
    private double amount;
    private String semester;
    private PaymentStatus paymentStatus;
    private String dueDate;
    private String paymentDate; // Added
    private String paymentMethod; // Added
    
    // Constructor with all fields
    public Billing(String billingId, int userId, double amount, String semester, 
                  PaymentStatus paymentStatus, String dueDate, String paymentDate, String paymentMethod) {
        this.billingId = billingId;
        this.userId = userId;
        this.amount = amount;
        this.semester = semester;
        this.paymentStatus = paymentStatus;
        this.dueDate = dueDate;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }
    
    // Constructor without payment info (for new bills)
    public Billing(String billingId, int userId, double amount, String semester, 
                  PaymentStatus paymentStatus, String dueDate) {
        this(billingId, userId, amount, semester, paymentStatus, dueDate, null, null);
    }
    
    // Getters
    public String getBillingId() { return billingId; }
    public int getUserId() { return userId; }
    public double getAmount() { return amount; }
    public String getSemester() { return semester; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public String getDueDate() { return dueDate; }
    public String getPaymentDate() { return paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    
    // Setters
    public void setBillingId(String billingId) { this.billingId = billingId; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
    public void setPaymentDate(String paymentDate) { this.paymentDate = paymentDate; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    // Business methods
    public boolean isOverdue() {
        if (paymentStatus == PaymentStatus.PAID) {
            return false;
        }
        
        try {
            LocalDate due = LocalDate.parse(dueDate, DateTimeFormatter.ISO_DATE);
            return LocalDate.now().isAfter(due);
        } catch (Exception e) {
            return false;
        }
    }
    
    public double calculateLateFee() {
        if (!isOverdue() || paymentStatus == PaymentStatus.PAID) {
            return 0.0;
        }
        
        try {
            LocalDate due = LocalDate.parse(dueDate, DateTimeFormatter.ISO_DATE);
            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(due, LocalDate.now());
            
            if (daysLate <= 0) return 0.0;
            
            double lateFeePercent = Math.min(daysLate, 20) * 0.01;
            return amount * lateFeePercent;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    @Override
    public String toString() {
        return "Billing{billingId='" + billingId + "', userId=" + userId + 
               ", amount=" + amount + ", semester='" + semester + 
               "', paymentStatus=" + paymentStatus + ", dueDate='" + dueDate + "'}";
    }
}