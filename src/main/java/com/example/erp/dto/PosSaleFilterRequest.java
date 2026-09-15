package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class PosSaleFilterRequest {

    private String saleNumber;
    private Long companyId;
    private Long registerId;
    private Long posSessionId;
    private String status;
    // Inclusive range over saleDate's date component — e.g. the POS
    // dashboard's "today" view passes dateFrom == dateTo == today.
    private LocalDate dateFrom;
    private LocalDate dateTo;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
