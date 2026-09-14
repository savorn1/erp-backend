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

import java.time.LocalDate;

// A logged follow-up on an overdue (or any) customer invoice — a phone call,
// email, letter, etc. chasing payment. Not tied to invoice status: notes can
// still be added after the invoice is paid off, for a complete history.
@Entity
@Table(name = "collection_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CollectionActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private LocalDate activityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CollectionContactMethod method;

    @Column(columnDefinition = "text")
    private String notes;

    // When to follow up again — optional.
    private LocalDate followUpDate;

    @Builder.Default
    private boolean resolved = false;

    private String createdBy;
}
