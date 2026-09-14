package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class DashboardTrendFilterRequest {

    private Long companyId;
    // Number of trailing calendar months to return, oldest first, ending
    // with the current month. Defaults to 6 when omitted.
    private Integer months;
}
