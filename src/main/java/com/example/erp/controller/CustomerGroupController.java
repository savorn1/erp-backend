package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CustomerGroupFilterRequest;
import com.example.erp.dto.CustomerGroupRequest;
import com.example.erp.dto.CustomerGroupResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.CustomerGroupService;
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
@RequestMapping("/api/admin/customer-groups")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class CustomerGroupController {

    private final CustomerGroupService service;

    @GetMapping
    public ResponseEntity<PageResponse<CustomerGroupResponse>> list(@ModelAttribute CustomerGroupFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerGroupResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerGroupResponse>> create(@Valid @RequestBody CustomerGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Customer group created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerGroupResponse>> update(@PathVariable Long id,
                                                                        @Valid @RequestBody CustomerGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Customer group updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Customer group deleted", null));
    }
}
