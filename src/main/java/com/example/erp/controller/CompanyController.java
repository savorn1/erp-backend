package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CompanyFilterRequest;
import com.example.erp.dto.CompanyResponse;
import com.example.erp.dto.CreateCompanyRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.UpdateCompanyRequest;
import com.example.erp.dto.UpdateCompanyStatusRequest;
import com.example.erp.service.CompanyService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

// Admin-only company management — legal entities the ERP tracks records for.
// Kept admin-only (like UserController) rather than opened up for read access
// by any authenticated user; broaden with an explicit @PreAuthorize override
// here if a later feature needs to read company info as a non-admin.
@RestController
@RequestMapping("/api/admin/companies")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<PageResponse<CompanyResponse>> list(@ModelAttribute CompanyFilterRequest filter) {
        return ResponseEntity.ok(companyService.listCompanies(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(companyService.getCompany(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> create(@Valid @RequestBody CreateCompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Company created", companyService.createCompany(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CompanyResponse>> update(@PathVariable Long id,
                                                                 @Valid @RequestBody UpdateCompanyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Company updated", companyService.updateCompany(id, request)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateStatus(@PathVariable Long id,
                                                                        @Valid @RequestBody UpdateCompanyStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Status updated", companyService.updateStatus(id, request)));
    }

    @PostMapping(value = "/{id}/logo", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CompanyResponse>> uploadLogo(@PathVariable Long id,
                                                                      @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("Logo updated", companyService.updateLogo(id, file)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(ApiResponse.success("Company deleted", null));
    }
}
