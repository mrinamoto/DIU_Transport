package model;

import model.enums.UserRole;

/**
 * Staff class - extends User class
 * Staff members have position information
 */
public class Staff extends User {
    private String position;
    
    // Constructor with String position
    public Staff(int id, String username, String password, String name, 
                 String email, String phone, String position) {
        super(id, username, password, UserRole.STAFF, name, email, phone);
        this.position = position;
    }
    
    // Alternative constructor for loading from database
    public Staff(int id, String username, String password, UserRole role,
                 String name, String email, String phone, String position) {
        super(id, username, password, role, name, email, phone);
        this.position = position;
    }
    
    // Constructor with String role for compatibility
    public Staff(int id, String username, String password, String role,
                 String name, String email, String phone, String position) {
        super(id, username, password, role, name, email, phone);
        this.position = position;
    }
    
    public String getPosition() { 
        return position; 
    }
    
    public void setPosition(String position) { 
        this.position = position; 
    }
    
    @Override
    public String toString() {
        return "Staff{" + 
               "id=" + getId() + 
               ", name='" + getName() + "'" + 
               ", username='" + getUsername() + "'" + 
               ", position='" + position + "'" + 
               ", email='" + getEmail() + "'" + 
               ", phone='" + getPhone() + "'" + 
               '}';
    }
}