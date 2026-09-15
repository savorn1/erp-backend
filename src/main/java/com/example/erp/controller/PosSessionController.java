package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.ClosePosSessionRequest;
import com.example.erp.dto.OpenPosSessionRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.PosSessionFilterRequest;
import com.example.erp.dto.PosSessionResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.PosSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pos-sessions")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class PosSessionController {

    private final PosSessionService posSessionService;

    @GetMapping
    public ResponseEntity<PageResponse<PosSessionResponse>> list(@ModelAttribute PosSessionFilterRequest filter) {
        return ResponseEntity.ok(posSessionService.list(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PosSessionResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(posSessionService.get(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PosSessionResponse>> open(@Valid @RequestBody OpenPosSessionRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Session opened", posSessionService.open(request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<PosSessionResponse>> close(@PathVariable Long id, @Valid @RequestBody ClosePosSessionRequest request,
                                                                    Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Session closed", posSessionService.close(id, request, requireUsername(authentication))));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
