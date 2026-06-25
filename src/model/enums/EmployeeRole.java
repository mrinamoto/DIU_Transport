package model.enums;

public enum EmployeeRole {
    MAINTENANCE("Maintenance"),
    TRANSPORT_OFFICER("Transport Officer"),
    HELPER("Helper"),
    SUPERVISOR("Supervisor"),
    ADMINISTRATOR("Administrator"),
    CLEANER("Cleaner");
    
    private final String displayName;
    
    EmployeeRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public static EmployeeRole fromString(String text) {
        for (EmployeeRole role : EmployeeRole.values()) {
            if (role.displayName.equalsIgnoreCase(text) || role.name().equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}