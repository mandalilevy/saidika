package com.hackathon.saidika.research;

import com.hackathon.saidika.domain.Provider;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Lightweight, deterministic verification: a researched fact is only promoted from
 * {@link ResearchStatus#PUBLIC_INFORMATION_FOUND} to {@link ResearchStatus#VERIFIED} when the
 * provider's own name literally appears in the discovered text. This is never decided by the LLM.
 */
@Service
public class ProviderInformationVerifier {

    public ProviderResearchResult verify(Provider provider, ProviderResearchResult discovered) {
        if (discovered.status() == ResearchStatus.INFORMATION_UNAVAILABLE) {
            return discovered;
        }
        boolean identityMatch = discovered.address() != null
                && normalize(discovered.address()).contains(normalize(provider.getName()));
        if (!identityMatch) {
            return discovered;
        }
        return new ProviderResearchResult(discovered.phone(), discovered.address(), discovered.website(),
                discovered.sourceUrl(), ResearchStatus.VERIFIED,
                discovered.notes() + " Provider name matched the public listing text, so this is marked VERIFIED.");
    }

    private String normalize(String text) {
        return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
