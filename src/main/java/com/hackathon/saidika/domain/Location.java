package com.hackathon.saidika.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public class Location {

    private double latitude;
    private double longitude;

    protected Location() {
    }

    public Location(double latitude, double longitude) {
        validateLatitude(latitude);
        validateLongitude(longitude);
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        validateLatitude(latitude);
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        validateLongitude(longitude);
        this.longitude = longitude;
    }

    private void validateLatitude(double latitude) {
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees.");
        }
    }

    private void validateLongitude(double longitude) {
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees.");
        }
    }
}
