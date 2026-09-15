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
public class PosHeldSaleResponse {

    private Long id;
    private Long companyId;
    private Long warehouseId;
    private Long registerId;
    private String registerName;
    private Long posSessionId;
    private Long customerId;
    private String customerName;
    private String heldNumber;
    private LocalDateTime heldAt;
    private String heldBy;
    private String note;
    private int itemCount;
    // Priced against each product's *current* selling price/tax — a
    // best-effort preview only; the authoritative total is recomputed at
    // checkout the same way a freshly-built cart is.
    private BigDecimal estimatedTotal;
    private List<PosHeldSaleLineResponse> lines;
}
