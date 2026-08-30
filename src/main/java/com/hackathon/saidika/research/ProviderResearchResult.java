package com.hackathon.saidika.research;

/** Result of a public-information lookup for one provider. Every non-null field must retain a source. */
public record ProviderResearchResult(
        String phone,
        String address,
        String website,
        String sourceUrl,
        ResearchStatus status,
        String notes
) {

    public static ProviderResearchResult unavailable(String notes) {
        return new ProviderResearchResult(null, null, null, null, ResearchStatus.INFORMATION_UNAVAILABLE, notes);
    }
}
