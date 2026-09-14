package com.example.erp.dto;

import com.example.erp.entity.ProductStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class ProductFilterRequest {

    private String name;
    private String sku;
    private Long companyId;
    private Long categoryId;
    private Long brandId;
    private Long typeId;
    private Long supplierId;
    private ProductStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
