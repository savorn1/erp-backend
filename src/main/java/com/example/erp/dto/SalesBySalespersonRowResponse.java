package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// "Salesperson" here is whoever created the SalesOrder (SalesOrder.createdBy)
// — there's no dedicated sales-rep assignment anywhere in the schema, so this
// is the closest real signal available. See SalesReportServiceImpl.
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesBySalespersonRowResponse {

    private String salesperson;
    private long orderCount;
    private BigDecimal revenue;
}
