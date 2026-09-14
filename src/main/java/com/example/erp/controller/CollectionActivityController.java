package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CollectionActivityFilterRequest;
import com.example.erp.dto.CollectionActivityRequest;
import com.example.erp.dto.CollectionActivityResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.CollectionActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/collection-activities")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CollectionActivityController {

    private final CollectionActivityService collectionActivityService;

    @GetMapping
    public ResponseEntity<PageResponse<CollectionActivityResponse>> list(@ModelAttribute CollectionActivityFilterRequest filter) {
        return ResponseEntity.ok(collectionActivityService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CollectionActivityResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(collectionActivityService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CollectionActivityResponse>> create(@Valid @RequestBody CollectionActivityRequest request,
                                                                              Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Collection activity logged", collectionActivityService.create(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CollectionActivityResponse>> update(@PathVariable Long id,
                                                                              @Valid @RequestBody CollectionActivityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Collection activity updated", collectionActivityService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        collectionActivityService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Collection activity deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
