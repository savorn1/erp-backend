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
public class PosSessionResponse {

    private Long id;
    private Long registerId;
    private String registerName;
    private Long companyId;
    private String openedBy;
    private LocalDateTime openedAt;
    private BigDecimal openingFloat;
    private String status;
    private String closedBy;
    private LocalDateTime closedAt;
    private BigDecimal countedCash;
    private BigDecimal expectedCash;
    private BigDecimal cashVariance;
    // Running total for an OPEN session, so the checkout UI can show
    // "expected so far" before closing.
    private BigDecimal salesTotalSoFar;
}
