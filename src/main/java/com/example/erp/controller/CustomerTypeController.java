package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CustomerTypeFilterRequest;
import com.example.erp.dto.CustomerTypeRequest;
import com.example.erp.dto.CustomerTypeResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.CustomerTypeService;
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
@RequestMapping("/api/admin/customer-types")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CustomerTypeController {

    private final CustomerTypeService service;

    @GetMapping
    public ResponseEntity<PageResponse<CustomerTypeResponse>> list(@ModelAttribute CustomerTypeFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerTypeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerTypeResponse>> create(@Valid @RequestBody CustomerTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Customer type created", service.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerTypeResponse>> update(@PathVariable Long id,
                                                                       @Valid @RequestBody CustomerTypeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Customer type updated", service.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Customer type deleted", null));
    }
}
