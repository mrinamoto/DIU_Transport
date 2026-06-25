package model;

public class Bus {
    private String busId;
    private String busNumber;
    private int capacity;
    private String status; // Active, Maintenance
    private String route;
    
    public Bus(String busId, String busNumber, int capacity, String status, String route) {
        this.busId = busId;
        this.busNumber = busNumber;
        this.capacity = capacity;
        this.status = status;
        this.route = route;
    }
    
    // Getters
    public String getBusId() { return busId; }
    public String getBusNumber() { return busNumber; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }  // Returns String
    public String getRoute() { return route; }    // This is getRoute(), not getRouteId()
    
    // Setters
    public void setBusId(String busId) { this.busId = busId; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setStatus(String status) { this.status = status; }
    public void setRoute(String route) { this.route = route; }
    
    @Override
    public String toString() {
        return "Bus{busId='" + busId + "', busNumber='" + busNumber + 
               "', capacity=" + capacity + ", status='" + status + 
               "', route='" + route + "'}";
    }
}