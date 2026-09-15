package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CustomRoleFilterRequest;
import com.example.erp.dto.CustomRoleRequest;
import com.example.erp.dto.CustomRoleResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.CustomRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/custom-roles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class CustomRoleController {

    private final CustomRoleService customRoleService;

    @GetMapping
    public ResponseEntity<PageResponse<CustomRoleResponse>> list(@ModelAttribute CustomRoleFilterRequest filter) {
        return ResponseEntity.ok(customRoleService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomRoleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(customRoleService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CustomRoleResponse>> create(@Valid @RequestBody CustomRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Custom role created", customRoleService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomRoleResponse>> update(@PathVariable Long id, @Valid @RequestBody CustomRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Custom role updated", customRoleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        customRoleService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Custom role deleted", null));
    }
}
