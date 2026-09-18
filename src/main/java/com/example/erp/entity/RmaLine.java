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

// One returned item on an RmaRequest. invoiceLineId is optional — when set,
// unitPrice is captured from that InvoiceLine at creation time (so the
// refund total matches what the customer actually paid); when unset, the
// creator must supply unitPrice directly (no invoice on file for this
// return). serialNumberId is optional and picked manually — SerialNumber
// has no link back to a sale, so there's no way to auto-resolve which unit
// a given customer bought (see RmaRequest's own comment).
@Entity
@Table(name = "rma_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RmaLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rma_id", nullable = false)
    private Long rmaId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "invoice_line_id")
    private Long invoiceLineId;

    @Column(name = "serial_number_id")
    private Long serialNumberId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "reason_note", columnDefinition = "text")
    private String reasonNote;
}
