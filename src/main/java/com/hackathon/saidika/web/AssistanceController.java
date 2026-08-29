package com.hackathon.saidika.web;

import com.hackathon.saidika.domain.ClassificationResult;
import com.hackathon.saidika.domain.MatchResult;
import com.hackathon.saidika.domain.RoadsideRequest;
import com.hackathon.saidika.service.ProviderMatchingService;
import com.hackathon.saidika.service.ServiceClassificationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

@Controller
public class AssistanceController {

    private final ServiceClassificationService classificationService;
    private final ProviderMatchingService providerMatchingService;

    public AssistanceController(ServiceClassificationService classificationService, ProviderMatchingService providerMatchingService) {
        this.classificationService = classificationService;
        this.providerMatchingService = providerMatchingService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("assistanceForm", new AssistanceForm());
        return "index";
    }

    @PostMapping("/assist")
    public String assist(@Valid @ModelAttribute("assistanceForm") AssistanceForm form,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Please correct the form values and try again.");
            return "index";
        }

        try {
            RoadsideRequest request = new RoadsideRequest(form.getRequestText(), Double.parseDouble(form.getLatitude()), Double.parseDouble(form.getLongitude()));
            ClassificationResult classification = classificationService.classify(request.getRequestText());

            if (!classification.isRecognized()) {
                model.addAttribute("errorMessage", classification.getReason());
                return "index";
            }

            Optional<MatchResult> bestProvider = providerMatchingService.findBestProvider(classification.getServiceType(), request.getUserLocation());
            if (bestProvider.isEmpty()) {
                model.addAttribute("errorMessage", "No eligible provider is available for this service right now.");
                return "index";
            }

            model.addAttribute("classification", classification);
            model.addAttribute("matchResult", bestProvider.get());
            return "result";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "index";
        }
    }
}
