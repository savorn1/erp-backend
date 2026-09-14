package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UomCategoryFilterRequest;
import com.example.erp.dto.UomCategoryRequest;
import com.example.erp.dto.UomCategoryResponse;
import com.example.erp.service.UomCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/uom-categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UomCategoryController {

    private final UomCategoryService uomCategoryService;

    @GetMapping
    public ResponseEntity<PageResponse<UomCategoryResponse>> list(@ModelAttribute UomCategoryFilterRequest filter) {
        return ResponseEntity.ok(uomCategoryService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UomCategoryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(uomCategoryService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UomCategoryResponse>> create(@Valid @RequestBody UomCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("UOM category created", uomCategoryService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UomCategoryResponse>> update(@PathVariable Long id, @Valid @RequestBody UomCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UOM category updated", uomCategoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        uomCategoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("UOM category deleted", null));
    }

    @PostMapping("/seed-standard")
    public ResponseEntity<ApiResponse<List<UomCategoryResponse>>> seedStandard() {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Standard UOM categories created", uomCategoryService.seedStandardCategories()));
    }
}
