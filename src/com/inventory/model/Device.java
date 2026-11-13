package com.inventory.model;

public class Device {
    private int id;
    private String deviceCode;
    private String deviceName;
    private String deviceType;
    private String ipAddress;
    private String macAddress;
    private String manufacturer;
    private String model;
    private String location;
    private String assignedUser;
    private String purchaseDate;
    private String warrantyExpiry;
    private String status;
    private String notes;

    public Device() {}

    public Device(int id, String deviceCode, String deviceName, String deviceType,
                  String ipAddress, String macAddress, String manufacturer,
                  String model, String location, String assignedUser,
                  String purchaseDate, String warrantyExpiry, String status, String notes) {
        this.id = id;
        this.deviceCode = deviceCode;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
        this.manufacturer = manufacturer;
        this.model = model;
        this.location = location;
        this.assignedUser = assignedUser;
        this.purchaseDate = purchaseDate;
        this.warrantyExpiry = warrantyExpiry;
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDeviceCode() { return deviceCode; }
    public void setDeviceCode(String deviceCode) { this.deviceCode = deviceCode; }

    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAssignedUser() { return assignedUser; }
    public void setAssignedUser(String assignedUser) { this.assignedUser = assignedUser; }

    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getWarrantyExpiry() { return warrantyExpiry; }
    public void setWarrantyExpiry(String warrantyExpiry) { this.warrantyExpiry = warrantyExpiry; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return deviceName + " (" + deviceCode + " - " + ipAddress + ")";
    }
}

