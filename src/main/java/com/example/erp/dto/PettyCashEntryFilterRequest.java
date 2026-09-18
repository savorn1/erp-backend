package com.example.erp.dto;

import com.example.erp.entity.PettyCashEntryType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

import java.time.LocalDate;

@Data
@ParameterObject
public class PettyCashEntryFilterRequest {

    private Long companyId;
    private PettyCashEntryType type;
    private LocalDate entryDateFrom;
    private LocalDate entryDateTo;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
