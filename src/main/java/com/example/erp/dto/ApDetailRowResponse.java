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
public class ApDetailRowResponse {

    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private long daysOverdue;
    private Long supplierId;
    private String supplierName;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal lineTotal;
    // This invoice's outstanding balance (not this line's own — payments/
    // credits aren't allocated line-by-line) so every line of a
    // partially-paid invoice reads the same remaining amount.
    private BigDecimal invoiceOutstanding;
}
