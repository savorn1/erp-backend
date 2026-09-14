package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class BalanceSheetFilterRequest {

    private Long companyId;
    // Defaults to today when omitted.
    private LocalDate asOfDate;
}
