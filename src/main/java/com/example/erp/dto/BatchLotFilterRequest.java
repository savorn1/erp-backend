package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

// No warehouseId filter — ProductBatch itself isn't warehouse-scoped (a
// batch can be split across warehouses via transfers), so quantities here
// are aggregated across every warehouse, matching that model.
@Data
@ParameterObject
public class BatchLotFilterRequest {

    private Long companyId;
    private Long productId;

    // When false (default), batches whose computed current quantity is <= 0
    // are left out — see InventoryReportServiceImpl.batchLotStock.
    private boolean includeDepleted = false;
}
