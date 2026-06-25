package model.enums;

public enum ContactRole {
    TRANSPORT_MANAGER("Transport Manager"),
    TRANSPORT_OFFICER("Transport Officer"),
    EMERGENCY_HOTLINE("Emergency Hotline"),
    ADMINISTRATOR("Administrator"),
    SUPPORT("Support"),
    SECURITY("Security");
    
    private final String displayName;
    
    ContactRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public static ContactRole fromString(String text) {
        for (ContactRole role : ContactRole.values()) {
            if (role.displayName.equalsIgnoreCase(text) || role.name().equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}