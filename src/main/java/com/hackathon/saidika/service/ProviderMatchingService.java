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
        List<Provider> eligibleProviders = providerRepository.findAll().stream()
                .filter(Provider::isAvailable)
                .filter(provider -> provider.supports(requestedServiceType))
                .filter(provider -> provider.isWithinServiceRadius(userLocation))
                .sorted(Comparator.<Provider>comparingDouble(provider -> DistanceCalculator.calculateKm(userLocation, provider.getLocation()))
                        .thenComparing(Provider::getName))
                .toList();

        if (eligibleProviders.isEmpty()) {
            return Optional.empty();
        }

        Provider selected = eligibleProviders.get(0);
        double distanceKm = DistanceCalculator.calculateKm(userLocation, selected.getLocation());
        String rationale = "Nearest eligible provider within " + selected.getServiceRadiusKm() + " km for " + requestedServiceType + ".";
        return Optional.of(new MatchResult(requestedServiceType, selected, distanceKm, rationale));
    }
}
