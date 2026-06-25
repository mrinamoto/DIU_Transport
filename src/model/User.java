package model;

import model.enums.UserRole;

public class User {
    private int id;
    private String name;
    private String username;
    private String password;
    private UserRole role;
    private String email;
    private String phone;

    // Constructor with UserRole enum
    public User(int id, String username, String password, UserRole role, 
                String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
        this.phone = phone;
    }

    // Alternative constructor with String role (for compatibility)
    public User(int id, String username, String password, String role, 
                String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
        this.role = UserRole.fromString(role); // Convert string to enum
        this.email = email;
        this.phone = phone;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public UserRole getRole() { return role; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(UserRole role) { this.role = role; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', username='" + username + 
               "', role=" + role + "}";
    }
}