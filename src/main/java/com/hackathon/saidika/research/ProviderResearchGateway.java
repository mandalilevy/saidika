package com.hackathon.saidika.research;

import com.hackathon.saidika.domain.Provider;

/**
 * Seam between Saidika and whichever public-information lookup is configured, mirroring
 * {@code LanguageModelGateway}'s role for the chat model. Implementations must never invent data:
 * unavailable information must be represented as {@link ResearchStatus#INFORMATION_UNAVAILABLE}.
 */
public interface ProviderResearchGateway {

    ProviderResearchResult research(Provider provider);
}
