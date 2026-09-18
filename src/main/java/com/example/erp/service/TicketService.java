package com.example.erp.service;

import com.example.erp.dto.CreateTicketCommentRequest;
import com.example.erp.dto.CreateTicketRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.TicketFilterRequest;
import com.example.erp.dto.TicketResponse;
import com.example.erp.dto.UpdateTicketRequest;

public interface TicketService {

    PageResponse<TicketResponse> listTickets(TicketFilterRequest filter);

    TicketResponse getTicket(Long id);

    TicketResponse createTicket(CreateTicketRequest request, String actingUsername);

    TicketResponse updateTicket(Long id, UpdateTicketRequest request);

    TicketResponse addComment(Long id, CreateTicketCommentRequest request, String actingUsername);

    TicketResponse startProgress(Long id);

    TicketResponse resolveTicket(Long id);

    TicketResponse closeTicket(Long id);

    TicketResponse reopenTicket(Long id);

    void deleteTicket(Long id);
}
