package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.RegisterFilterRequest;
import com.example.erp.dto.RegisterRequest;
import com.example.erp.dto.RegisterResponse;
import com.example.erp.service.RegisterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/registers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class RegisterController {

    private final RegisterService registerService;

    @GetMapping
    public ResponseEntity<PageResponse<RegisterResponse>> list(@ModelAttribute RegisterFilterRequest filter) {
        return ResponseEntity.ok(registerService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RegisterResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(registerService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RegisterResponse>> create(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Register created", registerService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RegisterResponse>> update(@PathVariable Long id, @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Register updated", registerService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        registerService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Register deleted", null));
    }
}
