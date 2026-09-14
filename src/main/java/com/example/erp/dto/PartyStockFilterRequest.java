package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

// Shared by Stock by Customer (partyId = a Customer id) and Stock by
// Supplier (partyId = a Supplier id).
@Data
@ParameterObject
public class PartyStockFilterRequest {

    private Long companyId;
    private Long warehouseId;
    private Long partyId;
}
