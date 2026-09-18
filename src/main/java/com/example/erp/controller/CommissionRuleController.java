package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CommissionRuleFilterRequest;
import com.example.erp.dto.CommissionRuleRequest;
import com.example.erp.dto.CommissionRuleResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.CommissionRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/commission-rules")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class CommissionRuleController {

    private final CommissionRuleService service;

    @GetMapping
    public ResponseEntity<PageResponse<CommissionRuleResponse>> list(@ModelAttribute CommissionRuleFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommissionRuleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommissionRuleResponse>> create(@Valid @RequestBody CommissionRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Commission rule created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommissionRuleResponse>> update(@PathVariable Long id,
                                                                       @Valid @RequestBody CommissionRuleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Commission rule updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Commission rule deleted", null));
    }
}
