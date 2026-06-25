package model;

/**
 * Contact class represents emergency contact information
 */
public class Contact {
    private String contactId;
    private String name;
    private String role;
    private String phone;
    private String email;
    
    // Valid contact roles
    private static final String[] VALID_ROLES = {
        "Transport Manager", "Transport Officer", "Emergency Hotline", 
        "Administrator", "Support"
    };
    
    public Contact(String contactId, String name, String role, String phone, String email) {
        if (contactId == null || contactId.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact ID cannot be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact name cannot be empty");
        }
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        boolean isValidRole = false;
        for (String validRole : VALID_ROLES) {
            if (validRole.equalsIgnoreCase(role.trim())) {
                this.role = validRole;
                isValidRole = true;
                break;
            }
        }
        if (!isValidRole) {
            throw new IllegalArgumentException("Invalid role. Must be one of: " + 
                    String.join(", ", VALID_ROLES));
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.contactId = contactId;
        this.name = name.trim();
        this.phone = phone.trim();
        this.email = email.trim().toLowerCase();
    }
    
    // Getters
    public String getContactId() { return contactId; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    
    // Setters with validation
    public void setContactId(String contactId) {
        if (contactId == null || contactId.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact ID cannot be empty");
        }
        this.contactId = contactId;
    }
    
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact name cannot be empty");
        }
        this.name = name.trim();
    }
    
    public void setRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role cannot be empty");
        }
        
        // Check if role is valid
        boolean isValid = false;
        for (String validRole : VALID_ROLES) {
            if (validRole.equalsIgnoreCase(role.trim())) {
                this.role = validRole; // Store in consistent format
                isValid = true;
                break;
            }
        }
        
        if (!isValid) {
            throw new IllegalArgumentException("Invalid role. Must be one of: " + 
                    String.join(", ", VALID_ROLES));
        }
    }
    
    public void setPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone cannot be empty");
        }
        this.phone = phone.trim();
    }
    
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        // Basic email validation
        if (!email.contains("@") || !email.contains(".")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        this.email = email.trim().toLowerCase();
    }
    
    // Check if this is an emergency contact
    public boolean isEmergencyContact() {
        return "Emergency Hotline".equals(role) || "Transport Manager".equals(role);
    }
    
    @Override
    public String toString() {
        return "Contact{contactId='" + contactId + "', name='" + name + 
               "', role='" + role + "', phone='" + phone + "'}";
    }
}