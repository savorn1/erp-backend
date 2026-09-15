package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PostingRuleRequest;
import com.example.erp.dto.PostingRuleResponse;
import com.example.erp.service.PostingRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/posting-rules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PostingRuleController {

    private final PostingRuleService postingRuleService;

    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<PostingRuleResponse>> getForCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(postingRuleService.getForCompany(companyId)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PostingRuleResponse>> upsert(@Valid @RequestBody PostingRuleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Posting rules saved", postingRuleService.upsert(request)));
    }

    @PostMapping("/seed/{companyId}")
    public ResponseEntity<ApiResponse<PostingRuleResponse>> seed(@PathVariable Long companyId) {
        return ResponseEntity.ok(ApiResponse.success("Posting rules seeded from the standard chart of accounts",
                postingRuleService.seedFromChartOfAccounts(companyId)));
    }
}
