package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class CommissionEntryFilterRequest {

    private Long companyId;
    private Long salesRepUserId;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Boolean paidOut;

    private String sortBy = "earnedDate";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
