package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOutstandingRowResponse {

    private Long orderId;
    private String soNumber;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private String status;
    private Long customerId;
    private String customerName;
    // Sum across lines of (quantityOrdered - quantityDelivered).
    private BigDecimal outstandingQuantity;
    // Value of the undelivered quantity, at each line's price/discount/tax.
    private BigDecimal outstandingValue;
}
