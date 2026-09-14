package com.example.erp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesPendingDeliveriesResponse {

    // Deliveries not yet DELIVERED or CANCELLED — sorted by delivery date, ascending.
    private List<SalesPendingDeliveryRowResponse> rows;
    private long deliveryCount;
}
