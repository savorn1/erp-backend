package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferLineResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantityRequested;
    private Long unitOfMeasureId;
    private String unitOfMeasureAbbreviation;
    private Long sourceBinId;
    private String sourceBinName;
    private Long destinationBinId;
    private String destinationBinName;
    private Long batchId;
    private String batchNumber;
    private BigDecimal quantityShipped;
    private BigDecimal quantityReceived;
    private List<String> serialNumbers;
}
