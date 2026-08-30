package com.hackathon.saidika.research;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.saidika.domain.Provider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Reproducible, no-API-key public research: reverse-geocodes a provider's coordinates against the
 * free OpenStreetMap Nominatim API to discover a real public address near it. Nominatim never returns
 * phone numbers or websites, so those fields are honestly reported as unavailable rather than guessed.
 * Every discovered fact retains {@code sourceUrl} as its provenance.
 */
@Service
public class NominatimProviderResearchGateway implements ProviderResearchGateway {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String userAgent;
    private final Duration timeout;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public NominatimProviderResearchGateway(
            @Value("${saidika.research.nominatim-base-url:https://nominatim.openstreetmap.org}") String baseUrl,
            @Value("${saidika.research.user-agent:Saidika-Hackathon-Agent/1.0 (educational, non-commercial)}") String userAgent,
            @Value("${saidika.research.timeout-ms:4000}") long timeoutMs) {
        this.baseUrl = baseUrl;
        this.userAgent = userAgent;
        this.timeout = Duration.ofMillis(timeoutMs);
        this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    @Override
    public ProviderResearchResult research(Provider provider) {
        String url = baseUrl + "/reverse?format=jsonv2&lat=" + provider.getLocation().getLatitude()
                + "&lon=" + provider.getLocation().getLongitude() + "&zoom=18&addressdetails=1";
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", userAgent)
                    .timeout(timeout)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return ProviderResearchResult.unavailable(
                        "Public address lookup returned HTTP " + response.statusCode() + ".");
            }
            return parseResponseBody(response.body(), url);
        } catch (Exception ex) {
            return ProviderResearchResult.unavailable(
                    "Public address lookup failed (" + ex.getClass().getSimpleName() + ").");
        }
    }

    /** Package-private so the JSON-parsing logic can be unit tested without a network call. */
    ProviderResearchResult parseResponseBody(String body, String sourceUrl) {
        try {
            JsonNode node = objectMapper.readTree(body);
            String displayName = node.hasNonNull("display_name") ? node.get("display_name").asText() : null;
            if (displayName == null || displayName.isBlank()) {
                return ProviderResearchResult.unavailable(
                        "No public address information was found near this provider's coordinates.");
            }
            return new ProviderResearchResult(null, displayName, null, sourceUrl,
                    ResearchStatus.PUBLIC_INFORMATION_FOUND,
                    "Address discovered via OpenStreetMap Nominatim reverse geocoding. "
                            + "Phone and website are not available from this public source.");
        } catch (Exception ex) {
            return ProviderResearchResult.unavailable("Could not parse the public address lookup response.");
        }
    }
}
