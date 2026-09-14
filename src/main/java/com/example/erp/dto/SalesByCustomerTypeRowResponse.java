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
public class SalesByCustomerTypeRowResponse {

    // Null when the customer has no type assigned.
    private Long customerTypeId;
    private String customerTypeName;
    private long orderCount;
    private BigDecimal revenue;
}
