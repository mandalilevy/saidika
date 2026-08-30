package com.hackathon.saidika.agent.tool;

import com.hackathon.saidika.domain.Location;
import com.hackathon.saidika.domain.MatchResult;
import com.hackathon.saidika.domain.ServiceType;
import com.hackathon.saidika.service.ProviderMatchingService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Agent-controlled tool exposing provider discovery ({@code findNearbyProviders}). Delegates entirely
 * to {@link ProviderMatchingService}; contains no distance, radius, availability, capability or ranking
 * logic of its own so the deterministic service remains the sole authority on provider facts.
 */
@Service
public class ProviderLookupTool {

    private final ProviderMatchingService providerMatchingService;

    public ProviderLookupTool(ProviderMatchingService providerMatchingService) {
        this.providerMatchingService = providerMatchingService;
    }

    public List<MatchResult> findNearbyProviders(ServiceType serviceType, Location location, int limit) {
        return providerMatchingService.findEligibleProviders(serviceType, location, limit);
    }
}
