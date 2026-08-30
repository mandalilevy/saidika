package com.hackathon.saidika.service;

import com.hackathon.saidika.domain.Location;
import com.hackathon.saidika.domain.MatchResult;
import com.hackathon.saidika.domain.Provider;
import com.hackathon.saidika.domain.ServiceType;
import com.hackathon.saidika.repository.ProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProviderMatchingService {

    private final ProviderRepository providerRepository;

    public ProviderMatchingService(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    // keeps the Hibernate session open so lazy-loaded supportedServices can be read during matching
    @Transactional(readOnly = true)
    public Optional<MatchResult> findBestProvider(ServiceType requestedServiceType, Location userLocation) {
        List<Provider> eligibleProviders = eligibleProviders(requestedServiceType, userLocation);
        if (eligibleProviders.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(toMatchResult(requestedServiceType, eligibleProviders.get(0), userLocation));
    }

    /**
     * Same eligibility/ranking as {@link #findBestProvider}, but returns up to {@code limit} eligible
     * providers instead of only the best one. Used by the agent's provider-lookup tool so it can present
     * alternates; contains no additional distance, radius, availability or capability logic.
     */
    @Transactional(readOnly = true)
    public List<MatchResult> findEligibleProviders(ServiceType requestedServiceType, Location userLocation, int limit) {
        return eligibleProviders(requestedServiceType, userLocation).stream()
                .limit(limit)
                .map(provider -> toMatchResult(requestedServiceType, provider, userLocation))
                .toList();
    }

    private List<Provider> eligibleProviders(ServiceType requestedServiceType, Location userLocation) {
        return providerRepository.findAll().stream()
                .filter(Provider::isAvailable)
                .filter(provider -> provider.supports(requestedServiceType))
                .filter(provider -> provider.isWithinServiceRadius(userLocation))
                .sorted(Comparator.<Provider>comparingDouble(provider -> DistanceCalculator.calculateKm(userLocation, provider.getLocation()))
                        .thenComparing(Provider::getName))
                .toList();
    }

    private MatchResult toMatchResult(ServiceType requestedServiceType, Provider provider, Location userLocation) {
        double distanceKm = DistanceCalculator.calculateKm(userLocation, provider.getLocation());
        String rationale = "Nearest eligible provider within " + provider.getServiceRadiusKm() + " km for " + requestedServiceType + ".";
        return new MatchResult(requestedServiceType, provider, distanceKm, rationale);
    }
}
