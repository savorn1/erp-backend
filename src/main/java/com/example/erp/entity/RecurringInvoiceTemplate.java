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

// A billing schedule that "Generate due invoices" (see
// RecurringInvoiceServiceImpl.generateDueInvoices) turns into a real
// SalesOrder -> Invoice each time nextRunDate comes due — there is no
// background job anywhere in this codebase, so this is always triggered by
// an admin clicking the button, same as Auto-Replenishment's
// generate-from-low-stock. Line items live in RecurringInvoiceTemplateLine.
@Entity
@Table(name = "recurring_invoice_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringInvoiceTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // Source warehouse for the SalesOrder generated each run.
    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurringInvoiceFrequency frequency;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "next_run_date", nullable = false)
    private LocalDate nextRunDate;

    @Column(name = "last_generated_date")
    private LocalDate lastGeneratedDate;

    // Null means indefinite.
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "auto_approve_invoice", nullable = false)
    @Builder.Default
    private boolean autoApproveInvoice = true;

    @Column(name = "auto_email_invoice", nullable = false)
    @Builder.Default
    private boolean autoEmailInvoice = false;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;
}
