package com.example.erp.entity;

// Absorbs the old OpportunityActivityType's WON/LOST/QUOTATION_CREATED (via
// LeadOpportunityMergeMigration) now that Lead covers the whole pipeline.
// CONVERTED is dropped — there's no separate conversion step anymore.
public enum LeadActivityType {
    CREATED,
    STATUS_CHANGE,
    ASSIGNED,
    FOLLOW_UP,
    NOTE,
    WON,
    LOST,
    QUOTATION_CREATED
}
