package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;

// A customer support ticket — a long-lived, mutable record (unlike RMA/
// CreditNote's "create = immediate effect, then append-only" convention).
// Editable while OPEN/IN_PROGRESS; comments (see TicketComment) are the
// append-only part.
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    private static final Map<TicketPriority, Integer> TARGET_DAYS = Map.of(
            TicketPriority.URGENT, 1,
            TicketPriority.HIGH, 3,
            TicketPriority.MEDIUM, 7,
            TicketPriority.LOW, 14
    );

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // Optional — which product the ticket is about, if any.
    @Column(name = "product_id")
    private Long productId;

    // The support agent working this ticket — plain FK, no relation, same
    // convention as Lead.assignedToUserId / Opportunity.assignedToUserId.
    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;

    @Column(unique = true)
    private String ticketNumber;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TicketPriority priority = TicketPriority.MEDIUM;

    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime closedAt;

    // Only OPEN/IN_PROGRESS tickets can be overdue — once resolved/closed the
    // clock that matters is how long it took, not how long it's been open.
    public boolean isOverdue() {
        if (status != TicketStatus.OPEN && status != TicketStatus.IN_PROGRESS) return false;
        return ChronoUnit.DAYS.between(createdAt.toLocalDate(), LocalDate.now()) > TARGET_DAYS.get(priority);
    }
}
