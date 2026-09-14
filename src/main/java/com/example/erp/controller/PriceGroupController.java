package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PriceGroupFilterRequest;
import com.example.erp.dto.PriceGroupRequest;
import com.example.erp.dto.PriceGroupResponse;
import com.example.erp.service.PriceGroupService;
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
@RequestMapping("/api/admin/price-groups")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PriceGroupController {

    private final PriceGroupService service;

    @GetMapping
    public ResponseEntity<PageResponse<PriceGroupResponse>> list(@ModelAttribute PriceGroupFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PriceGroupResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PriceGroupResponse>> create(@Valid @RequestBody PriceGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Price group created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PriceGroupResponse>> update(@PathVariable Long id,
                                                                      @Valid @RequestBody PriceGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Price group updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Price group deleted", null));
    }
}
