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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private String contactName;

    // The lead's own business, if this is a B2B lead — not to be confused
    // with companyId/companyName above, which is the ERP tenant.
    private String organizationName;

    private String email;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    // The salesperson (User.id) this lead is assigned to — see
    // LeadServiceImpl.assignSalesperson.
    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;

    @Column(precision = 19, scale = 4)
    private BigDecimal estimatedValue;

    @Column(columnDefinition = "text")
    private String notes;

    // Legacy — predates the Lead/Opportunity merge (see
    // LeadOpportunityMergeMigration). Left in place, unwritten by any
    // current code, per the "never destructively alter columns" convention.
    @Column(name = "converted_customer_id")
    private Long convertedCustomerId;

    @Column(name = "converted_opportunity_id")
    private Long convertedOpportunityId;

    private LocalDateTime convertedAt;

    private String createdBy;

    // Set/updated by LeadServiceImpl.addFollowUp — separate from the
    // historical LeadActivity log, this is the one scheduled "next contact"
    // date, surfaced by isFollowUpDue() and NotificationServiceImpl.
    @Column(name = "next_follow_up_date")
    private LocalDate nextFollowUpDate;

    // ── Deal-specific fields, absorbed from the old Opportunity entity ──
    // All nullable: a fresh NEW lead has none of these yet.

    // The deal's own label (e.g. "Acme Corp — Q4 renewal"), distinct from
    // contactName (the person). Falls back to contactName for display
    // wherever unset.
    @Column(name = "deal_name")
    private String dealName;

    // The firmer deal value once qualified — kept separate from
    // estimatedValue, which is the lead's own early guess.
    @Column(precision = 19, scale = 4)
    private BigDecimal amount;

    // 0-100.
    private Integer probability;

    private LocalDate expectedCloseDate;

    // Set for an upsell-style deal against an existing customer, or by
    // winLead() when it creates a new Customer.
    @Column(name = "customer_id")
    private Long customerId;

    private LocalDateTime closedAt;

    // Mirrors Ticket.isOverdue() — due once the scheduled date has arrived,
    // but never for a lead that's already left the active funnel.
    public boolean isFollowUpDue() {
        if (nextFollowUpDate == null) return false;
        if (status == LeadStatus.WON || status == LeadStatus.LOST) return false;
        return !nextFollowUpDate.isAfter(LocalDate.now());
    }
}
