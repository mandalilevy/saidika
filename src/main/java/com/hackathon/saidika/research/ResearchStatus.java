package com.hackathon.saidika.research;

/** Distinguishes "discovered" from "identity-confirmed" public provider information. Never set to VERIFIED by the LLM. */
public enum ResearchStatus {
    VERIFIED,
    PUBLIC_INFORMATION_FOUND,
    INFORMATION_UNAVAILABLE
}
