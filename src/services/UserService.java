package services;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import model.*;
import model.enums.UserRole;
import util.DatabaseConnection;

/**
 * UserService handles all user-related operations with database integration
 */
public class UserService {
    private final Connection connection; // Made final as suggested
   
    public UserService() {
        this.connection = DatabaseConnection.getConnection();
        initializeAdminUser();
    }
   
    /**
     * Ensure default admin user exists
     */
    private void initializeAdminUser() {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        String insertSql = "INSERT INTO users (username, password, role, name, email, phone, department, semester) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
           
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                    pstmt.setString(1, "admin");
                    pstmt.setString(2, "admin123"); // Default password
                    pstmt.setString(3, "Admin");
                    pstmt.setString(4, "System Administrator");
                    pstmt.setString(5, "admin@diu.edu.bd");
                    pstmt.setString(6, "01700000000");
                    pstmt.setString(7, "Administration");
                    pstmt.setInt(8, 0);
                   
                    pstmt.executeUpdate();
                    System.out.println("Default admin user created.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error initializing admin user: " + e.getMessage());
        }
    }
   
    /**
     * Login user with username and password
     */
    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, use hashed passwords!
            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()) {
                // Update last login time
                updateLastLogin(username);
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }
   
    /**
     * Register a new user
     */
    public boolean register(User user) {
        // Validate user data
        if (!validateUserForRegistration(user)) {
            return false;
        }
       
        String sql = "INSERT INTO users (username, password, role, name, email, phone, department, semester, position) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole().toString());
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getEmail());
            pstmt.setString(6, user.getPhone());
           
            // Set additional fields based on user type using pattern matching
            String department = "";
            int semester = 0;
            String position = "";
           
            if (user instanceof Student student) { // Pattern matching
                department = student.getDepartment();
                semester = student.getSemester();
                position = "Student";
            } else if (user instanceof Teacher teacher) { // Pattern matching
                department = teacher.getDepartment();
                position = "Faculty";
            } else if (user instanceof Staff staff) { // Pattern matching
                // If Staff class doesn't have getDepartment(), use position or empty string
                department = "";
                position = staff.getPosition();
            } else if (user instanceof Admin) {
                department = "Administration";
                position = "Administrator";
            }
           
            pstmt.setString(7, department);
            pstmt.setInt(8, semester);
            pstmt.setString(9, position);
           
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
           
        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("Username or email already exists!");
            }
            return false;
        }
    }
   
    /**
     * Update user profile
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE users SET name = ?, email = ?, phone = ?, department = ?, semester = ?, position = ? " +
                    "WHERE id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPhone());
           
            // Set additional fields based on user type using pattern matching
            String department = "";
            int semester = 0;
            String position = "";
           
            if (user instanceof Student student) { // Pattern matching
                department = student.getDepartment();
                semester = student.getSemester();
                position = "Student";
            } else if (user instanceof Teacher teacher) { // Pattern matching
                department = teacher.getDepartment();
                position = "Faculty";
            } else if (user instanceof Staff staff) { // Pattern matching
                // If Staff class doesn't have getDepartment(), use position or empty string
                department = "";
                position = staff.getPosition();
            } else if (user instanceof Admin) {
                department = "Administration";
                position = "Administrator";
            }
           
            pstmt.setString(4, department);
            pstmt.setInt(5, semester);
            pstmt.setString(6, position);
            pstmt.setInt(7, user.getId());
           
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
           
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Change user password
     */
    public boolean changePassword(int userId, String currentPassword, String newPassword) {
        // First verify current password
        String verifySql = "SELECT COUNT(*) FROM users WHERE id = ? AND password = ?";
       
        try (PreparedStatement verifyStmt = connection.prepareStatement(verifySql)) {
            verifyStmt.setInt(1, userId);
            verifyStmt.setString(2, currentPassword);
            ResultSet rs = verifyStmt.executeQuery();
           
            if (rs.next() && rs.getInt(1) == 1) {
                // Current password is correct, update to new password
                String updateSql = "UPDATE users SET password = ? WHERE id = ?";
                try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                    updateStmt.setString(1, newPassword);
                    updateStmt.setInt(2, userId);
                   
                    int rowsAffected = updateStmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
        }
        return false;
    }
   
    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
        }
        return null;
    }
   
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()) {
                return createUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by username: " + e.getMessage());
        }
        return null;
    }
   
    /**
     * Get all users (for admin)
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY role, name";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        return users;
    }
   
    /**
     * Get users by role
     */
    public List<User> getUsersByRole(UserRole role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = ? ORDER BY name";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, role.toString());
            ResultSet rs = pstmt.executeQuery();
           
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting users by role: " + e.getMessage());
        }
        return users;
    }
   
    /**
     * Get all students
     */
    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'Student' ORDER BY department, semester, name";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
                students.add(new Student(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department"),
                    rs.getInt("semester")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all students: " + e.getMessage());
        }
        return students;
    }
   
    /**
     * Get all teachers
     */
    public List<Teacher> getAllTeachers() {
        List<Teacher> teachers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'Teacher' ORDER BY department, name";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
                teachers.add(new Teacher(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("department")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all teachers: " + e.getMessage());
        }
        return teachers;
    }
   
    /**
     * Get all staff
     */
    public List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'Staff' ORDER BY name";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
                staffList.add(new Staff(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("position")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all staff: " + e.getMessage());
        }
        return staffList;
    }
    
    /**
     * Get all admins
     */
    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'Admin' ORDER BY name";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
                admins.add(new Admin(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all admins: " + e.getMessage());
        }
        return admins;
    }
    
    /**
     * Search users by name or username
     */
    public List<User> searchUsers(String keyword) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE name LIKE ? OR username LIKE ? OR email LIKE ? ORDER BY name";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
           
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                users.add(createUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error searching users: " + e.getMessage());
        }
        return users;
    }
   
    /**
     * Delete a user
     */
    public boolean deleteUser(int userId) {
        // Check if user exists
        User user = getUserById(userId);
        if (user == null) {
            return false;
        }
       
        // Don't allow deleting admin users
        if (user.getRole() == UserRole.ADMIN) {
            System.err.println("Cannot delete admin users!");
            return false;
        }
       
        String sql = "DELETE FROM users WHERE id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Get user statistics
     */
    public java.util.Map<String, Integer> getUserStatistics() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
       
        String sql = "SELECT role, COUNT(*) as count FROM users GROUP BY role";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
                stats.put(rs.getString("role"), rs.getInt("count"));
            }
           
            // Total users
            sql = "SELECT COUNT(*) as total FROM users";
            ResultSet totalRs = stmt.executeQuery(sql);
            if (totalRs.next()) {
                stats.put("TOTAL", totalRs.getInt("total"));
            }
           
        } catch (SQLException e) {
            System.err.println("Error getting user statistics: " + e.getMessage());
        }
        return stats;
    }
   
    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking username: " + e.getMessage());
        }
        return false;
    }
   
    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking email: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Check if phone exists
     */
    public boolean phoneExists(String phone) {
        String sql = "SELECT COUNT(*) FROM users WHERE phone = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking phone: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Update user role
     */
    public boolean updateUserRole(int userId, UserRole newRole) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newRole.toString());
            pstmt.setInt(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user role: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get user count by role
     */
    public int getUserCountByRole(UserRole role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, role.toString());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting user count by role: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get total user count
     */
    public int getTotalUserCount() {
        String sql = "SELECT COUNT(*) FROM users";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total user count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Reset user password (admin function)
     */
    public boolean resetPassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Helper method to create User object from ResultSet
     */
    private User createUserFromResultSet(ResultSet rs) throws SQLException {
        UserRole role = UserRole.valueOf(rs.getString("role"));
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String department = rs.getString("department");
        int semester = rs.getInt("semester");
        String position = rs.getString("position");
        
        // Converted to switch expression with default case
        return switch (role) {
            case STUDENT -> new Student(id, username, password, name, email, phone, department, semester);
            case TEACHER -> new Teacher(id, username, password, name, email, phone, department);
            case STAFF -> new Staff(id, username, password, name, email, phone, position);
            case ADMIN -> new Admin(id, username, password, name, email, phone);
            default -> throw new IllegalArgumentException("Unknown user role: " + role);
        };
    }
   
    /**
     * Helper method to update last login time
     */
    private void updateLastLogin(String username) {
        String sql = "UPDATE users SET updated_at = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            pstmt.setString(1, currentTime);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }
    
    /**
     * Validate user data for registration
     */
    private boolean validateUserForRegistration(User user) {
        // Check username
        if (user.getUsername() == null || user.getUsername().trim().isEmpty() || user.getUsername().length() < 3) {
            System.err.println("Invalid username format! Username must be at least 3 characters long.");
            return false;
        }
        
        // Check password
        if (user.getPassword() == null || user.getPassword().trim().isEmpty() || user.getPassword().length() < 6) {
            System.err.println("Invalid password format! Password must be at least 6 characters long.");
            return false;
        }
        
        // Check email
        if (user.getEmail() == null || !isValidEmail(user.getEmail())) {
            System.err.println("Invalid email format!");
            return false;
        }
        
        // Check phone
        if (user.getPhone() == null || !isValidPhone(user.getPhone())) {
            System.err.println("Invalid phone format!");
            return false;
        }
        
        // Check name
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            System.err.println("Name is required!");
            return false;
        }
        
        // Check if username already exists
        if (usernameExists(user.getUsername())) {
            System.err.println("Username already exists!");
            return false;
        }
        
        // Check if email already exists
        if (emailExists(user.getEmail())) {
            System.err.println("Email already exists!");
            return false;
        }
        
        // Check if phone already exists
        if (phoneExists(user.getPhone())) {
            System.err.println("Phone number already exists!");
            return false;
        }
        
        return true;
    }
    
    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Simple phone validation
     */
    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^[0-9]{11}$");
    }
}