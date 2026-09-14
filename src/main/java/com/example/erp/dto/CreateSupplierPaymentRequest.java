package com.example.erp.dto;

import com.example.erp.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateSupplierPaymentRequest {

    @NotNull
    private Long companyId;

    @NotNull
    private Long supplierId;

    @NotNull
    private LocalDate paymentDate;

    @NotNull
    private PaymentMethod method;

    private String reference;

    private String notes;

    // The payment's total amount is derived as the sum of these — see
    // SupplierPaymentServiceImpl.recordPayment.
    @NotEmpty
    @Valid
    private List<SupplierPaymentAllocationRequest> allocations;
}
