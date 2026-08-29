package com.hackathon.saidika.evaluation;

import com.hackathon.saidika.domain.ServiceType;

import java.util.List;

/**
 * Frozen set of ~24 deterministic evaluation scenarios for the Saidika deterministic baseline.
 *
 * Coordinates reuse the real Nairobi provider fixtures seeded by ProviderDataSeeder so the
 * evaluator exercises the actual production provider dataset rather than a duplicate fixture set.
 * Do not change these scenarios to make the baseline score higher; they exist to measure it.
 */
public final class EvaluationDataset {

    private EvaluationDataset() {
    }

    public static List<EvaluationScenario> scenarios() {
        return List.of(

                // 1. Straightforward requests (5)
                new EvaluationScenario("D01", ScenarioCategory.DIRECT_REQUEST,
                        "My battery is dead and my car won't start.",
                        -1.2864, 36.8172,
                        ServiceType.JUMP_START, true, ExpectedOutcome.MATCHED, null,
                        "Direct keyword match: 'battery is dead' / 'won't start'."),
                new EvaluationScenario("D02", ScenarioCategory.DIRECT_REQUEST,
                        "I have a flat tyre and need help.",
                        -1.2864, 36.8172,
                        ServiceType.TYRE_ASSISTANCE, true, ExpectedOutcome.MATCHED, null,
                        "Direct keyword match: 'flat tyre'."),
                new EvaluationScenario("D03", ScenarioCategory.DIRECT_REQUEST,
                        "I've run out of fuel on the highway.",
                        -1.2864, 36.8172,
                        ServiceType.FUEL_ASSISTANCE, true, ExpectedOutcome.MATCHED, null,
                        "Direct keyword match: 'out of fuel'."),
                new EvaluationScenario("D04", ScenarioCategory.DIRECT_REQUEST,
                        "I'm locked out of my car, my keys are inside.",
                        -1.2864, 36.8172,
                        ServiceType.LOCKSMITH, true, ExpectedOutcome.MATCHED, null,
                        "Direct keyword match: 'locked out' / 'keys inside'."),
                new EvaluationScenario("D05", ScenarioCategory.DIRECT_REQUEST,
                        "I need a tow truck to move my car.",
                        -1.2864, 36.8172,
                        ServiceType.TOWING, true, ExpectedOutcome.MATCHED, null,
                        "Direct keyword match: 'tow truck'."),

                // 2. Natural-language variations (5)
                new EvaluationScenario("N01", ScenarioCategory.NATURAL_LANGUAGE,
                        "The engine won't turn over and I think my battery is dead.",
                        -1.2864, 36.8172,
                        ServiceType.JUMP_START, true, ExpectedOutcome.MATCHED, null,
                        "Contains rule phrase 'battery is dead' despite different sentence framing."),
                new EvaluationScenario("N02", ScenarioCategory.NATURAL_LANGUAGE,
                        "I got a puncture and need someone to help replace the tyre.",
                        -1.2864, 36.8172,
                        ServiceType.TYRE_ASSISTANCE, true, ExpectedOutcome.MATCHED, null,
                        "Contains rule phrase 'puncture'."),
                new EvaluationScenario("N03", ScenarioCategory.NATURAL_LANGUAGE,
                        "I've completely run out of fuel.",
                        -1.2864, 36.8172,
                        ServiceType.FUEL_ASSISTANCE, true, ExpectedOutcome.MATCHED, null,
                        "Contains rule phrase 'out of fuel'."),
                new EvaluationScenario("N04", ScenarioCategory.NATURAL_LANGUAGE,
                        "I locked my keys inside the vehicle.",
                        -1.2864, 36.8172,
                        ServiceType.LOCKSMITH, true, ExpectedOutcome.MATCHED, null,
                        "Contains rule phrase 'keys inside'."),
                new EvaluationScenario("N05", ScenarioCategory.NATURAL_LANGUAGE,
                        "My car needs to be transported to a garage.",
                        -1.2864, 36.8172,
                        ServiceType.TOWING, true, ExpectedOutcome.MATCHED, null,
                        "Objectively a towing request, but contains none of the TOWING rule phrases "
                                + "('car won't move', 'accident', 'tow truck', 'towing'). Expected to expose a "
                                + "genuine keyword-coverage gap in the deterministic classifier."),

                // 3. Ambiguous requests (4)
                new EvaluationScenario("A01", ScenarioCategory.AMBIGUOUS,
                        "My car won't start.",
                        -1.2864, 36.8172,
                        ServiceType.JUMP_START, true, ExpectedOutcome.MATCHED, null,
                        "Real-world cause is ambiguous (battery, fuel, mechanical), but the deterministic "
                                + "rule contract explicitly maps the phrase 'won't start' to JUMP_START, so the "
                                + "documented expected result is JUMP_START."),
                new EvaluationScenario("A02", ScenarioCategory.AMBIGUOUS,
                        "The vehicle stopped suddenly.",
                        -1.2864, 36.8172,
                        null, false, ExpectedOutcome.AMBIGUOUS, null,
                        "No rule phrase matches and no single service is objectively defensible; excluded "
                                + "from strict classification accuracy. Baseline's actual decision is still recorded."),
                new EvaluationScenario("A03", ScenarioCategory.AMBIGUOUS,
                        "I can't get the car moving.",
                        -1.2864, 36.8172,
                        null, false, ExpectedOutcome.AMBIGUOUS, null,
                        "No rule phrase matches ('moving' != 'move'); could be TOWING, MOBILE_MECHANIC or "
                                + "JUMP_START. Excluded from strict classification accuracy."),
                new EvaluationScenario("A04", ScenarioCategory.AMBIGUOUS,
                        "There's a strange noise coming from under the car.",
                        -1.2864, 36.8172,
                        null, false, ExpectedOutcome.AMBIGUOUS, null,
                        "No rule phrase matches ('strange noise' != 'engine noise'); genuinely ambiguous. "
                                + "Excluded from strict classification accuracy."),

                // 4. Multi-clue requests (4)
                new EvaluationScenario("M01", ScenarioCategory.MULTI_CLUE,
                        "My car was involved in an accident, it won't move, and I need it taken to a garage.",
                        -1.2864, 36.8172,
                        ServiceType.TOWING, true, ExpectedOutcome.MATCHED, null,
                        "Multiple TOWING clues ('accident', 'won't move') agree; discovered during manual "
                                + "testing as the important towing scenario."),
                new EvaluationScenario("M02", ScenarioCategory.MULTI_CLUE,
                        "My battery is dead but I also think I have a flat tyre after hitting a pothole.",
                        -1.2864, 36.8172,
                        ServiceType.JUMP_START, true, ExpectedOutcome.MATCHED, null,
                        "Two competing clues; the primary complaint stated first ('battery is dead') is the "
                                + "strongest interpretation and matches the classifier's earliest-mention rule."),
                new EvaluationScenario("M03", ScenarioCategory.MULTI_CLUE,
                        "The tow truck can't reach me because my tyre also blew out and now the car won't move at all.",
                        -1.2864, 36.8172,
                        ServiceType.TOWING, true, ExpectedOutcome.MATCHED, null,
                        "Multiple clues across two services; earliest mention ('tow truck') aligns with the "
                                + "overall towing situation."),
                new EvaluationScenario("M04", ScenarioCategory.MULTI_CLUE,
                        "I think I might be locked out, but actually my tyre burst is the bigger issue right now.",
                        -1.2864, 36.8172,
                        ServiceType.TYRE_ASSISTANCE, true, ExpectedOutcome.MATCHED, null,
                        "The requester explicitly states the tyre burst is the bigger/priority issue, but "
                                + "'locked out' is mentioned first. Expected to expose a genuine weakness: the "
                                + "classifier's earliest-mention heuristic ignores explicit priority/urgency "
                                + "qualifiers ('actually', 'bigger issue right now')."),

                // 5. Provider matching (2) - deterministic, real seeded Nairobi providers
                new EvaluationScenario("P01", ScenarioCategory.PROVIDER_MATCHING,
                        "My battery is dead, I need a jump start.",
                        -1.2576, 36.8037,
                        ServiceType.JUMP_START, true, ExpectedOutcome.MATCHED, "Westlands Battery Care",
                        "Query location is exactly at 'Westlands Battery Care' (distance 0km), which is "
                                + "unambiguously nearer than the other available JUMP_START providers."),
                new EvaluationScenario("P02", ScenarioCategory.PROVIDER_MATCHING,
                        "I have a flat tyre near Ngong Road.",
                        -1.3900, 36.7200,
                        ServiceType.TYRE_ASSISTANCE, true, ExpectedOutcome.MATCHED, "Karen Tyre Fix",
                        "Query location is exactly at the unavailable 'Closed Nairobi Tyre' provider, which "
                                + "must be excluded by availability filtering. 'Karen Tyre Fix' is the next "
                                + "nearest available+capable provider within its own radius; 'Eastleigh Tyre "
                                + "Response' and 'Kibera Road Care' are both outside their radii from this point."),

                // 6. Serviceability / no-provider (2)
                new EvaluationScenario("S01", ScenarioCategory.SERVICEABILITY,
                        "I'm locked out of my car and need a locksmith.",
                        -1.5500, 36.4500,
                        ServiceType.LOCKSMITH, true, ExpectedOutcome.NO_PROVIDER, null,
                        "Nearest capable provider ('Ngong Lockout Team') supports LOCKSMITH but this point "
                                + "falls outside its 20km service radius; 'CBD Lock & Go' is far further still."),
                new EvaluationScenario("S02", ScenarioCategory.SERVICEABILITY,
                        "My engine is overheating and making a knocking noise.",
                        -4.0435, 39.6682,
                        ServiceType.MOBILE_MECHANIC, true, ExpectedOutcome.NO_PROVIDER, null,
                        "Mombasa-area coordinates are far outside every MOBILE_MECHANIC provider's service "
                                + "radius (all configured radii are 25km or less), so no provider is serviceable."),

                // 7. Unsupported requests (2)
                new EvaluationScenario("U01", ScenarioCategory.UNSUPPORTED,
                        "My laptop won't turn on.",
                        -1.2864, 36.8172,
                        null, true, ExpectedOutcome.UNSUPPORTED, null,
                        "Outside the roadside-assistance domain entirely; classifier is expected to return unrecognized."),
                new EvaluationScenario("U02", ScenarioCategory.UNSUPPORTED,
                        "I need help fixing my washing machine at home.",
                        -1.2864, 36.8172,
                        null, true, ExpectedOutcome.UNSUPPORTED, null,
                        "Outside the roadside-assistance domain entirely; classifier is expected to return unrecognized.")
        );
    }
}
