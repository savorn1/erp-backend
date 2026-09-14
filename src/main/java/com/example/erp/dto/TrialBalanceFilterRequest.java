package com.example.erp.dto;

import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class TrialBalanceFilterRequest {

    private Long companyId;
    // Defaults to today when omitted. Includes every POSTED journal entry
    // dated on or before this date.
    private LocalDate asOfDate;
}
