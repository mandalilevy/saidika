package com.hackathon.saidika.evaluation;

public enum ScenarioCategory {
    DIRECT_REQUEST("Direct requests"),
    NATURAL_LANGUAGE("Natural language"),
    AMBIGUOUS("Ambiguous"),
    MULTI_CLUE("Multi-clue"),
    PROVIDER_MATCHING("Provider matching"),
    SERVICEABILITY("Serviceability"),
    UNSUPPORTED("Unsupported");

    private final String displayName;

    ScenarioCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
