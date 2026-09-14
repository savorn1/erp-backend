package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse {

    private Long id;
    private Long companyId;
    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private Long binId;
    private String binName;
    private String type;
    private BigDecimal quantityDelta;
    private String referenceType;
    private Long referenceId;
    private String createdBy;
    private LocalDateTime createdAt;
}
