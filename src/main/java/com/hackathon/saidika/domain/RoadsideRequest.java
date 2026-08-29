package com.hackathon.saidika.domain;

public class RoadsideRequest {

    private final String requestText;
    private final Location userLocation;

    public RoadsideRequest(String requestText, double latitude, double longitude) {
        if (requestText == null || requestText.isBlank()) {
            throw new IllegalArgumentException("Request text is required.");
        }
        this.requestText = requestText.trim();
        this.userLocation = new Location(latitude, longitude);
    }

    public String getRequestText() {
        return requestText;
    }

    public Location getUserLocation() {
        return userLocation;
    }
}
