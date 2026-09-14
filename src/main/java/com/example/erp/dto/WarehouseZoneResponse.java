package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseZoneResponse {

    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private String name;
    private String description;
    private boolean active;
    private long binCount;
}
