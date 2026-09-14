package com.example.erp.service;

import com.example.erp.dto.AddOpportunityFollowUpRequest;
import com.example.erp.dto.AddOpportunityNoteRequest;
import com.example.erp.dto.CreateOpportunityRequest;
import com.example.erp.dto.LoseOpportunityRequest;
import com.example.erp.dto.OpportunityActivityFilterRequest;
import com.example.erp.dto.OpportunityActivityResponse;
import com.example.erp.dto.OpportunityFilterRequest;
import com.example.erp.dto.OpportunityResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateOpportunityRequest;
import com.example.erp.dto.UpdateOpportunityStageRequest;

public interface OpportunityService {

    PageResponse<OpportunityResponse> listOpportunities(OpportunityFilterRequest filter);

    OpportunityResponse getOpportunity(Long id);

    OpportunityResponse createOpportunity(CreateOpportunityRequest request, String actingUsername);

    OpportunityResponse updateOpportunity(Long id, UpdateOpportunityRequest request);

    OpportunityResponse updateStage(Long id, UpdateOpportunityStageRequest request, String actingUsername);

    OpportunityResponse winOpportunity(Long id, String actingUsername);

    OpportunityResponse loseOpportunity(Long id, LoseOpportunityRequest request, String actingUsername);

    void deleteOpportunity(Long id);

    PageResponse<OpportunityActivityResponse> listActivities(Long opportunityId, OpportunityActivityFilterRequest filter);

    OpportunityActivityResponse addNote(Long opportunityId, AddOpportunityNoteRequest request, String actingUsername);

    OpportunityActivityResponse addFollowUp(Long opportunityId, AddOpportunityFollowUpRequest request, String actingUsername);
}
