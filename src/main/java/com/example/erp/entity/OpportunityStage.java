package com.example.erp.entity;

// CLOSED_WON/CLOSED_LOST are terminal and reached only via
// OpportunityService.winOpportunity/loseOpportunity — never through the
// generic updateStage endpoint (see its guard).
public enum OpportunityStage {
    QUALIFICATION,
    NEEDS_ANALYSIS,
    PROPOSAL,
    NEGOTIATION,
    CLOSED_WON,
    CLOSED_LOST
}
