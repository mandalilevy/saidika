package com.hackathon.saidika.agent;

import com.hackathon.saidika.domain.ServiceType;

/**
 * The model's structured classification decision. {@code serviceType} is null whenever the request is
 * ambiguous/unsupported. {@code usedFallbackClassifier} records whether the deterministic baseline
 * classifier had to be used because the model was unavailable or returned unusable output.
 */
record AgentClassification(ServiceType serviceType, boolean ambiguous, String explanation, boolean usedFallbackClassifier) {
}
