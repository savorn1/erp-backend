package com.example.erp.controller;

import com.example.erp.dto.AddLeadFollowUpRequest;
import com.example.erp.dto.AddLeadNoteRequest;
import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.AssignLeadRequest;
import com.example.erp.dto.ConvertLeadToQuotationRequest;
import com.example.erp.dto.CreateLeadRequest;
import com.example.erp.dto.LeadActivityFilterRequest;
import com.example.erp.dto.LeadActivityResponse;
import com.example.erp.dto.LeadFilterRequest;
import com.example.erp.dto.LeadResponse;
import com.example.erp.dto.LoseLeadRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.QuotationResponse;
import com.example.erp.dto.UpdateLeadRequest;
import com.example.erp.dto.UpdateLeadStatusRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.LeadService;
import com.example.erp.service.QuotationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Admin-only lead management — covers the whole pipeline from a fresh
// contact through to a closed deal (formerly split across a separate Lead
// and Opportunity; see LeadOpportunityMergeMigration). Status changes,
// assignment, wins/losses, notes, and follow-ups are all logged as
// LeadActivity entries (see GET .../{id}/activities).
@RestController
@RequestMapping("/api/admin/leads")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class LeadController {

    private final LeadService leadService;
    private final QuotationService quotationService;

    @GetMapping
    public ResponseEntity<PageResponse<LeadResponse>> list(@ModelAttribute LeadFilterRequest filter) {
        return ResponseEntity.ok(leadService.listLeads(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(leadService.getLead(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LeadResponse>> create(@Valid @RequestBody CreateLeadRequest request,
                                                                Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Lead created", leadService.createLead(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LeadResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateLeadRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Lead updated", leadService.updateLead(id, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LeadResponse>> updateStatus(@PathVariable Long id,
                                                                      @Valid @RequestBody UpdateLeadStatusRequest request,
                                                                      Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                leadService.updateStatus(id, request, requireUsername(authentication))));
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<ApiResponse<LeadResponse>> assign(@PathVariable Long id,
                                                                @Valid @RequestBody AssignLeadRequest request,
                                                                Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Lead assigned",
                leadService.assignSalesperson(id, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/win")
    public ResponseEntity<ApiResponse<LeadResponse>> win(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Lead won",
                leadService.winLead(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/lose")
    public ResponseEntity<ApiResponse<LeadResponse>> lose(@PathVariable Long id,
                                                              @RequestBody(required = false) LoseLeadRequest request,
                                                              Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Lead lost",
                leadService.loseLead(id, request == null ? new LoseLeadRequest() : request, requireUsername(authentication))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(ApiResponse.success("Lead deleted", null));
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<PageResponse<LeadActivityResponse>> listActivities(@PathVariable Long id,
                                                                                 @ModelAttribute LeadActivityFilterRequest filter) {
        return ResponseEntity.ok(leadService.listActivities(id, filter));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<ApiResponse<LeadActivityResponse>> addNote(@PathVariable Long id,
                                                                         @Valid @RequestBody AddLeadNoteRequest request,
                                                                         Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note added", leadService.addNote(id, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/follow-ups")
    public ResponseEntity<ApiResponse<LeadActivityResponse>> addFollowUp(@PathVariable Long id,
                                                                             @Valid @RequestBody AddLeadFollowUpRequest request,
                                                                             Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Follow-up added", leadService.addFollowUp(id, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/convert-to-quotation")
    public ResponseEntity<ApiResponse<QuotationResponse>> convertToQuotation(@PathVariable Long id,
                                                                                 @Valid @RequestBody ConvertLeadToQuotationRequest request,
                                                                                 Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quotation created", quotationService.createFromLead(id, request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
