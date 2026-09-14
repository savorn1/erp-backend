package com.example.erp.dto;

import com.example.erp.entity.OpportunityStage;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class OpportunityFilterRequest {

    private String search;
    private Long companyId;
    private OpportunityStage stage;
    private Long leadId;
    private Long customerId;
    private Long assignedToUserId;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
