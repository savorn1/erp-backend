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
public class SupplierActivityResponse {

    private Long id;
    private Long supplierId;
    private String type;
    private String description;
    private BigDecimal amount;
    private String createdBy;
    private LocalDateTime createdAt;
}
