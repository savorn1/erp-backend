package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerialNumberResponse {

    private Long id;
    private Long companyId;
    private Long productId;
    private String productName;
    private String productSku;
    private Long warehouseId;
    private String warehouseName;
    private Long binId;
    private String binName;
    private String batchNumber;
    private LocalDate expirationDate;
    private String serialNumber;
    private String status;
    private LocalDateTime createdAt;
}
