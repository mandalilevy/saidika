package com.hackathon.saidika.agent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.saidika.domain.ServiceType;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Defensively extracts and validates the model's structured JSON classification output. The model must
 * never be trusted to produce well-formed output: any parse failure or unrecognized service type yields
 * {@link Optional#empty()}, which the agent treats as a signal to fall back to the deterministic classifier.
 */
final class AgentClassificationParser {

    // Tolerates the model wrapping its JSON in markdown fences or stray prose/thinking despite instructions.
    private static final Pattern JSON_OBJECT = Pattern.compile("\\{[\\s\\S]*}");

    private final ObjectMapper objectMapper = new ObjectMapper();

    Optional<AgentClassification> parse(String rawModelOutput) {
        if (rawModelOutput == null || rawModelOutput.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = JSON_OBJECT.matcher(rawModelOutput);
        if (!matcher.find()) {
            return Optional.empty();
        }
        try {
            JsonNode node = objectMapper.readTree(matcher.group());
            JsonNode serviceTypeNode = node.get("service_type");
            JsonNode ambiguousNode = node.get("ambiguous");
            if (serviceTypeNode == null || ambiguousNode == null) {
                return Optional.empty();
            }

            String rawServiceType = serviceTypeNode.asText("").trim().toUpperCase(Locale.ROOT);
            boolean ambiguous = ambiguousNode.asBoolean(false);
            String explanation = node.hasNonNull("explanation") ? node.get("explanation").asText().trim() : "";
            if (explanation.isBlank()) {
                explanation = "No explanation provided by the model.";
            }

            if (ambiguous || rawServiceType.isBlank() || "UNKNOWN".equals(rawServiceType)) {
                return Optional.of(new AgentClassification(null, true, explanation, false));
            }

            ServiceType serviceType;
            try {
                serviceType = ServiceType.valueOf(rawServiceType);
            } catch (IllegalArgumentException ex) {
                return Optional.empty();
            }
            return Optional.of(new AgentClassification(serviceType, false, explanation, false));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }
}
