package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesPendingDeliveryRowResponse {

    private Long deliveryId;
    private String deliveryNumber;
    private LocalDate deliveryDate;
    private String status;
    private Long salesOrderId;
    private String soNumber;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private long lineCount;
}
