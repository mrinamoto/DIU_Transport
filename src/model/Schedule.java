package model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Schedule class represents bus schedule information
 */
public class Schedule {
    private String scheduleId;
    private String busId;
    private String routeId;
    private String departureTime;
    private String arrivalTime;
    private String daysOfWeek;
    
    public Schedule(String scheduleId, String busId, String routeId, String departureTime, String arrivalTime, String daysOfWeek) {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Schedule ID cannot be empty");
        }
        if (busId == null || busId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bus ID cannot be empty");
        }
        if (routeId == null || routeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be empty");
        }
        if (!isValidTimeFormat(departureTime)) {
            throw new IllegalArgumentException("Invalid departure time format. Use HH:mm");
        }
        if (!isValidTimeFormat(arrivalTime)) {
            throw new IllegalArgumentException("Invalid arrival time format. Use HH:mm");
        }
        if (daysOfWeek == null || daysOfWeek.trim().isEmpty()) {
            throw new IllegalArgumentException("Days of week cannot be empty");
        }
        this.scheduleId = scheduleId;
        this.busId = busId;
        this.routeId = routeId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.daysOfWeek = daysOfWeek;
    }
    
    // Getters
    public String getScheduleId() { return scheduleId; }
    public String getBusId() { return busId; }
    public String getRouteId() { return routeId; }
    public String getDepartureTime() { return departureTime; }
    public String getArrivalTime() { return arrivalTime; }
    public String getDaysOfWeek() { return daysOfWeek; }
    
    // Setters with validation
    public void setScheduleId(String scheduleId) {
        if (scheduleId == null || scheduleId.trim().isEmpty()) {
            throw new IllegalArgumentException("Schedule ID cannot be empty");
        }
        this.scheduleId = scheduleId;
    }
    
    public void setBusId(String busId) {
        if (busId == null || busId.trim().isEmpty()) {
            throw new IllegalArgumentException("Bus ID cannot be empty");
        }
        this.busId = busId;
    }
    
    public void setRouteId(String routeId) {
        if (routeId == null || routeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be empty");
        }
        this.routeId = routeId;
    }
    
    public void setDepartureTime(String departureTime) {
        if (!isValidTimeFormat(departureTime)) {
            throw new IllegalArgumentException("Invalid departure time format. Use HH:mm");
        }
        this.departureTime = departureTime;
    }
    
    public void setArrivalTime(String arrivalTime) {
        if (!isValidTimeFormat(arrivalTime)) {
            throw new IllegalArgumentException("Invalid arrival time format. Use HH:mm");
        }
        this.arrivalTime = arrivalTime;
    }
    
    public void setDaysOfWeek(String daysOfWeek) {
        if (daysOfWeek == null || daysOfWeek.trim().isEmpty()) {
            throw new IllegalArgumentException("Days of week cannot be empty");
        }
        this.daysOfWeek = daysOfWeek;
    }
    
    private boolean isValidTimeFormat(String time) {
        try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    @Override
    public String toString() {
        return "Schedule{" + "scheduleId=" + scheduleId + ", busId=" + busId + ", departureTime=" + departureTime + ", arrivalTime=" + arrivalTime + '}';
    }
}