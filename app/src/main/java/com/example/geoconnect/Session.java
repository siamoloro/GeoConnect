package com.example.geoconnect;

public class Session {
    private final String id;
    private final String deviceName;
    private final String locationInfo;

    public Session(String id, String deviceName, String locationInfo) {
        this.id = id;
        this.deviceName = deviceName;
        this.locationInfo = locationInfo;
    }

    public String getId() { return id; }
    public String getDeviceName() { return deviceName; }
    public String getLocationInfo() { return locationInfo; }
}