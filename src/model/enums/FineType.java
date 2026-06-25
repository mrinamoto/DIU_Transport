package model.enums;

public enum FineType {
    LATE_PAYMENT("Late Payment"),
    TRANSPORT_MISUSE("Transport Misuse"),
    DAMAGE_TO_BUS("Damage to Bus"),
    UNAUTHORIZED_TRAVEL("Unauthorized Travel"),
    LOST_CARD("Lost Card"),
    RULE_VIOLATION("Rule Violation"),
    LITTERING("Littering"),
    MISBEHAVIOR("Misbehavior"),
    OTHER("Other");
    
    private final String displayName;
    
    FineType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public static FineType fromString(String text) {
        for (FineType type : FineType.values()) {
            if (type.displayName.equalsIgnoreCase(text) || type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}