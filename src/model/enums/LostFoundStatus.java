package model.enums;

public enum LostFoundStatus {
    CLAIMED("Claimed"),
    UNCLAIMED("Unclaimed"),
    PROCESSING("Processing"),
    ARCHIVED("Archived");
    
    private final String displayName;
    
    LostFoundStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public static LostFoundStatus fromString(String text) {
        for (LostFoundStatus status : LostFoundStatus.values()) {
            if (status.displayName.equalsIgnoreCase(text) || status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}