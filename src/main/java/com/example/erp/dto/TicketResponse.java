package com.example.erp.dto;

import com.example.erp.entity.TicketPriority;
import com.example.erp.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private Long companyId;
    private Long customerId;
    private String customerName;
    private Long productId;
    private String productName;
    private String ticketNumber;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private Long assignedToUserId;
    private String assignedToUsername;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime closedAt;
    // Days from createdAt to now (open tickets) or to closedAt (closed ones).
    private long daysOpen;
    private boolean overdue;
    private List<TicketCommentResponse> comments;
}
