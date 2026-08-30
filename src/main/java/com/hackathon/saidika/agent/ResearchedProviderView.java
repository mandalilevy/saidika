package com.hackathon.saidika.agent;

import com.hackathon.saidika.domain.MatchResult;
import com.hackathon.saidika.domain.ServiceType;
import com.hackathon.saidika.research.ProviderResearchResult;
import com.hackathon.saidika.research.ResearchStatus;

/** The primary recommended provider, enriched with (honestly-labelled) public research and verification. */
public class ResearchedProviderView {

    private final String name;
    private final ServiceType serviceType;
    private final double distanceKm;
    private final double latitude;
    private final double longitude;
    private final boolean available;
    private final String phone;
    private final String address;
    private final String website;
    private final String sourceUrl;
    private final ResearchStatus verificationStatus;
    private final String verificationNotes;
    private final boolean researchAttempted;

    ResearchedProviderView(String name, ServiceType serviceType, double distanceKm, double latitude, double longitude,
                           boolean available, String phone, String address, String website, String sourceUrl,
                           ResearchStatus verificationStatus, String verificationNotes, boolean researchAttempted) {
        this.name = name;
        this.serviceType = serviceType;
        this.distanceKm = distanceKm;
        this.latitude = latitude;
        this.longitude = longitude;
        this.available = available;
        this.phone = phone;
        this.address = address;
        this.website = website;
        this.sourceUrl = sourceUrl;
        this.verificationStatus = verificationStatus;
        this.verificationNotes = verificationNotes;
        this.researchAttempted = researchAttempted;
    }

    static ResearchedProviderView of(MatchResult match, ProviderResearchResult research, boolean researchAttempted) {
        return new ResearchedProviderView(
                match.getProvider().getName(),
                match.getRequestedServiceType(),
                match.getDistanceKm(),
                match.getProvider().getLocation().getLatitude(),
                match.getProvider().getLocation().getLongitude(),
                match.getProvider().isAvailable(),
                research.phone(), research.address(), research.website(), research.sourceUrl(),
                research.status(), research.notes(), researchAttempted);
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

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getWebsite() {
        return website;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public ResearchStatus getVerificationStatus() {
        return verificationStatus;
    }

    public String getVerificationNotes() {
        return verificationNotes;
    }

    public boolean isResearchAttempted() {
        return researchAttempted;
    }
}
