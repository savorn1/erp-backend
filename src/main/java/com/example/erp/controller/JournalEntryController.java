package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.JournalEntryFilterRequest;
import com.example.erp.dto.JournalEntryRequest;
import com.example.erp.dto.JournalEntryResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.JournalEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/journal-entries")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class JournalEntryController {

    private final JournalEntryService journalEntryService;

    @GetMapping
    public ResponseEntity<PageResponse<JournalEntryResponse>> list(@ModelAttribute JournalEntryFilterRequest filter) {
        return ResponseEntity.ok(journalEntryService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(journalEntryService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JournalEntryResponse>> create(@Valid @RequestBody JournalEntryRequest request,
                                                                        Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Journal entry created", journalEntryService.create(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> update(@PathVariable Long id, @Valid @RequestBody JournalEntryRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Journal entry updated", journalEntryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        journalEntryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Journal entry deleted", null));
    }

    @PostMapping("/{id}/post")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> post(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Journal entry posted", journalEntryService.post(id, requireUsername(authentication))));
    }

    @PostMapping("/{id}/reverse")
    public ResponseEntity<ApiResponse<JournalEntryResponse>> reverse(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reversing journal entry created", journalEntryService.reverse(id, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
