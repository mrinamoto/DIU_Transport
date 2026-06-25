package model.enums;

public enum FineStatus {
    PENDING("Pending"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    WAIVED("Waived"),
    APPEALED("Appealed"),
    PARTIAL("Partial Payment");
    
    private final String displayName;
    
    FineStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public static FineStatus fromString(String text) {
        for (FineStatus status : FineStatus.values()) {
            if (status.displayName.equalsIgnoreCase(text) || status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}