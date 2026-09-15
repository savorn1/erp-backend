package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.JournalFilterRequest;
import com.example.erp.dto.JournalRequest;
import com.example.erp.dto.JournalResponse;
import com.example.erp.dto.PageResponse;
import com.example.erp.service.JournalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/journals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class JournalController {

    private final JournalService journalService;

    @GetMapping
    public ResponseEntity<PageResponse<JournalResponse>> list(@ModelAttribute JournalFilterRequest filter) {
        return ResponseEntity.ok(journalService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(journalService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JournalResponse>> create(@Valid @RequestBody JournalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Journal created", journalService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JournalResponse>> update(@PathVariable Long id, @Valid @RequestBody JournalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Journal updated", journalService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        journalService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Journal deleted", null));
    }
}
