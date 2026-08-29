package com.hackathon.saidika.service;

import com.hackathon.saidika.domain.Location;

public final class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private DistanceCalculator() {
    }

    public static double calculateKm(Location first, Location second) {
        double latDistance = Math.toRadians(second.getLatitude() - first.getLatitude());
        double lonDistance = Math.toRadians(second.getLongitude() - first.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(first.getLatitude())) * Math.cos(Math.toRadians(second.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
