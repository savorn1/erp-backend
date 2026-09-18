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

// A flat commission rate for a salesperson, applied to the paid portion of
// invoices raised against sales orders they're credited on (SalesOrder.
// salesRepUserId) — see CommissionEntry/PaymentServiceImpl for how it's
// consumed. userId == null is the company-wide default rate, used when a
// salesperson has no rule of their own; at most one default rule per
// company is enforced by CommissionRuleServiceImpl.
@Entity
@Table(name = "commission_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "rate_percent", nullable = false, precision = 19, scale = 4)
    private BigDecimal ratePercent;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
