package com.example.erp.controller;

import com.example.erp.dto.AddOpportunityFollowUpRequest;
import com.example.erp.dto.AddOpportunityNoteRequest;
import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.ConvertOpportunityToQuotationRequest;
import com.example.erp.dto.CreateOpportunityRequest;
import com.example.erp.dto.LoseOpportunityRequest;
import com.example.erp.dto.OpportunityActivityFilterRequest;
import com.example.erp.dto.OpportunityActivityResponse;
import com.example.erp.dto.OpportunityFilterRequest;
import com.example.erp.dto.OpportunityResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.QuotationResponse;
import com.example.erp.dto.UpdateOpportunityRequest;
import com.example.erp.dto.UpdateOpportunityStageRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.OpportunityService;
import com.example.erp.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Admin-only opportunity management — the deal-in-progress stage between a
// qualified Lead and a won Customer (see LeadController.convert). Stage
// changes, wins, losses, and notes are logged as OpportunityActivity entries.
@RestController
@RequestMapping("/api/admin/opportunities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class OpportunityController {

    private final OpportunityService opportunityService;
    private final QuotationService quotationService;

    @GetMapping
    public ResponseEntity<PageResponse<OpportunityResponse>> list(@ModelAttribute OpportunityFilterRequest filter) {
        return ResponseEntity.ok(opportunityService.listOpportunities(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OpportunityResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(opportunityService.getOpportunity(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OpportunityResponse>> create(@Valid @RequestBody CreateOpportunityRequest request,
                                                                       Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Opportunity created", opportunityService.createOpportunity(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<OpportunityResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateOpportunityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Opportunity updated", opportunityService.updateOpportunity(id, request)));
    }

    @PutMapping("/{id}/stage")
    public ResponseEntity<ApiResponse<OpportunityResponse>> updateStage(@PathVariable Long id,
                                                                            @Valid @RequestBody UpdateOpportunityStageRequest request,
                                                                            Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Stage updated",
                opportunityService.updateStage(id, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/win")
    public ResponseEntity<ApiResponse<OpportunityResponse>> win(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Opportunity won",
                opportunityService.winOpportunity(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/lose")
    public ResponseEntity<ApiResponse<OpportunityResponse>> lose(@PathVariable Long id,
                                                                     @RequestBody(required = false) LoseOpportunityRequest request,
                                                                     Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Opportunity lost",
                opportunityService.loseOpportunity(id, request == null ? new LoseOpportunityRequest() : request, requireUsername(authentication))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        opportunityService.deleteOpportunity(id);
        return ResponseEntity.ok(ApiResponse.success("Opportunity deleted", null));
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<PageResponse<OpportunityActivityResponse>> listActivities(@PathVariable Long id,
                                                                                        @ModelAttribute OpportunityActivityFilterRequest filter) {
        return ResponseEntity.ok(opportunityService.listActivities(id, filter));
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<OpportunityActivityResponse>> addNote(@PathVariable Long id,
                                                                                @Valid @RequestBody AddOpportunityNoteRequest request,
                                                                                Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note added", opportunityService.addNote(id, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/follow-ups")
    public ResponseEntity<ApiResponse<OpportunityActivityResponse>> addFollowUp(@PathVariable Long id,
                                                                                    @Valid @RequestBody AddOpportunityFollowUpRequest request,
                                                                                    Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Follow-up added", opportunityService.addFollowUp(id, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/convert-to-quotation")
    public ResponseEntity<ApiResponse<QuotationResponse>> convertToQuotation(@PathVariable Long id,
                                                                                 @Valid @RequestBody ConvertOpportunityToQuotationRequest request,
                                                                                 Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation created", quotationService.createFromOpportunity(id, request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
