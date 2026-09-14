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
public class CollectionByCustomerRowResponse {

    private Long customerId;
    private String customerName;
    private long paymentCount;
    private BigDecimal received;
    private BigDecimal refunded;
    private BigDecimal net;
}
