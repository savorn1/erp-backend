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
public class PosExchangeResponse {

    private Long id;
    private Long companyId;
    private Long warehouseId;
    private Long registerId;
    private String registerName;
    private Long posSessionId;
    private Long originalPosSaleId;
    private String originalSaleNumber;
    private String exchangeNumber;
    private LocalDateTime exchangeDate;
    private BigDecimal returnValue;
    private BigDecimal returnTaxValue;
    private BigDecimal newValue;
    private BigDecimal newTaxValue;
    private BigDecimal netAmount;
    private String settlementMethod;
    private BigDecimal settlementAmount;
    private String settlementReference;
    private String createdBy;
    private List<PosExchangeReturnLineResponse> returnLines;
    private List<PosExchangeNewLineResponse> newLines;
    // Only meaningful on the create response itself (cash tendered minus the
    // amount actually applied, CASH + customer-owes case only).
    private BigDecimal changeDue;
}
