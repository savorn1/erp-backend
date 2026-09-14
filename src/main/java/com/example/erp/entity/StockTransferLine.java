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
@Table(name = "stock_transfer_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_transfer_id", nullable = false)
    private Long stockTransferId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityRequested;

    @Column(name = "source_bin_id")
    private Long sourceBinId;

    @Column(name = "destination_bin_id")
    private Long destinationBinId;

    // Set when the product is BATCH- or SERIAL-tracked, referencing an
    // existing batch — never created by a transfer, same reasoning as
    // DeliveryLine (batches aren't warehouse-scoped, so the id is stable
    // across the move).
    @Column(name = "batch_id")
    private Long batchId;

    // Set when the transfer is shipped — the quantity actually pulled from
    // the source. Always equal to quantityRequested (no partial shipping).
    @Column(precision = 19, scale = 4)
    private BigDecimal quantityShipped;

    // Set when the transfer is received at the destination.
    @Column(precision = 19, scale = 4)
    private BigDecimal quantityReceived;
}
