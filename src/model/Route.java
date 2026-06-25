package model;

import java.util.Arrays;
import java.util.List;

/**
 * Route class represents a bus route with stops and timing
 */
public class Route {
    private String routeId;
    private String routeName;
    private String stops; // Comma separated stops
    private String timing;
    
    public Route(String routeId, String routeName, String stops, String timing) {
        if (routeId == null || routeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be empty");
        }
        if (routeName == null || routeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Route name cannot be empty");
        }
        if (stops == null || stops.trim().isEmpty()) {
            throw new IllegalArgumentException("Stops cannot be empty");
        }
        if (timing == null || timing.trim().isEmpty()) {
            throw new IllegalArgumentException("Timing cannot be empty");
        }
        this.routeId = routeId;
        this.routeName = routeName;
        this.stops = stops;
        this.timing = timing;
    }
    
    // Getters
    public String getRouteId() { return routeId; }
    public String getRouteName() { return routeName; }
    public String getStops() { return stops; }
    public String getTiming() { return timing; }
    
    // Convenience method to get stops as list
    public List<String> getStopsAsList() {
        return Arrays.asList(stops.split(","));
    }
    
    // Setters with validation
    public void setRouteId(String routeId) {
        if (routeId == null || routeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Route ID cannot be empty");
        }
        this.routeId = routeId;
    }
    
    public void setRouteName(String routeName) {
        if (routeName == null || routeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Route name cannot be empty");
        }
        this.routeName = routeName;
    }
    
    public void setStops(String stops) {
        if (stops == null || stops.trim().isEmpty()) {
            throw new IllegalArgumentException("Stops cannot be empty");
        }
        this.stops = stops;
    }
    
    public void setTiming(String timing) {
        if (timing == null || timing.trim().isEmpty()) {
            throw new IllegalArgumentException("Timing cannot be empty");
        }
        this.timing = timing;
    }
    
    @Override
    public String toString() {
        return "Route{" + "routeId=" + routeId + ", routeName=" + routeName + ", stops=" + stops + ", timing=" + timing + '}';
    }
}