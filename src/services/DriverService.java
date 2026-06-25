package services;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Driver;
import util.DatabaseConnection;

/**
 * DriverService handles driver information with database integration
 */
public class DriverService {
    private final Connection connection;
    
    public DriverService() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    /**
     * Add a new driver
     */
    public boolean addDriver(Driver driver) {
        String sql = "INSERT INTO drivers (driver_id, name, phone, license_number, assigned_bus, shift) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, driver.getDriverId());
            pstmt.setString(2, driver.getName());
            pstmt.setString(3, driver.getPhone());
            pstmt.setString(4, driver.getLicenseNumber());
            pstmt.setString(5, driver.getAssignedBus());
            pstmt.setString(6, driver.getShift());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding driver: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Add a new driver with all details
     */
    public boolean addDriverFull(String driverId, String name, String phone, String licenseNumber, 
                                String assignedBus, String shift) {
        String sql = "INSERT INTO drivers (driver_id, name, phone, license_number, assigned_bus, shift) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, driverId);
            pstmt.setString(2, name);
            pstmt.setString(3, phone);
            pstmt.setString(4, licenseNumber);
            pstmt.setString(5, assignedBus);
            pstmt.setString(6, shift);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding driver: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all drivers
     */
    public List<Driver> getAllDrivers() {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT * FROM drivers ORDER BY name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Driver driver = createDriverFromResultSet(rs);
                drivers.add(driver);
            }
        } catch (SQLException e) {
            System.err.println("Error getting drivers: " + e.getMessage());
        }
        return drivers;
    }
    
