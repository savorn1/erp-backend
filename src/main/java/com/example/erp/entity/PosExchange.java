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
import java.time.LocalDateTime;

// A partial-line return from a completed PosSale, optionally bundled with
// new item(s), netted into a single signed settlement. netAmount > 0 means
// the customer paid the difference; < 0 means the store refunded it; = 0 is
// an even swap (settlementMethod stays null). No void/reverse path — a
// mistaken exchange is corrected with a second, opposite-direction exchange
// (same convention as PosSale.voidSale having no "unvoid").
@Entity
@Table(name = "pos_exchanges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosExchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "register_id", nullable = false)
    private Long registerId;

    @Column(name = "pos_session_id", nullable = false)
    private Long posSessionId;

    @Column(name = "original_pos_sale_id", nullable = false)
    private Long originalPosSaleId;

    @Column(unique = true)
    private String exchangeNumber;

    @Column(name = "exchange_date", nullable = false)
    private LocalDateTime exchangeDate;

    @Column(name = "return_value", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal returnValue = BigDecimal.ZERO;

    @Column(name = "return_tax_value", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal returnTaxValue = BigDecimal.ZERO;

    @Column(name = "new_value", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal newValue = BigDecimal.ZERO;

    @Column(name = "new_tax_value", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal newTaxValue = BigDecimal.ZERO;

    // (newValue+newTaxValue) - (returnValue+returnTaxValue). Positive =
    // customer owed the store; negative = store refunded the customer.
    @Column(name = "net_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal netAmount = BigDecimal.ZERO;

    // Null only when netAmount is zero (even swap).
    @Enumerated(EnumType.STRING)
    @Column(name = "settlement_method")
    private PaymentMethod settlementMethod;

    // Signed, mirrors netAmount exactly.
    @Column(name = "settlement_amount", precision = 19, scale = 4)
    private BigDecimal settlementAmount;

    @Column(name = "settlement_reference")
    private String settlementReference;

    @Column(name = "created_by")
    private String createdBy;
}
