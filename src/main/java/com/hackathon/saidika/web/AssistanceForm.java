package com.hackathon.saidika.web;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public class AssistanceForm {

    @NotBlank(message = "Please describe the roadside problem.")
    private String requestText;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90.")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90.")
    private String latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180.")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180.")
    private String longitude;

    public String getRequestText() {
        return requestText;
    }

    public void setRequestText(String requestText) {
        this.requestText = requestText;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
