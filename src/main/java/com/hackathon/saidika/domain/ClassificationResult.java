package com.hackathon.saidika.domain;

public class ClassificationResult {

    private final boolean recognized;
    private final ServiceType serviceType;
    private final String reason;

    public ClassificationResult(boolean recognized, ServiceType serviceType, String reason) {
        this.recognized = recognized;
        this.serviceType = serviceType;
        this.reason = reason;
    }

    public boolean isRecognized() {
        return recognized;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public String getReason() {
        return reason;
    }
}