    /**
     * Get available drivers (not assigned to any bus)
     */
    public List<Driver> getAvailableDrivers() {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT * FROM drivers WHERE assigned_bus IS NULL OR assigned_bus = '' ORDER BY name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Driver driver = createDriverFromResultSet(rs);
                drivers.add(driver);
            }
        } catch (SQLException e) {
            System.err.println("Error getting available drivers: " + e.getMessage());
        }
        return drivers;
    }
    
    /**
     * Get assigned drivers
     */
    public List<Driver> getAssignedDrivers() {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT * FROM drivers WHERE assigned_bus IS NOT NULL AND assigned_bus != '' ORDER BY name";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Driver driver = createDriverFromResultSet(rs);
                drivers.add(driver);
            }
        } catch (SQLException e) {
            System.err.println("Error getting assigned drivers: " + e.getMessage());
        }
        return drivers;
    }
    
    /**
     * Find driver by ID
     */
    public Driver findDriverById(String driverId) {
        String sql = "SELECT * FROM drivers WHERE driver_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, driverId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createDriverFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding driver by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Find driver by name
     */
    public List<Driver> findDriversByName(String name) {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT * FROM drivers WHERE name LIKE ? ORDER BY name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, "%" + name + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Driver driver = createDriverFromResultSet(rs);
                drivers.add(driver);
            }
        } catch (SQLException e) {
            System.err.println("Error finding drivers by name: " + e.getMessage());
        }
        return drivers;
    }
    
    /**
     * Find driver by license number
     */
    public Driver findDriverByLicense(String licenseNumber) {
        String sql = "SELECT * FROM drivers WHERE license_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, licenseNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createDriverFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error finding driver by license: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Update driver information
     */
    public boolean updateDriver(Driver driver) {
        String sql = "UPDATE drivers SET name = ?, phone = ?, license_number = ?, " +
                    "assigned_bus = ?, shift = ? WHERE driver_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, driver.getName());
            pstmt.setString(2, driver.getPhone());
            pstmt.setString(3, driver.getLicenseNumber());
            pstmt.setString(4, driver.getAssignedBus());
            pstmt.setString(5, driver.getShift());
            pstmt.setString(6, driver.getDriverId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating driver: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update driver basic information
     */
    public boolean updateDriverInfo(String driverId, String name, String phone, String licenseNumber, String shift) {
        String sql = "UPDATE drivers SET name = ?, phone = ?, license_number = ?, shift = ? WHERE driver_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, licenseNumber);
            pstmt.setString(4, shift);
            pstmt.setString(5, driverId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating driver info: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a driver
     */
    public boolean deleteDriver(String driverId) {
        String sql = "DELETE FROM drivers WHERE driver_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, driverId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting driver: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Assign driver to bus
     */
    public boolean assignDriverToBus(String driverId, String busId) {
        String sql = "UPDATE drivers SET assigned_bus = ? WHERE driver_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, busId);
            pstmt.setString(2, driverId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error assigning driver to bus: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Unassign driver from bus
     */
    public boolean unassignDriver(String driverId) {
        String sql = "UPDATE drivers SET assigned_bus = NULL WHERE driver_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, driverId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error unassigning driver: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get drivers by shift
     */
    public List<Driver> getDriversByShift(String shift) {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT * FROM drivers WHERE shift = ? ORDER BY name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, shift);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Driver driver = createDriverFromResultSet(rs);
                drivers.add(driver);
            }
        } catch (SQLException e) {
            System.err.println("Error getting drivers by shift: " + e.getMessage());
        }
        return drivers;
    }
    
    /**
     * Get drivers assigned to a specific bus
     */
    public Driver getDriverByBus(String busId) {
        String sql = "SELECT * FROM drivers WHERE assigned_bus = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, busId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createDriverFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting driver by bus: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Check if driver license is valid (not expired)
     * Note: This requires license_expiry field in database
     */
    public boolean isLicenseValid(String driverId) {
        String sql = "SELECT license_expiry FROM drivers WHERE driver_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, driverId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String expiryDate = rs.getString("license_expiry");
                if (expiryDate != null && !expiryDate.isEmpty()) {
                    try {
                        LocalDate expiry = LocalDate.parse(expiryDate);
                        return LocalDate.now().isBefore(expiry) || LocalDate.now().isEqual(expiry);
                    } catch (java.time.format.DateTimeParseException e) {
                        // If date parsing fails, assume license is valid
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking license validity: " + e.getMessage());
        }
        return true; // If no expiry date, assume valid
    }
    
    /**
     * Get drivers with expired licenses
     */
    public List<Driver> getDriversWithExpiredLicenses() {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT * FROM drivers WHERE license_expiry IS NOT NULL AND license_expiry != ''";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String expiryDate = rs.getString("license_expiry");
                try {
                    LocalDate expiry = LocalDate.parse(expiryDate);
                    if (LocalDate.now().isAfter(expiry)) {
                        Driver driver = createDriverFromResultSet(rs);
                        drivers.add(driver);
                    }
                } catch (java.time.format.DateTimeParseException | SQLException e) {
                    // Skip if date parsing fails
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting drivers with expired licenses: " + e.getMessage());
        }
        return drivers;
    }
    
    /**
     * Update driver license expiry
     */
    public boolean updateLicenseExpiry(String driverId, String expiryDate) {
        String sql = "UPDATE drivers SET license_expiry = ? WHERE driver_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, expiryDate);
            pstmt.setString(2, driverId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating license expiry: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update driver experience years
     */
    public boolean updateExperienceYears(String driverId, int years) {
        String sql = "UPDATE drivers SET experience_years = ? WHERE driver_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, years);
            pstmt.setString(2, driverId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating experience years: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get total driver count
     */
    public int getTotalDriverCount() {
        String sql = "SELECT COUNT(*) FROM drivers";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting driver count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get assigned driver count
     */
    public int getAssignedDriverCount() {
        String sql = "SELECT COUNT(*) FROM drivers WHERE assigned_bus IS NOT NULL AND assigned_bus != ''";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting assigned driver count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Check if driver ID exists
     */
    public boolean driverIdExists(String driverId) {
        String sql = "SELECT COUNT(*) FROM drivers WHERE driver_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, driverId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking driver ID: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Check if license number exists
     */
    public boolean licenseNumberExists(String licenseNumber) {
        String sql = "SELECT COUNT(*) FROM drivers WHERE license_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, licenseNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking license number: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Generate next driver ID
     */
    public String generateNextDriverId() {
        String sql = "SELECT MAX(driver_id) FROM drivers";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith("D")) {
                    try {
                        int lastNumber = Integer.parseInt(lastId.substring(1));
                        return String.format("D%03d", lastNumber + 1);
                    } catch (NumberFormatException e) {
                        // If parsing fails, start from 1
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next driver ID: " + e.getMessage());
        }
        return "D001";
    }
    
    /**
     * Get driver statistics
     */
    public java.util.Map<String, Integer> getDriverStatistics() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        
        String sql = "SELECT shift, COUNT(*) as count FROM drivers GROUP BY shift";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stats.put(rs.getString("shift"), rs.getInt("count"));
            }
            
            // Total drivers
            sql = "SELECT COUNT(*) as total FROM drivers";
            ResultSet totalRs = stmt.executeQuery(sql);
            if (totalRs.next()) {
                stats.put("TOTAL", totalRs.getInt("total"));
            }
            
            // Assigned drivers
            sql = "SELECT COUNT(*) as assigned FROM drivers WHERE assigned_bus IS NOT NULL AND assigned_bus != ''";
            ResultSet assignedRs = stmt.executeQuery(sql);
            if (assignedRs.next()) {
                stats.put("ASSIGNED", assignedRs.getInt("assigned"));
            }
            
            // Available drivers
            sql = "SELECT COUNT(*) as available FROM drivers WHERE assigned_bus IS NULL OR assigned_bus = ''";
            ResultSet availableRs = stmt.executeQuery(sql);
            if (availableRs.next()) {
                stats.put("AVAILABLE", availableRs.getInt("available"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting driver statistics: " + e.getMessage());
        }
        return stats;
    }
    
    /**
     * Search drivers by name or license number
     */
    public List<Driver> searchDrivers(String keyword) {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT * FROM drivers WHERE name LIKE ? OR license_number LIKE ? ORDER BY name";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Driver driver = createDriverFromResultSet(rs);
                drivers.add(driver);
            }
        } catch (SQLException e) {
            System.err.println("Error searching drivers: " + e.getMessage());
        }
        return drivers;
    }
    
    /**
     * Get all shifts (distinct)
     */
    public List<String> getAllShifts() {
        List<String> shifts = new ArrayList<>();
        String sql = "SELECT DISTINCT shift FROM drivers WHERE shift IS NOT NULL AND shift != '' ORDER BY shift";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                shifts.add(rs.getString("shift"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting shifts: " + e.getMessage());
        }
        return shifts;
    }
    
    /**
     * Helper method to create Driver object from ResultSet
     */
    private Driver createDriverFromResultSet(ResultSet rs) throws SQLException {
        String driverId = rs.getString("driver_id");
        String name = rs.getString("name");
        String phone = rs.getString("phone");
        String licenseNumber = rs.getString("license_number");
        String assignedBus = rs.getString("assigned_bus");
        String shift = rs.getString("shift");
        
        // Check if assignedBus is null
        if (assignedBus == null) {
            assignedBus = "";
        }
        
        // Check if shift is null
        if (shift == null) {
            shift = "Morning"; // Default shift
        }
        
        return new Driver(driverId, name, phone, licenseNumber, assignedBus, shift);
    }
}