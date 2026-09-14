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
public class SalesByCustomerGroupRowResponse {

    // Null when the customer has no group assigned.
    private Long customerGroupId;
    private String customerGroupName;
    private long orderCount;
    private BigDecimal revenue;
}
