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

// Line items live in the separate StockTransferLine entity/table, looked up
// by stockTransferId — same reasoning as PurchaseOrder/SalesOrder. Header
// carries the four-stage workflow: request -> approve -> ship -> receive.
@Entity
@Table(name = "stock_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "source_warehouse_id", nullable = false)
    private Long sourceWarehouseId;

    @Column(name = "destination_warehouse_id", nullable = false)
    private Long destinationWarehouseId;

    @Column(unique = true)
    private String transferNumber;

    @Column(nullable = false)
    private LocalDate requestDate;

    private LocalDate shipDate;

    private LocalDate receiveDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StockTransferStatus status = StockTransferStatus.REQUESTED;

    @Column(columnDefinition = "text")
    private String notes;

    private String requestedBy;

    private String approvedBy;

    private String shippedBy;

    private String receivedBy;

    // The status this document held when it was cancelled or rejected.
    // Overwriting `status` destroys the only record of how far the workflow
    // actually got, which is what decides whether anything has to be unwound —
    // reserved stock, a posted receipt — so it is captured on the way past.
    //
    // Nullable, and not only for ddl-auto=update: rows cancelled before this
    // existed genuinely have nothing to report, and the UI shows no progress
    // rather than inventing some.
    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_from_status")
    private StockTransferStatus cancelledFromStatus;
}
