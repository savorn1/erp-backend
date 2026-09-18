package com.example.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateSalesOrderRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long customerId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private LocalDate orderDate;

    private LocalDate expectedDate;

    private String notes;

    // Optional — see SalesOrder.salesRepUserId. Null means no commission is
    // tracked for this order.
    private Long salesRepUserId;

    // Optional reference-only foreign currency — see SalesOrder's own
    // comment. Both null or both set, enforced in SalesOrderServiceImpl.
    private String foreignCurrency;
    private BigDecimal exchangeRate;

    @NotEmpty
    @Valid
    private List<SalesOrderLineRequest> lines;
}
