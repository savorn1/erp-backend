package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockAdjustmentLineResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Long binId;
    private String binName;
    private String reason;
    private BigDecimal quantity;
    private Long batchId;
    private String batchNumber;
    private LocalDate expirationDate;
    private List<String> serialNumbers;
}
