package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectionRowResponse {

    private String moNumber;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantity;
    private String reason;
    private String rejectedBy;
    private LocalDateTime rejectedAt;
}
