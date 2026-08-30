package com.hackathon.saidika.agent;

import com.hackathon.saidika.domain.MatchResult;
import com.hackathon.saidika.domain.ServiceType;

/** A nearby eligible provider that was not selected as the primary recommendation (no research performed). */
public class ProviderCandidate {

    private final String name;
    private final ServiceType serviceType;
    private final double distanceKm;
    private final double latitude;
    private final double longitude;
    private final boolean available;

    private ProviderCandidate(String name, ServiceType serviceType, double distanceKm,
                               double latitude, double longitude, boolean available) {
        this.name = name;
        this.serviceType = serviceType;
        this.distanceKm = distanceKm;
        this.latitude = latitude;
        this.longitude = longitude;
        this.available = available;
    }

    public static ProviderCandidate from(MatchResult match) {
        return new ProviderCandidate(
                match.getProvider().getName(),
                match.getRequestedServiceType(),
                match.getDistanceKm(),
                match.getProvider().getLocation().getLatitude(),
                match.getProvider().getLocation().getLongitude(),
                match.getProvider().isAvailable());
    }

    public String getName() {
        return name;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public boolean isAvailable() {
        return available;
    }
}
