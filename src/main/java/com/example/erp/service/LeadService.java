package com.example.erp.service;

import com.example.erp.dto.AddLeadFollowUpRequest;
import com.example.erp.dto.AssignLeadRequest;
import com.example.erp.dto.CreateLeadRequest;
import com.example.erp.dto.LeadActivityFilterRequest;
import com.example.erp.dto.LeadActivityResponse;
import com.example.erp.dto.LeadFilterRequest;
import com.example.erp.dto.LeadResponse;
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

    LeadResponse convertLead(Long id, String actingUsername);

    void deleteLead(Long id);

    PageResponse<LeadActivityResponse> listActivities(Long leadId, LeadActivityFilterRequest filter);

    LeadActivityResponse addFollowUp(Long leadId, AddLeadFollowUpRequest request, String actingUsername);
}
