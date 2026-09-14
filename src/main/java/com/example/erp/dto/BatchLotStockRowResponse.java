package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLotStockRowResponse {

    private Long batchId;
    private String batchNumber;
    private Long productId;
    private String productName;
    private String productSku;
    private LocalDate expirationDate;
    // received (passed goods receipts + approved STOCK_INCREASE adjustments)
    // minus issued (shipped deliveries) minus approved adjustments that
    // reduce stock — aggregated across every warehouse, since ProductBatch
    // itself isn't warehouse-scoped.
    private BigDecimal currentQuantity;
}
