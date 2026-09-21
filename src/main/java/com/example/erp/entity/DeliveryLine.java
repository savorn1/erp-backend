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

@Entity
@Table(name = "delivery_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_id", nullable = false)
    private Long deliveryId;

    @Column(name = "sales_order_line_id", nullable = false)
    private Long salesOrderLineId;

    // Denormalized from the SO line at posting time.
    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Expressed in the sales order line's unit, not necessarily the product's
    // inventory unit — same convention as GoodsReceiptLine.quantityReceived.
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityDelivered;

    // Copied from the SO line at posting time so stock maths stays reproducible
    // even if the SO line or its ProductUom is touched later. Multiply
    // quantityDelivered by this to get the quantity in the product's inventory
    // unit — that, not quantityDelivered, is what moves stock. Nullable: rows
    // written before UoM support read back as factor 1.
    @Column(name = "conversion_factor", precision = 19, scale = 6)
    private BigDecimal conversionFactor;

    // Bin picked from — optional.
    @Column(name = "bin_id")
    private Long binId;

    // Requester's raw, unresolved input — validated and resolved to batchId
    // only at ship time (see DeliveryServiceImpl.shipDelivery), same
    // "nothing happens until the stock-moving action" reasoning as
    // StockAdjustmentLine.
    private String batchNumber;

    @Column(columnDefinition = "text")
    private String serialNumbersRaw;

    // Resolved at ship time.
    @Column(name = "batch_id")
    private Long batchId;
}
