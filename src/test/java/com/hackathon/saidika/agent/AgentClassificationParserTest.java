package com.hackathon.saidika.agent;

import com.hackathon.saidika.domain.ServiceType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure unit test: no Spring context, no LLM call. Proves the parser never trusts unvalidated model output. */
class AgentClassificationParserTest {

    private final AgentClassificationParser parser = new AgentClassificationParser();

    @Test
    void parsesWellFormedJson() {
        Optional<AgentClassification> result = parser.parse(
                "{\"service_type\": \"TOWING\", \"ambiguous\": false, \"explanation\": \"Needs a tow.\"}");

        assertThat(result).isPresent();
        assertThat(result.get().serviceType()).isEqualTo(ServiceType.TOWING);
        assertThat(result.get().ambiguous()).isFalse();
        assertThat(result.get().explanation()).isEqualTo("Needs a tow.");
    }

    @Test
    void extractsJsonFromSurroundingProseOrMarkdownFences() {
        String raw = "Sure, here you go:\n```json\n"
                + "{\"service_type\": \"JUMP_START\", \"ambiguous\": false, \"explanation\": \"Dead battery.\"}\n"
                + "```\nLet me know if you need anything else.";

        Optional<AgentClassification> result = parser.parse(raw);

        assertThat(result).isPresent();
        assertThat(result.get().serviceType()).isEqualTo(ServiceType.JUMP_START);
    }

    @Test
    void ambiguousTrueYieldsNullServiceTypeRegardlessOfServiceTypeField() {
        Optional<AgentClassification> result = parser.parse(
                "{\"service_type\": \"TOWING\", \"ambiguous\": true, \"explanation\": \"Not sure.\"}");

        assertThat(result).isPresent();
        assertThat(result.get().serviceType()).isNull();
        assertThat(result.get().ambiguous()).isTrue();
    }

    @Test
    void unknownServiceTypeYieldsNullServiceType() {
        Optional<AgentClassification> result = parser.parse(
                "{\"service_type\": \"UNKNOWN\", \"ambiguous\": true, \"explanation\": \"Outside domain.\"}");

        assertThat(result).isPresent();
        assertThat(result.get().serviceType()).isNull();
    }

    @Test
    void unrecognizedServiceTypeStringFailsParsing() {
        Optional<AgentClassification> result = parser.parse(
                "{\"service_type\": \"HELICOPTER_RESCUE\", \"ambiguous\": false, \"explanation\": \"x\"}");

        assertThat(result).isEmpty();
    }

    @Test
    void missingRequiredFieldsFailsParsing() {
        assertThat(parser.parse("{\"ambiguous\": false}")).isEmpty();
        assertThat(parser.parse("{\"service_type\": \"TOWING\"}")).isEmpty();
    }

    @Test
    void malformedJsonFailsParsing() {
        assertThat(parser.parse("{service_type: TOWING, ambiguous: false")).isEmpty();
    }

    @Test
    void blankOrNullInputFailsParsing() {
        assertThat(parser.parse(null)).isEmpty();
        assertThat(parser.parse("   ")).isEmpty();
    }

    @Test
    void missingExplanationFallsBackToDefaultText() {
        Optional<AgentClassification> result = parser.parse(
                "{\"service_type\": \"LOCKSMITH\", \"ambiguous\": false}");

        assertThat(result).isPresent();
        assertThat(result.get().explanation()).isEqualTo("No explanation provided by the model.");
    }
}
