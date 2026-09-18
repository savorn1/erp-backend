package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreatePettyCashEntryRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PettyCashEntryFilterRequest;
import com.example.erp.dto.PettyCashEntryResponse;
import com.example.erp.dto.PettyCashSummaryResponse;
import com.example.erp.service.PettyCashService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Append-only petty cash ledger — no update/delete, a mistake is corrected
// with an offsetting entry, same as a real petty cash book (see
// PettyCashEntry's own comment).
@RestController
@RequestMapping("/api/admin/petty-cash")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class PettyCashController {

    private final PettyCashService pettyCashService;

    @GetMapping
    public ResponseEntity<PageResponse<PettyCashEntryResponse>> list(@ModelAttribute PettyCashEntryFilterRequest filter) {
        return ResponseEntity.ok(pettyCashService.listEntries(filter));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PettyCashSummaryResponse>> summary(@RequestParam Long companyId) {
        return ResponseEntity.ok(ApiResponse.success(pettyCashService.getSummary(companyId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PettyCashEntryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(pettyCashService.getEntry(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PettyCashEntryResponse>> create(@Valid @RequestBody CreatePettyCashEntryRequest request,
                                                                       Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(pettyCashService.createEntry(request, authentication.getName())));
    }
}
