package com.hackathon.saidika.web;

import com.hackathon.saidika.agent.AgentAssistanceResult;
import com.hackathon.saidika.agent.AgentRequest;
import com.hackathon.saidika.agent.RoadsideAssistanceAgent;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/** Advanced-agent counterpart to {@link AssistanceController}; reuses the same form/index page. */
@Controller
public class AgentController {

    private final RoadsideAssistanceAgent agent;

    public AgentController(RoadsideAssistanceAgent agent) {
        this.agent = agent;
    }

    @PostMapping("/agent/assist")
    public String assist(@Valid @ModelAttribute("assistanceForm") AssistanceForm form,
                          BindingResult bindingResult,
                          Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errorMessage", "Please correct the form values and try again.");
            return "index";
        }

        double latitude;
        double longitude;
        try {
            latitude = Double.parseDouble(form.getLatitude());
            longitude = Double.parseDouble(form.getLongitude());
        } catch (NumberFormatException ex) {
            model.addAttribute("errorMessage", "Location is required. Please share your location and try again.");
            return "index";
        }

        AgentAssistanceResult result = agent.assist(new AgentRequest(form.getRequestText(), latitude, longitude));
        model.addAttribute("agentResult", result);
        return "agent-result";
    }
}
