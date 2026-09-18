package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.ApprovalRuleFilterRequest;
import com.example.erp.dto.ApprovalRuleRequest;
import com.example.erp.dto.ApprovalRuleResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.ApprovalRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/approval-rules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class ApprovalRuleController {

    private final ApprovalRuleService approvalRuleService;

    @GetMapping
    public ResponseEntity<PageResponse<ApprovalRuleResponse>> list(@ModelAttribute ApprovalRuleFilterRequest filter) {
        return ResponseEntity.ok(approvalRuleService.listRules(filter));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApprovalRuleResponse>> create(@Valid @RequestBody ApprovalRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Approval rule created", approvalRuleService.createRule(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ApprovalRuleResponse>> update(@PathVariable Long id, @Valid @RequestBody ApprovalRuleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Approval rule updated", approvalRuleService.updateRule(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        approvalRuleService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success("Approval rule deleted", null));
    }
}
