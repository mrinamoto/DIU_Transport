package model.enums;

public enum NotificationType {
    SCHEDULE_UPDATE("Schedule Update"),
    CANCELLATION("Cancellation"),
    NEW_ROUTE("New Route"),
    SPECIAL_TRIP("Special Trip"),
    SYSTEM_ALERT("System Alert"),
    MAINTENANCE("Maintenance"),
    PAYMENT_REMINDER("Payment Reminder");
    
    private final String displayName;
    
    NotificationType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public static NotificationType fromString(String text) {
        for (NotificationType type : NotificationType.values()) {
            if (type.displayName.equalsIgnoreCase(text) || type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}