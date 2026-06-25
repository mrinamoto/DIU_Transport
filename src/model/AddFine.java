package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import model.enums.FineStatus;
import model.enums.FineType;

/**
 * AddFine class represents fines for breaking transport rules
 */
public class AddFine {
    private String fineId;
    private int userId;
    private String description;
    private double amount;
    private FineType fineType;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private FineStatus status;
    private String issuedBy; // Admin who issued the fine
    private String violationDetails;
    
    // Constructor with all parameters
    public AddFine(String fineId, int userId, String description, double amount,
                  FineType fineType, LocalDate issueDate, LocalDate dueDate,
                  FineStatus status, String issuedBy, String violationDetails) {
        setFineId(fineId);
        setUserId(userId);
        setDescription(description);
        setAmount(amount);
        setFineType(fineType);
        setIssueDate(issueDate);
        setDueDate(dueDate);
        setStatus(status);
        setIssuedBy(issuedBy);
        setViolationDetails(violationDetails);
    }
    
    // Constructor with String dates for convenience
    public AddFine(String fineId, int userId, String description, double amount,
                  FineType fineType, String issueDate, String dueDate,
                  FineStatus status, String issuedBy, String violationDetails) {
        this(fineId, userId, description, amount, fineType,
             parseDate(issueDate), parseDate(dueDate),
             status, issuedBy, violationDetails);
    }
    
    // Getters
    public String getFineId() { return fineId; }
    public int getUserId() { return userId; }
    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public FineType getFineType() { return fineType; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public FineStatus getStatus() { return status; }
    public String getIssuedBy() { return issuedBy; }
    public String getViolationDetails() { return violationDetails; }
    
    // Formatted date getters
    public String getIssueDateString() { 
        return issueDate.format(DateTimeFormatter.ISO_DATE); 
    }
    
    public String getDueDateString() { 
        return dueDate.format(DateTimeFormatter.ISO_DATE); 
    }
    
    // Setters with validation
    public void setFineId(String fineId) {
        if (fineId == null || fineId.trim().isEmpty()) {
            throw new IllegalArgumentException("Fine ID cannot be empty");
        }
        this.fineId = fineId;
    }
    
    public void setUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be positive");
        }
        this.userId = userId;
    }
    
    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty");
        }
        if (description.length() > 200) {
            throw new IllegalArgumentException("Description cannot exceed 200 characters");
        }
        this.description = description.trim();
    }
    
    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > 10000) {
            throw new IllegalArgumentException("Fine amount cannot exceed 10,000");
        }
        this.amount = amount;
    }
    
    public void setFineType(FineType fineType) {
        if (fineType == null) {
            throw new IllegalArgumentException("Fine type cannot be null");
        }
        this.fineType = fineType;
    }
    
    public void setIssueDate(LocalDate issueDate) {
        if (issueDate == null) {
            throw new IllegalArgumentException("Issue date cannot be null");
        }
        if (issueDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Issue date cannot be in the future");
        }
        this.issueDate = issueDate;
    }
    
    public void setDueDate(LocalDate dueDate) {
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }
        if (dueDate.isBefore(issueDate)) {
            throw new IllegalArgumentException("Due date cannot be before issue date");
        }
        this.dueDate = dueDate;
    }
    
    public void setStatus(FineStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        this.status = status;
    }
    
    public void setIssuedBy(String issuedBy) {
        if (issuedBy == null || issuedBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Issued by cannot be empty");
        }
        this.issuedBy = issuedBy.trim();
    }
    
    public void setViolationDetails(String violationDetails) {
        this.violationDetails = violationDetails != null ? violationDetails.trim() : "";
    }
    
    // Business methods
    public boolean isOverdue() {
        if (status == FineStatus.PAID || status == FineStatus.WAIVED) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate);
    }
    
    public double calculateLateFee() {
        if (!isOverdue() || status == FineStatus.PAID || status == FineStatus.WAIVED) {
            return 0.0;
        }
        
        long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        if (daysLate <= 0) return 0.0;
        
        // 0.5% per day late fee, max 25% of original fine
        double lateFeePercent = Math.min(daysLate * 0.005, 0.25);
        return amount * lateFeePercent;
    }
    
    public double getTotalAmount() {
        return amount + calculateLateFee();
    }
    
    public void payFine() {
        if (status == FineStatus.PAID) {
            throw new IllegalStateException("Fine is already paid");
        }
        this.status = FineStatus.PAID;
    }
    
    public void waiveFine() {
        if (status == FineStatus.WAIVED) {
            throw new IllegalStateException("Fine is already waived");
        }
        this.status = FineStatus.WAIVED;
    }
    
    public void appealFine() {
        if (status == FineStatus.PAID || status == FineStatus.WAIVED) {
            throw new IllegalStateException("Cannot appeal paid or waived fine");
        }
        this.status = FineStatus.APPEALED;
    }
    
    public boolean canAppeal() {
        return status != FineStatus.PAID && status != FineStatus.WAIVED;
    }
    
    // Helper method to parse date
    private static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be empty");
        }
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd");
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "AddFine{fineId='%s', userId=%d, type=%s, amount=%.2f, status=%s, dueDate=%s}",
            fineId, userId, fineType, amount, status, getDueDateString()
        );
    }
}