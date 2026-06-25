package model;

/**
 * Employee class represents transport staff employees
 */
public class Employee {
    private String employeeId;
    private String name;
    private String role; // Maintenance, Officer, Helper
    private String phone;
    private String email;
    
    // Valid employee roles
    private static final String[] VALID_ROLES = {
        "Maintenance", "Transport Officer", "Helper", "Supervisor", "Administrator"
    };
    
    public Employee(String employeeId, String name, String role, String phone, String email) {
        setEmployeeId(employeeId);
        setName(name);
        setRole(role);
        setPhone(phone);
        setEmail(email);
    }
    
    // Getters
    public String getEmployeeId() { return employeeId; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    
    // Setters with validation
    public void setEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be empty");
        }
        this.employeeId = employeeId;
    }
    
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be empty");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Name cannot exceed 50 characters");
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
        
        // Simple phone validation for Bangladesh format
        String cleanedPhone = phone.replaceAll("[^0-9]", "");
        if (cleanedPhone.length() != 11 || !cleanedPhone.startsWith("01")) {
            throw new IllegalArgumentException("Invalid phone number. Must be 11 digits starting with 01");
        }
        this.phone = cleanedPhone;
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
    
    // Check if employee is management level
    public boolean isManagement() {
        return "Transport Officer".equals(role) || "Supervisor".equals(role) || 
               "Administrator".equals(role);
    }
    
    @Override
    public String toString() {
        return "Employee{employeeId='" + employeeId + "', name='" + name + 
               "', role='" + role + "', phone='" + phone + "'}";
    }
}