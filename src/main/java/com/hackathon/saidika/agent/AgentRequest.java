package com.hackathon.saidika.agent;

/** Natural-language roadside-assistance request plus the browser-supplied coordinates. */
public record AgentRequest(String requestText, double latitude, double longitude) {
}
