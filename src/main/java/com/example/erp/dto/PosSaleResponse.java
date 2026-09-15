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
public class PosSaleResponse {

    private Long id;
    private Long companyId;
    private Long warehouseId;
    private String warehouseName;
    private Long registerId;
    private String registerName;
    private Long posSessionId;
    private Long customerId;
    private String customerName;
    private String saleNumber;
    private LocalDateTime saleDate;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String status;
    private String voidedBy;
    private LocalDateTime voidedAt;
    private String voidReason;
    private String createdBy;
    private List<PosSaleLineResponse> lines;
    private List<PosPaymentLineResponse> payments;
    // Only meaningful on the checkout response itself (cash tendered minus
    // the amount actually applied).
    private BigDecimal changeDue;
}
