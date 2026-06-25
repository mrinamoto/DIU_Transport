package services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Bus;
import model.enums.BusStatus;
import util.DatabaseConnection;

/**
 * BusService handles bus-related operations with database integration
 */
public class BusService {
    private final Connection connection;
    
    public BusService() {
        this.connection = DatabaseConnection.getConnection();
    }
    
    /**
     * Add a new bus (using basic constructor)
     */
    public boolean addBus(String busId, String busNumber, int capacity, BusStatus status, String route) {
        return addBus(new Bus(busId, busNumber, capacity, status.toString(), route));
    }
    
    /**
     * Add a new bus
     */
    public boolean addBus(Bus bus) {
        String sql = "INSERT INTO buses (bus_id, bus_number, capacity, status, route) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, bus.getBusId());
            pstmt.setString(2, bus.getBusNumber());
            pstmt.setInt(3, bus.getCapacity());
            pstmt.setString(4, bus.getStatus());
            pstmt.setString(5, bus.getRoute());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding bus: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all buses
     */
    public List<Bus> getAllBuses() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT * FROM buses ORDER BY bus_number";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Bus bus = createBusFromResultSet(rs);
                buses.add(bus);
            }
        } catch (SQLException e) {
            System.err.println("Error getting buses: " + e.getMessage());
        }
        return buses;
    }
    
    /**
     * Get available buses (not assigned to any route)
     */
    public List<Bus> getAvailableBuses() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT * FROM buses WHERE (route IS NULL OR route = '') AND status = 'Active' ORDER BY bus_number";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Bus bus = createBusFromResultSet(rs);
                buses.add(bus);
            }
        } catch (SQLException e) {
            System.err.println("Error getting available buses: " + e.getMessage());
        }
        return buses;
    }
    
    /**
     * Get active buses
     */
    public List<Bus> getActiveBuses() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT * FROM buses WHERE status = 'Active' ORDER BY bus_number";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Bus bus = createBusFromResultSet(rs);
                buses.add(bus);
            }
        } catch (SQLException e) {
            System.err.println("Error getting active buses: " + e.getMessage());
        }
        return buses;
    }
    
    /**
     * Get buses needing maintenance
     */
    public List<Bus> getBusesNeedingMaintenance() {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT * FROM buses WHERE status = 'Maintenance' ORDER BY bus_number";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Bus bus = createBusFromResultSet(rs);
                buses.add(bus);
            }
        } catch (SQLException e) {
            System.err.println("Error getting buses needing maintenance: " + e.getMessage());
        }
        return buses;
    }
    
    /**
     * Get bus by ID
     */
    public Bus getBusById(String busId) {
        String sql = "SELECT * FROM buses WHERE bus_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, busId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createBusFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting bus by ID: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Get bus by bus number
     */
    public Bus getBusByNumber(String busNumber) {
        String sql = "SELECT * FROM buses WHERE bus_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, busNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return createBusFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting bus by number: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Update bus information
     */
    public boolean updateBus(Bus bus) {
        String sql = "UPDATE buses SET bus_number = ?, capacity = ?, status = ?, route = ? WHERE bus_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, bus.getBusNumber());
            pstmt.setInt(2, bus.getCapacity());
            pstmt.setString(3, bus.getStatus());
            pstmt.setString(4, bus.getRoute());
            pstmt.setString(5, bus.getBusId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating bus: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update bus status
     */
    public boolean updateBusStatus(String busId, String status) {
        String sql = "UPDATE buses SET status = ? WHERE bus_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, busId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating bus status: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update bus status with BusStatus enum
     */
    public boolean updateBusStatus(String busId, BusStatus status) {
        return updateBusStatus(busId, status.toString());
    }
    
    /**
     * Assign bus to route
     */
    public boolean assignBusToRoute(String busId, String route) {
        String sql = "UPDATE buses SET route = ? WHERE bus_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, route);
            pstmt.setString(2, busId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error assigning bus to route: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Unassign bus from route
     */
    public boolean unassignBus(String busId) {
        String sql = "UPDATE buses SET route = NULL WHERE bus_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, busId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error unassigning bus: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a bus
     */
    public boolean deleteBus(String busId) {
        String sql = "DELETE FROM buses WHERE bus_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, busId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting bus: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get buses by status (String version)
     */
    public List<Bus> getBusesByStatus(String status) {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT * FROM buses WHERE status = ? ORDER BY bus_number";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Bus bus = createBusFromResultSet(rs);
                buses.add(bus);
            }
        } catch (SQLException e) {
            System.err.println("Error getting buses by status: " + e.getMessage());
        }
        return buses;
    }
    
    /**
     * Get buses by status (BusStatus enum version)
     */
    public List<Bus> getBusesByStatus(BusStatus status) {
        return getBusesByStatus(status.toString());
    }
    
    /**
     * Get buses by route
     */
    public List<Bus> getBusesByRoute(String route) {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT * FROM buses WHERE route = ? ORDER BY bus_number";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, route);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Bus bus = createBusFromResultSet(rs);
                buses.add(bus);
            }
        } catch (SQLException e) {
            System.err.println("Error getting buses by route: " + e.getMessage());
        }
        return buses;
    }
    
    /**
     * Get total bus count
     */
    public int getTotalBusCount() {
        String sql = "SELECT COUNT(*) FROM buses";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting bus count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get active bus count
     */
    public int getActiveBusCount() {
        String sql = "SELECT COUNT(*) FROM buses WHERE status = 'Active'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting active bus count: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Get total bus capacity
     */
    public int getTotalBusCapacity() {
        String sql = "SELECT SUM(capacity) FROM buses WHERE status = 'Active'";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total bus capacity: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Check if bus ID exists
     */
    public boolean busIdExists(String busId) {
        String sql = "SELECT COUNT(*) FROM buses WHERE bus_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, busId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking bus ID: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Check if bus number exists
     */
    public boolean busNumberExists(String busNumber) {
        String sql = "SELECT COUNT(*) FROM buses WHERE bus_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, busNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking bus number: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Generate next bus ID
     */
    public String generateNextBusId() {
        String sql = "SELECT MAX(bus_id) FROM buses";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith("B")) {
                    try {
                        int lastNumber = Integer.parseInt(lastId.substring(1));
                        return String.format("B%03d", lastNumber + 1);
                    } catch (NumberFormatException e) {
                        // If parsing fails, start from 1
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next bus ID: " + e.getMessage());
        }
        return "B001";
    }
    
    /**
     * Get bus statistics
     */
    public java.util.Map<String, Integer> getBusStatistics() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        
        String sql = "SELECT status, COUNT(*) as count FROM buses GROUP BY status";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stats.put(rs.getString("status"), rs.getInt("count"));
            }
            
            // Total buses
            sql = "SELECT COUNT(*) as total FROM buses";
            ResultSet totalRs = stmt.executeQuery(sql);
            if (totalRs.next()) {
                stats.put("TOTAL", totalRs.getInt("total"));
            }
            
            // Total capacity
            sql = "SELECT SUM(capacity) as total_capacity FROM buses";
            ResultSet capacityRs = stmt.executeQuery(sql);
            if (capacityRs.next()) {
                stats.put("TOTAL_CAPACITY", capacityRs.getInt("total_capacity"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting bus statistics: " + e.getMessage());
        }
        return stats;
    }
    
    /**
     * Search buses by bus number or route
     */
    public List<Bus> searchBuses(String keyword) {
        List<Bus> buses = new ArrayList<>();
        String sql = "SELECT * FROM buses WHERE bus_number LIKE ? OR route LIKE ? ORDER BY bus_number";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Bus bus = createBusFromResultSet(rs);
                buses.add(bus);
            }
        } catch (SQLException e) {
            System.err.println("Error searching buses: " + e.getMessage());
        }
        return buses;
    }
    
    /**
     * Get all bus routes (distinct)
     */
    public List<String> getAllRoutes() {
        List<String> routes = new ArrayList<>();
        String sql = "SELECT DISTINCT route FROM buses WHERE route IS NOT NULL AND route != '' ORDER BY route";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                routes.add(rs.getString("route"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting routes: " + e.getMessage());
        }
        return routes;
    }
    
    /**
     * Helper method to create Bus object from ResultSet
     */
    private Bus createBusFromResultSet(ResultSet rs) throws SQLException {
        String busId = rs.getString("bus_id");
        String busNumber = rs.getString("bus_number");
        int capacity = rs.getInt("capacity");
        String status = rs.getString("status");
        String route = rs.getString("route");
        
        // Check if route is null
        if (route == null) {
            route = "";
        }
        
        return new Bus(busId, busNumber, capacity, status, route);
    }
}