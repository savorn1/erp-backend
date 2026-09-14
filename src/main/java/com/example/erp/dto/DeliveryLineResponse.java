package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryLineResponse {

    private Long id;
    private Long salesOrderLineId;
    private Long productId;
    private String productName;
    private String productSku;
    private BigDecimal quantityDelivered;
    private Long binId;
    private String binName;
    private Long batchId;
    private String batchNumber;
    private List<String> serialNumbers;
}
