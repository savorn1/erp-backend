package com.example.erp.dto;

import com.example.erp.entity.RoutingStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class RoutingFilterRequest {

    private String routingNumber;
    private Long companyId;
    private Long bomId;
    private RoutingStatus status;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
