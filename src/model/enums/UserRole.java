package model.enums;

public enum UserRole {
    ADMIN("Admin"),
    STUDENT("Student"),
    TEACHER("Teacher"),
    STAFF("Staff"),
    DRIVER("Driver"),
    GUEST("Guest");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public static UserRole fromString(String text) {
        for (UserRole role : UserRole.values()) {
            if (role.displayName.equalsIgnoreCase(text) || role.name().equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}