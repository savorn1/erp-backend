package com.example.erp.controller;

import com.example.erp.dto.PageResponse;
import com.example.erp.dto.SerialNumberFilterRequest;
import com.example.erp.dto.SerialNumberResponse;
import com.example.erp.service.SerialNumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Read-only — serial numbers are only ever created by goods receipt posting
// for SERIAL-tracked products (see GoodsReceiptServiceImpl).
@RestController
@RequestMapping("/api/admin/serial-numbers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SerialNumberController {

    private final SerialNumberService service;

    @GetMapping
    public ResponseEntity<PageResponse<SerialNumberResponse>> list(@ModelAttribute SerialNumberFilterRequest filter) {
        return ResponseEntity.ok(service.list(filter));
    }
}
