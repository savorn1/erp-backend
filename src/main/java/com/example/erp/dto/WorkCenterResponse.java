package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkCenterResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String name;
    private String code;
    private Long warehouseId;
    private String warehouseName;
    private String description;
    private BigDecimal capacityPerHour;
    private boolean active;
}
