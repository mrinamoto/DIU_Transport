package model.enums;

public enum DriverShift {
    MORNING("Morning"),
    EVENING("Evening"),
    NIGHT("Night"),
    FLEXIBLE("Flexible"),
    PART_TIME("Part-Time");
    
    private final String displayName;
    
    DriverShift(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public static DriverShift fromString(String text) {
        for (DriverShift shift : DriverShift.values()) {
            if (shift.displayName.equalsIgnoreCase(text) || shift.name().equalsIgnoreCase(text)) {
                return shift;
            }
        }
        throw new IllegalArgumentException("No constant with text " + text + " found");
    }
}