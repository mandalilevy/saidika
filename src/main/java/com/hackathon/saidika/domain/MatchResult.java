package com.hackathon.saidika.domain;

public class MatchResult {

    private final ServiceType requestedServiceType;
    private final Provider provider;
    private final double distanceKm;
    private final String rationale;

    public MatchResult(ServiceType requestedServiceType, Provider provider, double distanceKm, String rationale) {
        this.requestedServiceType = requestedServiceType;
        this.provider = provider;
        this.distanceKm = distanceKm;
        this.rationale = rationale;
    }

    public ServiceType getRequestedServiceType() {
        return requestedServiceType;
    }

    public Provider getProvider() {
        return provider;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public String getRationale() {
        return rationale;
    }
}
