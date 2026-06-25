package model;

import model.enums.UserRole;

/**
 * Admin class - extends User class
 * Admin has special privileges
 */
public class Admin extends User {
    
    // Constructor
    public Admin(int id, String username, String password, String name, 
                 String email, String phone) {
        super(id, username, password, UserRole.ADMIN, name, email, phone);
    }
    
    // Alternative constructor for loading from database
    public Admin(int id, String username, String password, UserRole role,
                 String name, String email, String phone) {
        super(id, username, password, role, name, email, phone);
    }
    
    // Constructor with String role for compatibility
    public Admin(int id, String username, String password, String role,
                 String name, String email, String phone) {
        super(id, username, password, role, name, email, phone);
    }
    
    // Admin-specific methods can be added here
    public boolean hasFullAccess() {
        return true; // Admin always has full access
    }
    
    @Override
    public String toString() {
        return "Admin{" + 
               "id=" + getId() + 
               ", name='" + getName() + "'" + 
               ", username='" + getUsername() + "'" + 
               ", email='" + getEmail() + "'" + 
               ", phone='" + getPhone() + "'" + 
               '}';
    }
}