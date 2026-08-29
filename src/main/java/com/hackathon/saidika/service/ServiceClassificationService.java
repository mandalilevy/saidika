package com.hackathon.saidika.service;

import com.hackathon.saidika.domain.ClassificationResult;
import com.hackathon.saidika.domain.ServiceType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ServiceClassificationService {

    private static final Map<ServiceType, List<String>> RULES = Map.of(
            ServiceType.JUMP_START, List.of("dead battery", "battery is dead", "car won't start", "car will not start", "won't start", "jump start"),
            ServiceType.TYRE_ASSISTANCE, List.of("flat tyre", "flat tire", "puncture", "tyre blowout", "tire blowout", "tyre", "tire"),
            ServiceType.MOBILE_MECHANIC, List.of("engine noise", "mechanical problem", "overheating", "mechanical issue", "engine issue", "knocking"),
            ServiceType.TOWING, List.of("car cannot move", "car won't move", "won't move", "accident", "tow truck", "towing"),
            ServiceType.FUEL_ASSISTANCE, List.of("out of fuel", "ran out of fuel", "empty tank", "out of gas", "ran out of gas"),
            ServiceType.LOCKSMITH, List.of("locked out", "keys inside", "cannot unlock", "can't unlock", "car key stuck")
    );

    public ClassificationResult classify(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return new ClassificationResult(false, null, "No supported service could be identified from the empty request.");
        }

        String normalized = normalize(rawText);
        List<RuleMatch> matches = new ArrayList<>();

        for (Map.Entry<ServiceType, List<String>> entry : RULES.entrySet()) {
            for (String phrase : entry.getValue()) {
                String normalizedPhrase = normalize(phrase);
                int index = normalized.indexOf(normalizedPhrase);
                if (index >= 0) {
                    matches.add(new RuleMatch(entry.getKey(), phrase, index, normalizedPhrase.length()));
                }
            }
        }

        if (matches.isEmpty()) {
            return new ClassificationResult(false, null, "No supported service could be identified from the request.");
        }

        matches.sort(Comparator
                .comparingInt(RuleMatch::index)
                .thenComparing(Comparator.comparingInt(RuleMatch::specificity).reversed())
                .thenComparing(rule -> rule.serviceType().name()));

        RuleMatch selected = matches.get(0);
        return new ClassificationResult(true, selected.serviceType(), "Matched rule: " + selected.phrase());
    }

    private String normalize(String text) {
        String lowered = text.toLowerCase();
        String normalized = lowered.replace("’", "'").replace("-", " ");
        normalized = normalized.replaceAll("[.,!?;:()\"]", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private record RuleMatch(ServiceType serviceType, String phrase, int index, int specificity) {
    }
}
