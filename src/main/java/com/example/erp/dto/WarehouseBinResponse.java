package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseBinResponse {

    private Long id;
    private Long zoneId;
    private String zoneName;
    private Long warehouseId;
    private String warehouseName;
    private String name;
    private boolean active;
}
