package model.enums;

public enum BusStatus {
    ACTIVE("Active"),
    MAINTENANCE("Maintenance"),
    OUT_OF_SERVICE("Out of Service"),
    RESERVED("Reserved"),
    CLEANING("Cleaning"),
    FUELING("Fueling");
    
    private final String displayName;
    
    BusStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public static BusStatus fromString(String text) {
        for (BusStatus status : BusStatus.values()) {
            if (status.displayName.equalsIgnoreCase(text) || status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}