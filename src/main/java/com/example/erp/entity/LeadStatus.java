package com.example.erp.entity;

// The unified pipeline, from a fresh contact through to a closed deal —
// formerly split across LeadStatus (NEW..CONVERTED/LOST) and a separate
// OpportunityStage; merged into one flow (see LeadOpportunityMergeMigration
// for the one-time data migration off the old two-entity shape). WON/LOST
// are terminal, reached only via LeadService.winLead/loseLead — never
// through the generic updateStatus endpoint (see its guard).
public enum LeadStatus {
    NEW,
    QUALIFIED,
    NEEDS_ANALYSIS,
    QUOTATION,
    NEGOTIATION,
    WON,
    LOST
}
