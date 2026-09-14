package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillOfMaterialResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long productId;
    private String productName;
    private String productSku;
    private String bomNumber;
    private String name;
    private BigDecimal outputQuantity;
    private Long unitOfMeasureId;
    private String unitOfMeasureAbbreviation;
    private String status;
    private Integer version;
    private Long previousVersionId;
    private Long supersededByBomId;
    private String notes;
    private String createdBy;
    private LocalDateTime createdAt;
    private List<BillOfMaterialLineResponse> lines;
}
