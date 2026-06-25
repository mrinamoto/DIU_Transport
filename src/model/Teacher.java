package model;

import model.enums.UserRole;

/**
 * Teacher class - extends User class
 * Teachers have department information
 */
public class Teacher extends User {
    private String department;
    
    // Constructor with String department
    public Teacher(int id, String username, String password, String name, 
                   String email, String phone, String department) {
        super(id, username, password, UserRole.TEACHER, name, email, phone);
        this.department = department;
    }
    
    // Alternative constructor for loading from database
    public Teacher(int id, String username, String password, UserRole role,
                   String name, String email, String phone, String department) {
        super(id, username, password, role, name, email, phone);
        this.department = department;
    }
    
    // Constructor with String role for compatibility
    public Teacher(int id, String username, String password, String role,
                   String name, String email, String phone, String department) {
        super(id, username, password, role, name, email, phone);
        this.department = department;
    }
    
    public String getDepartment() { 
        return department; 
    }
    
    public void setDepartment(String department) { 
        this.department = department; 
    }
    
    @Override
    public String toString() {
        return "Teacher{" + 
               "id=" + getId() + 
               ", name='" + getName() + "'" + 
               ", username='" + getUsername() + "'" + 
               ", department='" + department + "'" + 
               ", email='" + getEmail() + "'" + 
               ", phone='" + getPhone() + "'" + 
               '}';
    }
}