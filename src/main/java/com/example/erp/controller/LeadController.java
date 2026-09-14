package com.example.erp.controller;

import com.example.erp.dto.AddLeadFollowUpRequest;
import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.AssignLeadRequest;
import com.example.erp.dto.CreateLeadRequest;
import com.example.erp.dto.LeadActivityFilterRequest;
import com.example.erp.dto.LeadActivityResponse;
import com.example.erp.dto.LeadFilterRequest;
import com.example.erp.dto.LeadResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateLeadRequest;
import com.example.erp.dto.UpdateLeadStatusRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.LeadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

// Admin-only lead management. Status changes, assignment, follow-ups, and
// conversion are attributed to the acting admin and logged as LeadActivity
// entries (see GET .../{id}/activities).
@RestController
@RequestMapping("/api/admin/leads")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LeadController {

    private final LeadService leadService;

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

    @PostMapping("/{id}/convert")
    public ResponseEntity<ApiResponse<LeadResponse>> convert(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Lead converted to customer",
                leadService.convertLead(id, requireUsername(authentication))));
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

    @PostMapping("/{id}/activities")
    public ResponseEntity<ApiResponse<LeadActivityResponse>> addFollowUp(@PathVariable Long id,
                                                                             @Valid @RequestBody AddLeadFollowUpRequest request,
                                                                             Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Follow-up added", leadService.addFollowUp(id, request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
