package model;

public class Driver {
    private String driverId;
    private String name;
    private String phone;
    private String licenseNumber;
    private String assignedBus;
    private String shift; // Morning, Evening, Night
    
    public Driver(String driverId, String name, String phone, String licenseNumber, 
                  String assignedBus, String shift) {
        this.driverId = driverId;
        this.name = name;
        this.phone = phone;
        this.licenseNumber = licenseNumber;
        this.assignedBus = assignedBus;
        this.shift = shift;
    }
    
    // Getters
    public String getDriverId() { return driverId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getAssignedBus() { return assignedBus; }
    public String getShift() { return shift; }  // Returns String, not DriverShift enum
    
    // Setters
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public void setAssignedBus(String assignedBus) { this.assignedBus = assignedBus; }
    public void setShift(String shift) { this.shift = shift; }
    
    @Override
    public String toString() {
        return "Driver{driverId='" + driverId + "', name='" + name + 
               "', license='" + licenseNumber + "', shift='" + shift + 
               "', assignedBus='" + assignedBus + "'}";
    }
}