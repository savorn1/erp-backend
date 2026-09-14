package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxRateResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private String code;
    private String name;
    private String type;
    private BigDecimal ratePercent;
    private boolean active;
}
