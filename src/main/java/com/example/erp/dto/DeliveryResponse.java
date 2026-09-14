package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {

    private Long id;
    private Long companyId;
    private Long salesOrderId;
    private String soNumber;
    private Long warehouseId;
    private String warehouseName;
    private String deliveryNumber;
    private LocalDate deliveryDate;
    private String status;
    private String notes;
    private String createdBy;
    private String pickedBy;
    private LocalDateTime pickedAt;
    private String packedBy;
    private LocalDateTime packedAt;
    private String shippedBy;
    private LocalDateTime shippedAt;
    private String deliveredBy;
    private LocalDateTime deliveredAt;
    private List<DeliveryLineResponse> lines;
}
