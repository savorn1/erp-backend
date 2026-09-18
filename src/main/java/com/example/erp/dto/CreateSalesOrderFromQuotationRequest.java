package com.example.erp.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateSalesOrderFromQuotationRequest {

    // Quotation has no warehouse of its own — the destination sales order
    // needs one to know where deliveries will ship from.
    @NotNull
    private Long warehouseId;

    @NotNull
    private LocalDate orderDate;

    private LocalDate expectedDate;

    // Optional override — see SalesOrder.salesRepUserId. When omitted,
    // SalesOrderServiceImpl defaults it to the source Quotation's linked
    // Opportunity's assignedToUserId, if any.
    private Long salesRepUserId;
}
