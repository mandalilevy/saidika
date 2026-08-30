package com.hackathon.saidika.research;

import com.hackathon.saidika.domain.Location;
import com.hackathon.saidika.domain.Provider;
import com.hackathon.saidika.domain.ServiceType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Manual connectivity check against the real, free OpenStreetMap Nominatim API. Deliberately named *IT*
 * (not *Test*) so Surefire's default inclusion pattern skips it during ".\mvnw.cmd test" (requires
 * internet access and is subject to Nominatim's usage policy/rate limits).
 *
 * Run explicitly via: .\mvnw.cmd test -Dtest=NominatimLiveConnectivityIT
 */
class NominatimLiveConnectivityIT {

    @Test
    void reverseGeocodesARealNairobiCoordinate() {
        NominatimProviderResearchGateway gateway = new NominatimProviderResearchGateway(
                "https://nominatim.openstreetmap.org", "Saidika-Hackathon-Test/1.0", 5000);
        Provider provider = new Provider("Nairobi Jump Centre", new Location(-1.2864, 36.8172),
                true, Set.of(ServiceType.JUMP_START));

        ProviderResearchResult result = gateway.research(provider);

        assertThat(result.status()).isIn(ResearchStatus.PUBLIC_INFORMATION_FOUND, ResearchStatus.INFORMATION_UNAVAILABLE);
        if (result.status() == ResearchStatus.PUBLIC_INFORMATION_FOUND) {
            assertThat(result.address()).isNotBlank();
            assertThat(result.sourceUrl()).contains("nominatim.openstreetmap.org");
        }
    }
}
