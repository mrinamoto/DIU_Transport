package model;

import model.enums.UserRole;

/**
 * Student class - extends User class
 * Students have additional properties like department and semester
 */
public class Student extends User {
    private String department;
    private int semester;
    
    // Constructor with String department and int semester
    public Student(int id, String username, String password, String name, 
                   String email, String phone, String department, int semester) {
        super(id, username, password, UserRole.STUDENT, name, email, phone);
        this.department = department;
        this.semester = semester;
    }
    
    // Alternative constructor for loading from database
    public Student(int id, String username, String password, UserRole role,
                   String name, String email, String phone, String department, int semester) {
        super(id, username, password, role, name, email, phone);
        this.department = department;
        this.semester = semester;
    }
    
    // Constructor with String role for compatibility
    public Student(int id, String username, String password, String role,
                   String name, String email, String phone, String department, int semester) {
        super(id, username, password, role, name, email, phone);
        this.department = department;
        this.semester = semester;
    }
    
    // Getters and Setters for student-specific properties
    public String getDepartment() { 
        return department; 
    }
    
    public void setDepartment(String department) { 
        this.department = department; 
    }
    
    public int getSemester() { 
        return semester; 
    }
    
    public void setSemester(int semester) { 
        if (semester >= 1 && semester <= 12) {
            this.semester = semester;
        } else {
            throw new IllegalArgumentException("Semester must be between 1 and 12");
        }
    }
    
    @Override
    public String toString() {
        return "Student{" + 
               "id=" + getId() + 
               ", name='" + getName() + "'" + 
               ", username='" + getUsername() + "'" + 
               ", department='" + department + "'" + 
               ", semester=" + semester + 
               ", email='" + getEmail() + "'" + 
               ", phone='" + getPhone() + "'" + 
               '}';
    }
}