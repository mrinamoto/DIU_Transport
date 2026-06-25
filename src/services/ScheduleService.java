package services;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import model.Schedule;
import util.DatabaseConnection;

/**
 * ScheduleService handles bus schedule operations with database integration
 */
public class ScheduleService {
    private final Connection connection; // Made final as suggested
   
    public ScheduleService() {
        this.connection = DatabaseConnection.getConnection();
    }
   
    /**
     * Add a new schedule
     */
    public boolean addSchedule(Schedule schedule) {
        String sql = "INSERT INTO schedules (schedule_id, bus_id, route_id, departure_time, arrival_time, days_of_week, is_active) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, schedule.getScheduleId());
            pstmt.setString(2, schedule.getBusId());
            pstmt.setString(3, schedule.getRouteId());
            pstmt.setString(4, schedule.getDepartureTime());
            pstmt.setString(5, schedule.getArrivalTime());
            pstmt.setString(6, schedule.getDaysOfWeek());
            pstmt.setInt(7, 1); // Default active
           
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding schedule: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Get all schedules
     */
    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules ORDER BY departure_time";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
                Schedule schedule = new Schedule(
                    rs.getString("schedule_id"),
                    rs.getString("bus_id"),
                    rs.getString("route_id"),
                    rs.getString("departure_time"),
                    rs.getString("arrival_time"),
                    rs.getString("days_of_week")
                );
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all schedules: " + e.getMessage());
        }
        return schedules;
    }
   
    /**
     * Get active schedules only
     */
    public List<Schedule> getActiveSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE is_active = 1 ORDER BY departure_time";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
                Schedule schedule = new Schedule(
                    rs.getString("schedule_id"),
                    rs.getString("bus_id"),
                    rs.getString("route_id"),
                    rs.getString("departure_time"),
                    rs.getString("arrival_time"),
                    rs.getString("days_of_week")
                );
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("Error getting active schedules: " + e.getMessage());
        }
        return schedules;
    }
   
    /**
     * Get schedules for a specific bus
     */
    public List<Schedule> getSchedulesByBusId(String busId) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE bus_id = ? AND is_active = 1 ORDER BY departure_time";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, busId);
            ResultSet rs = pstmt.executeQuery();
           
            while (rs.next()) {
                Schedule schedule = new Schedule(
                    rs.getString("schedule_id"),
                    rs.getString("bus_id"),
                    rs.getString("route_id"),
                    rs.getString("departure_time"),
                    rs.getString("arrival_time"),
                    rs.getString("days_of_week")
                );
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("Error getting schedules for bus " + busId + ": " + e.getMessage());
        }
        return schedules;
    }
   
    /**
     * Get schedules for a specific route
     */
    public List<Schedule> getSchedulesByRouteId(String routeId) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE route_id = ? AND is_active = 1 ORDER BY departure_time";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, routeId);
            ResultSet rs = pstmt.executeQuery();
           
            while (rs.next()) {
                Schedule schedule = new Schedule(
                    rs.getString("schedule_id"),
                    rs.getString("bus_id"),
                    rs.getString("route_id"),
                    rs.getString("departure_time"),
                    rs.getString("arrival_time"),
                    rs.getString("days_of_week")
                );
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("Error getting schedules for route " + routeId + ": " + e.getMessage());
        }
        return schedules;
    }
   
    /**
     * Find schedule by ID
     */
    public Schedule findScheduleById(String scheduleId) {
        String sql = "SELECT * FROM schedules WHERE schedule_id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, scheduleId);
            ResultSet rs = pstmt.executeQuery();
           
            if (rs.next()) {
                return new Schedule(
                    rs.getString("schedule_id"),
                    rs.getString("bus_id"),
                    rs.getString("route_id"),
                    rs.getString("departure_time"),
                    rs.getString("arrival_time"),
                    rs.getString("days_of_week")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error finding schedule by ID: " + e.getMessage());
        }
        return null;
    }
   
    /**
     * Update a schedule
     */
    public boolean updateSchedule(Schedule schedule) {
        String sql = "UPDATE schedules SET bus_id = ?, route_id = ?, departure_time = ?, " +
                    "arrival_time = ?, days_of_week = ? WHERE schedule_id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, schedule.getBusId());
            pstmt.setString(2, schedule.getRouteId());
            pstmt.setString(3, schedule.getDepartureTime());
            pstmt.setString(4, schedule.getArrivalTime());
            pstmt.setString(5, schedule.getDaysOfWeek());
            pstmt.setString(6, schedule.getScheduleId());
           
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating schedule: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Delete a schedule
     */
    public boolean deleteSchedule(String scheduleId) {
        String sql = "DELETE FROM schedules WHERE schedule_id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, scheduleId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting schedule: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Deactivate a schedule (soft delete)
     */
    public boolean deactivateSchedule(String scheduleId) {
        String sql = "UPDATE schedules SET is_active = 0 WHERE schedule_id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, scheduleId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deactivating schedule: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Activate a schedule
     */
    public boolean activateSchedule(String scheduleId) {
        String sql = "UPDATE schedules SET is_active = 1 WHERE schedule_id = ?";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, scheduleId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error activating schedule: " + e.getMessage());
            return false;
        }
    }
   
    /**
     * Get schedules for today
     */
    public List<Schedule> getTodaySchedules() {
        List<Schedule> schedules = new ArrayList<>();
       
        // Get current day of week (Monday=1, Sunday=7 in SQLite)
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        String dayName = today.toString().substring(0, 3); // "MON", "TUE", etc.
       
        String sql = "SELECT * FROM schedules WHERE is_active = 1 AND " +
                    "(days_of_week LIKE '%" + dayName + "%' OR days_of_week LIKE '%Daily%' OR " +
                    "days_of_week LIKE '%Weekdays%' OR days_of_week LIKE '%" + getDayRange(today) + "%') " +
                    "ORDER BY departure_time";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            while (rs.next()) {
                Schedule schedule = new Schedule(
                    rs.getString("schedule_id"),
                    rs.getString("bus_id"),
                    rs.getString("route_id"),
                    rs.getString("departure_time"),
                    rs.getString("arrival_time"),
                    rs.getString("days_of_week")
                );
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("Error getting today's schedules: " + e.getMessage());
        }
        return schedules;
    }
   
    /**
     * Get upcoming schedules (next 2 hours)
     */
    public List<Schedule> getUpcomingSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        LocalTime now = LocalTime.now();
        LocalTime twoHoursLater = now.plusHours(2);
       
        String sql = "SELECT * FROM schedules WHERE is_active = 1 AND " +
                    "TIME(departure_time) BETWEEN TIME(?) AND TIME(?) " +
                    "ORDER BY departure_time";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, now.toString());
            pstmt.setString(2, twoHoursLater.toString());
            ResultSet rs = pstmt.executeQuery();
           
            while (rs.next()) {
                Schedule schedule = new Schedule(
                    rs.getString("schedule_id"),
                    rs.getString("bus_id"),
                    rs.getString("route_id"),
                    rs.getString("departure_time"),
                    rs.getString("arrival_time"),
                    rs.getString("days_of_week")
                );
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("Error getting upcoming schedules: " + e.getMessage());
        }
        return schedules;
    }
   
    /**
     * Helper method to get day range - Converted to switch expression
     */
    private String getDayRange(DayOfWeek day) {
        return switch (day) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Mon-Fri";
            case SATURDAY, SUNDAY -> "Sat-Sun";
        };
    }
   
    /**
     * Get total schedule count
     */
    public int getTotalScheduleCount() {
        String sql = "SELECT COUNT(*) FROM schedules";
       
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
           
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting schedule count: " + e.getMessage());
        }
        return 0;
    }
   
    /**
     * Check if bus has conflicting schedule
     */
    public boolean hasConflictingSchedule(String busId, String departureTime, String arrivalTime, String days) {
        String sql = "SELECT COUNT(*) FROM schedules WHERE bus_id = ? AND days_of_week = ? AND " +
                    "((TIME(departure_time) <= TIME(?) AND TIME(arrival_time) >= TIME(?)) OR " +
                    "(TIME(departure_time) <= TIME(?) AND TIME(arrival_time) >= TIME(?)))";
       
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, busId);
            pstmt.setString(2, days);
            pstmt.setString(3, arrivalTime);
            pstmt.setString(4, departureTime);
            pstmt.setString(5, departureTime);
            pstmt.setString(6, arrivalTime);
           
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking conflicting schedule: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Get schedule count by status
     */
    public int getScheduleCountByStatus(boolean isActive) {
        String sql = "SELECT COUNT(*) FROM schedules WHERE is_active = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, isActive ? 1 : 0);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting schedule count by status: " + e.getMessage());
        }
        return 0;
    }
    
    /**
     * Check if schedule ID exists
     */
    public boolean scheduleIdExists(String scheduleId) {
        String sql = "SELECT COUNT(*) FROM schedules WHERE schedule_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, scheduleId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking schedule ID: " + e.getMessage());
        }
        return false;
    }
    
    /**
     * Generate next schedule ID
     */
    public String generateNextScheduleId() {
        String sql = "SELECT MAX(schedule_id) FROM schedules";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String lastId = rs.getString(1);
                if (lastId != null && lastId.startsWith("SCH")) {
                    try {
                        int lastNumber = Integer.parseInt(lastId.substring(3));
                        return String.format("SCH%03d", lastNumber + 1);
                    } catch (NumberFormatException e) {
                        // If parsing fails, start from 1
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error generating next schedule ID: " + e.getMessage());
        }
        return "SCH001";
    }
    
    /**
     * Search schedules by bus ID, route ID, or days
     */
    public List<Schedule> searchSchedules(String keyword) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedules WHERE bus_id LIKE ? OR route_id LIKE ? OR days_of_week LIKE ? ORDER BY departure_time";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Schedule schedule = new Schedule(
                    rs.getString("schedule_id"),
                    rs.getString("bus_id"),
                    rs.getString("route_id"),
                    rs.getString("departure_time"),
                    rs.getString("arrival_time"),
                    rs.getString("days_of_week")
                );
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            System.err.println("Error searching schedules: " + e.getMessage());
        }
        return schedules;
    }
}