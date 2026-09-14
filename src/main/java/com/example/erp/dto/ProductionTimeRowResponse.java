package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionTimeRowResponse {

    private String moNumber;
    private Long productId;
    private String productName;
    private String productSku;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDateTime actualStartDate;
    private LocalDateTime actualEndDate;
    // actualEndDate - actualStartDate, in hours.
    private BigDecimal actualDurationHours;
}
