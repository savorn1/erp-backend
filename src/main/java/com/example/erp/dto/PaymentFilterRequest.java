package com.example.erp.dto;

import com.example.erp.entity.PaymentMethod;
import com.example.erp.entity.PaymentType;
import lombok.Data;
import org.springdoc.core.annotations.ParameterObject;

@Data
@ParameterObject
public class PaymentFilterRequest {

    private String paymentNumber;
    private Long companyId;
    private Long customerId;
    private PaymentMethod method;
    private PaymentType type;

    private String sortBy = "id";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;
}
