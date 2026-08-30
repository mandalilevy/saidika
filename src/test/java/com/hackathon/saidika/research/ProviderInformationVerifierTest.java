package com.hackathon.saidika.research;

import com.hackathon.saidika.domain.Location;
import com.hackathon.saidika.domain.Provider;
import com.hackathon.saidika.domain.ServiceType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Never marks information verified just because it was discovered; requires an explicit identity match. */
class ProviderInformationVerifierTest {

    private final ProviderInformationVerifier verifier = new ProviderInformationVerifier();

    @Test
    void promotesToVerifiedWhenProviderNameAppearsInDiscoveredAddress() {
        Provider provider = new Provider("Karen Tyre Fix", new Location(-1.3180, 36.7069), true, Set.of(ServiceType.TYRE_ASSISTANCE));
        ProviderResearchResult discovered = new ProviderResearchResult(null, "Karen Tyre Fix, Karen Road, Nairobi",
                null, "https://example.org/source", ResearchStatus.PUBLIC_INFORMATION_FOUND, "found");

        ProviderResearchResult result = verifier.verify(provider, discovered);

        assertThat(result.status()).isEqualTo(ResearchStatus.VERIFIED);
    }

    @Test
    void staysPublicInformationFoundWhenIdentityCannotBeConfirmed() {
        Provider provider = new Provider("Karen Tyre Fix", new Location(-1.3180, 36.7069), true, Set.of(ServiceType.TYRE_ASSISTANCE));
        ProviderResearchResult discovered = new ProviderResearchResult(null, "Karen Road, Nairobi, Kenya",
                null, "https://example.org/source", ResearchStatus.PUBLIC_INFORMATION_FOUND, "found");

        ProviderResearchResult result = verifier.verify(provider, discovered);

        assertThat(result.status()).isEqualTo(ResearchStatus.PUBLIC_INFORMATION_FOUND);
    }

    @Test
    void informationUnavailableIsNeverPromoted() {
        Provider provider = new Provider("Karen Tyre Fix", new Location(-1.3180, 36.7069), true, Set.of(ServiceType.TYRE_ASSISTANCE));
        ProviderResearchResult discovered = ProviderResearchResult.unavailable("no data");

        ProviderResearchResult result = verifier.verify(provider, discovered);

        assertThat(result.status()).isEqualTo(ResearchStatus.INFORMATION_UNAVAILABLE);
    }
}
