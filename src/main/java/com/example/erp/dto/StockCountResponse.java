package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCountResponse {

    private Long id;
    private Long companyId;
    private Long warehouseId;
    private String warehouseName;
    private String countNumber;
    private LocalDate countDate;
    private String status;
    private String notes;
    private String countedBy;
    private Long adjustmentId;
    private String adjustmentNumber;
    private List<StockCountLineResponse> lines;
}
