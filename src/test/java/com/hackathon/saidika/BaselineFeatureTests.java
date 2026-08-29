package com.hackathon.saidika;

import com.hackathon.saidika.domain.ClassificationResult;
import com.hackathon.saidika.domain.Location;
import com.hackathon.saidika.domain.Provider;
import com.hackathon.saidika.domain.ServiceType;
import com.hackathon.saidika.repository.ProviderRepository;
import com.hackathon.saidika.service.DistanceCalculator;
import com.hackathon.saidika.service.ProviderMatchingService;
import com.hackathon.saidika.service.ServiceClassificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class BaselineFeatureTests {

    @Autowired
    private ServiceClassificationService classificationService;

    @Autowired
    private ProviderMatchingService providerMatchingService;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        providerRepository.deleteAll();
    }

    @Test
    void classifyDeadBattery() {
        ClassificationResult result = classificationService.classify("My battery is dead and my car won't start.");

        assertThat(result.isRecognized()).isTrue();
        assertThat(result.getServiceType()).isEqualTo(ServiceType.JUMP_START);
    }

    @Test
    void classifyFlatTyreAndFlatTire() {
        assertThat(classificationService.classify("flat tyre on the road").getServiceType()).isEqualTo(ServiceType.TYRE_ASSISTANCE);
        assertThat(classificationService.classify("flat tire and puncture").getServiceType()).isEqualTo(ServiceType.TYRE_ASSISTANCE);
    }

    @Test
    void classifyOverheatingAndMechanicalProblem() {
        assertThat(classificationService.classify("engine noise and overheating").getServiceType()).isEqualTo(ServiceType.MOBILE_MECHANIC);
        assertThat(classificationService.classify("mechanical problem").getServiceType()).isEqualTo(ServiceType.MOBILE_MECHANIC);
    }

    @Test
    void classifyVehicleMovementAndFuelAndLocksmith() {
        assertThat(classificationService.classify("car cannot move after accident").getServiceType()).isEqualTo(ServiceType.TOWING);
        assertThat(classificationService.classify("out of fuel and ran out of fuel").getServiceType()).isEqualTo(ServiceType.FUEL_ASSISTANCE);
        assertThat(classificationService.classify("locked out; keys inside the car").getServiceType()).isEqualTo(ServiceType.LOCKSMITH);
    }

    @Test
    void classifyUnsupportedRequest() {
        ClassificationResult result = classificationService.classify("I need help with a pizza order");
        assertThat(result.isRecognized()).isFalse();
        assertThat(result.getReason()).contains("No supported");
    }

    @Test
    void classifyAmbiguousRequestUsesDeterministicPriority() {
        ClassificationResult result = classificationService.classify("dead battery and out of fuel");
        assertThat(result.getServiceType()).isEqualTo(ServiceType.JUMP_START);
    }

    @Test
    void classifyNormalizesPunctuationAndCase() {
        ClassificationResult result = classificationService.classify("!!! BATTERY!!! DEAD? CAR WON'T START !!!");
        assertThat(result.isRecognized()).isTrue();
        assertThat(result.getServiceType()).isEqualTo(ServiceType.JUMP_START);
    }

    @Test
    void distanceSameCoordinatesApproxZero() {
        assertThat(DistanceCalculator.calculateKm(new Location(51.5, -0.1), new Location(51.5, -0.1))).isZero();
    }

    @Test
    void distanceKnownPairMatchesApproximation() {
        double distance = DistanceCalculator.calculateKm(new Location(0.0, 0.0), new Location(0.0, 1.0));
        assertThat(distance).isBetween(111.0, 112.5);
    }

    @Test
    void distanceIsSymmetric() {
        double aToB = DistanceCalculator.calculateKm(new Location(51.5, -0.1), new Location(52.5, -0.1));
        double bToA = DistanceCalculator.calculateKm(new Location(52.5, -0.1), new Location(51.5, -0.1));
        assertThat(aToB).isEqualTo(bToA, within(0.0001));
    }

    @Test
    void providerMatchingFindsNearestAvailableProvider() {
        providerRepository.save(new Provider("Alpha Jump", new Location(51.5000, -0.1000), true, Set.of(ServiceType.JUMP_START)));
        providerRepository.save(new Provider("Bravo Jump", new Location(51.5200, -0.1200), true, Set.of(ServiceType.JUMP_START)));
        providerRepository.save(new Provider("Closed Jump", new Location(51.4800, -0.0900), false, Set.of(ServiceType.JUMP_START)));

        Optional<com.hackathon.saidika.domain.MatchResult> result = providerMatchingService.findBestProvider(
                ServiceType.JUMP_START,
                new Location(51.5100, -0.1100)
        );

        assertThat(result).isPresent();
        assertThat(result.get().getProvider().getName()).isEqualTo("Alpha Jump");
    }

    @Test
    void providerMatchingExcludesUnavailableAndIgnoresWrongService() {
        providerRepository.save(new Provider("Wrong Service", new Location(51.5000, -0.1000), true, Set.of(ServiceType.TOWING)));
        providerRepository.save(new Provider("Closed Fuel", new Location(51.5000, -0.1000), false, Set.of(ServiceType.FUEL_ASSISTANCE)));
        providerRepository.save(new Provider("Open Fuel", new Location(51.5100, -0.1100), true, Set.of(ServiceType.FUEL_ASSISTANCE)));

        Optional<com.hackathon.saidika.domain.MatchResult> result = providerMatchingService.findBestProvider(
                ServiceType.FUEL_ASSISTANCE,
                new Location(51.5000, -0.1000)
        );

        assertThat(result).isPresent();
        assertThat(result.get().getProvider().getName()).isEqualTo("Open Fuel");
    }

    @Test
    void providerMatchingReturnsEmptyWhenNoEligibleProviderExists() {
        providerRepository.save(new Provider("Unavailable Towing", new Location(51.5000, -0.1000), false, Set.of(ServiceType.TOWING)));

        Optional<com.hackathon.saidika.domain.MatchResult> result = providerMatchingService.findBestProvider(
                ServiceType.TOWING,
                new Location(51.5000, -0.1000)
        );

        assertThat(result).isEmpty();
    }

    @Test
    void getRootPageLoadsForm() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void validAssistRequestReturnsResult() throws Exception {
        mockMvc.perform(post("/assist")
                        .contentType("application/x-www-form-urlencoded")
                        .param("requestText", "My battery is dead and my car won't start")
                        .param("latitude", "51.5")
                        .param("longitude", "-0.1"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attributeExists("classification"))
                .andExpect(model().attributeExists("matchResult"));
    }

    @Test
    void unrecognizedAssistRequestProducesPageError() throws Exception {
        mockMvc.perform(post("/assist")
                        .contentType("application/x-www-form-urlencoded")
                        .param("requestText", "I need a pizza")
                        .param("latitude", "51.5")
                        .param("longitude", "-0.1"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void invalidCoordinatesAreRejected() throws Exception {
        mockMvc.perform(post("/assist")
                        .contentType("application/x-www-form-urlencoded")
                        .param("requestText", "My battery is dead")
                        .param("latitude", "1000")
                        .param("longitude", "-0.1"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
