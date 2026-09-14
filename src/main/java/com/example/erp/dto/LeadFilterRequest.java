package com.example.erp.dto;

import com.example.erp.entity.LeadSource;
import com.example.erp.entity.LeadStatus;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class LeadFilterRequest {

    // Matches against contactName or organizationName.
    private String search;
    private Long companyId;
    private LeadStatus status;
    private LeadSource source;
    private Long assignedToUserId;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
