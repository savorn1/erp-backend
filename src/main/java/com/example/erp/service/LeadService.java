package com.example.erp.service;

import com.example.erp.dto.AddLeadFollowUpRequest;
import com.example.erp.dto.AddLeadNoteRequest;
import com.example.erp.dto.AssignLeadRequest;
import com.example.erp.dto.CreateLeadRequest;
import com.example.erp.dto.LeadActivityFilterRequest;
import com.example.erp.dto.LeadActivityResponse;
import com.example.erp.dto.LeadFilterRequest;
import com.example.erp.dto.LeadResponse;
import com.example.erp.dto.LoseLeadRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateLeadRequest;
import com.example.erp.dto.UpdateLeadStatusRequest;

public interface LeadService {

    PageResponse<LeadResponse> listLeads(LeadFilterRequest filter);

    LeadResponse getLead(Long id);

    LeadResponse createLead(CreateLeadRequest request, String actingUsername);

    LeadResponse updateLead(Long id, UpdateLeadRequest request);

    LeadResponse updateStatus(Long id, UpdateLeadStatusRequest request, String actingUsername);

    LeadResponse assignSalesperson(Long id, AssignLeadRequest request, String actingUsername);

    // Absorbed from the old OpportunityService — closes the deal, creating a
    // Customer if none is linked yet.
    LeadResponse winLead(Long id, String actingUsername);

    LeadResponse loseLead(Long id, LoseLeadRequest request, String actingUsername);

    void deleteLead(Long id);

    PageResponse<LeadActivityResponse> listActivities(Long leadId, LeadActivityFilterRequest filter);

    LeadActivityResponse addNote(Long leadId, AddLeadNoteRequest request, String actingUsername);

    LeadActivityResponse addFollowUp(Long leadId, AddLeadFollowUpRequest request, String actingUsername);
}
