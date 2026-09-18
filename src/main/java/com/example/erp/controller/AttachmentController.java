package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.AttachmentResponse;
import com.example.erp.dto.CreateAttachmentRequest;
import com.example.erp.service.AttachmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// The metadata layer over the generic S3 upload primitive (FileController /
// FileStorageService) — the frontend uploads bytes via POST /api/files/upload
// first, then records the resulting key/url against an owner record here.
@RestController
@RequestMapping("/api/admin/attachments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttachmentResponse>>> list(@RequestParam String ownerType, @RequestParam Long ownerId) {
        return ResponseEntity.ok(ApiResponse.success(attachmentService.list(ownerType, ownerId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttachmentResponse>> create(@Valid @RequestBody CreateAttachmentRequest request,
                                                                   Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(attachmentService.create(request, authentication.getName())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        attachmentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
