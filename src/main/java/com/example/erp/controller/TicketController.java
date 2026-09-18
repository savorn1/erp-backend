package com.example.erp.controller;

import com.example.erp.dto.ApiResponse;
import com.example.erp.dto.CreateTicketCommentRequest;
import com.example.erp.dto.CreateTicketRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.TicketFilterRequest;
import com.example.erp.dto.TicketResponse;
import com.example.erp.dto.UpdateTicketRequest;
import com.example.erp.exception.AppException;
import com.example.erp.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tickets")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class TicketController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<PageResponse<TicketResponse>> list(@ModelAttribute TicketFilterRequest filter) {
        return ResponseEntity.ok(ticketService.listTickets(filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ticketService.getTicket(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> create(@Valid @RequestBody CreateTicketRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created", ticketService.createTicket(request, requireUsername(authentication))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> update(@PathVariable Long id, @Valid @RequestBody UpdateTicketRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Ticket updated", ticketService.updateTicket(id, request)));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<TicketResponse>> addComment(@PathVariable Long id, @Valid @RequestBody CreateTicketCommentRequest request,
                                                                    Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success("Comment added", ticketService.addComment(id, request, requireUsername(authentication))));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<TicketResponse>> start(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Ticket started", ticketService.startProgress(id)));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<TicketResponse>> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Ticket resolved", ticketService.resolveTicket(id)));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<ApiResponse<TicketResponse>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Ticket closed", ticketService.closeTicket(id)));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<TicketResponse>> reopen(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Ticket reopened", ticketService.reopenTicket(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket deleted", null));
    }

    private String requireUsername(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
