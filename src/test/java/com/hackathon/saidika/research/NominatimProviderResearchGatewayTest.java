package com.hackathon.saidika.research;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Parsing/normalization logic tested without a network call. */
class NominatimProviderResearchGatewayTest {

    private final NominatimProviderResearchGateway gateway =
            new NominatimProviderResearchGateway("https://nominatim.openstreetmap.org", "test-agent", 4000);

    @Test
    void parsesDisplayNameIntoPublicInformationFound() {
        String body = "{\"display_name\": \"Kimathi Street, Nairobi Central, Nairobi, Kenya\"}";

        ProviderResearchResult result = gateway.parseResponseBody(body, "https://example.org/reverse?lat=1&lon=2");

        assertThat(result.status()).isEqualTo(ResearchStatus.PUBLIC_INFORMATION_FOUND);
        assertThat(result.address()).isEqualTo("Kimathi Street, Nairobi Central, Nairobi, Kenya");
        assertThat(result.sourceUrl()).isEqualTo("https://example.org/reverse?lat=1&lon=2");
        assertThat(result.phone()).isNull();
        assertThat(result.website()).isNull();
    }

    @Test
    void missingDisplayNameYieldsInformationUnavailable() {
        String body = "{\"error\": \"Unable to geocode\"}";

        ProviderResearchResult result = gateway.parseResponseBody(body, "https://example.org/reverse");

        assertThat(result.status()).isEqualTo(ResearchStatus.INFORMATION_UNAVAILABLE);
        assertThat(result.address()).isNull();
        assertThat(result.sourceUrl()).isNull();
    }

    @Test
    void malformedJsonYieldsInformationUnavailable() {
        ProviderResearchResult result = gateway.parseResponseBody("not json at all", "https://example.org/reverse");

        assertThat(result.status()).isEqualTo(ResearchStatus.INFORMATION_UNAVAILABLE);
    }
}
