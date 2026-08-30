package com.hackathon.saidika.agent.tool;

import com.hackathon.saidika.domain.Location;
import com.hackathon.saidika.domain.MatchResult;
import com.hackathon.saidika.domain.Provider;
import com.hackathon.saidika.domain.ServiceType;
import com.hackathon.saidika.service.ProviderMatchingService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Proves the tool delegates to ProviderMatchingService rather than reimplementing any matching logic. */
class ProviderLookupToolTest {

    @Test
    void delegatesToProviderMatchingServiceWithSameArguments() {
        ProviderMatchingService providerMatchingService = mock(ProviderMatchingService.class);
        Location location = new Location(-1.2864, 36.8172);
        Provider provider = new Provider("Test Provider", location, true, Set.of(ServiceType.TOWING));
        MatchResult expected = new MatchResult(ServiceType.TOWING, provider, 1.5, "rationale");
        when(providerMatchingService.findEligibleProviders(eq(ServiceType.TOWING), eq(location), eq(3)))
                .thenReturn(List.of(expected));

        ProviderLookupTool tool = new ProviderLookupTool(providerMatchingService);
        List<MatchResult> result = tool.findNearbyProviders(ServiceType.TOWING, location, 3);

        assertThat(result).containsExactly(expected);
        verify(providerMatchingService).findEligibleProviders(ServiceType.TOWING, location, 3);
    }
}
