package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class ProductPurchasePriceHistoryFilterRequest {

    private Long companyId;
    private Long productId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}
