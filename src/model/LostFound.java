package model;

public class LostFound {
    private String itemId;
    private String itemName;
    private String description;
    private String foundLocation;
    private String foundDate;
    private String status; // Claimed, Unclaimed
    
    public LostFound(String itemId, String itemName, String description, 
                    String foundLocation, String foundDate, String status) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.description = description;
        this.foundLocation = foundLocation;
        this.foundDate = foundDate;
        this.status = status;
    }
    
    // Getters
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public String getDescription() { return description; }
    public String getFoundLocation() { return foundLocation; }
    public String getFoundDate() { return foundDate; }
    public String getStatus() { return status; } // Returns String, not LostFoundStatus enum
    
    // Setters
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setDescription(String description) { this.description = description; }
    public void setFoundLocation(String foundLocation) { this.foundLocation = foundLocation; }
    public void setFoundDate(String foundDate) { this.foundDate = foundDate; }
    public void setStatus(String status) { this.status = status; }
    
    @Override
    public String toString() {
        return "LostFound{itemId='" + itemId + "', itemName='" + itemName + 
               "', foundLocation='" + foundLocation + "', status='" + status + 
               "', foundDate='" + foundDate + "'}";
    }
}