package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Long customerId;
    private String customerName;
    private Long warehouseId;
    private String warehouseName;
    private String soNumber;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private String status;
    private String notes;
    private String createdBy;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private List<SalesOrderLineResponse> lines;
}
