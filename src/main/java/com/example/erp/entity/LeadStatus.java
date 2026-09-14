package com.example.erp.entity;

// CONVERTED and LOST are terminal — reached only via LeadService.convertLead
// and a direct status update respectively; CONVERTED can only be reached
// through convertLead (see LeadServiceImpl.updateStatus's guard).
public enum LeadStatus {
    NEW,
    CONTACTED,
    QUALIFIED,
    PROPOSAL,
    NEGOTIATION,
    CONVERTED,
    LOST
}
