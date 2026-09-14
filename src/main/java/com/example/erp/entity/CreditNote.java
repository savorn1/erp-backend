package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

// An immutable corrective document against an approved Invoice — issuing one
// immediately credits the customer's balance (see
// CreditNoteServiceImpl.createCreditNote), same "create = immediate effect"
// reasoning as GoodsReceipt/Delivery rather than a draft/approve workflow.
@Entity
@Table(name = "credit_notes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(unique = true)
    private String creditNoteNumber;

    @Column(nullable = false)
    private LocalDate creditNoteDate;

    @Column(columnDefinition = "text")
    private String reason;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    private String createdBy;
}
