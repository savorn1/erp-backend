package com.example.erp.dto;

import com.example.erp.entity.LeadSource;
import com.example.erp.entity.LeadStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreateLeadRequest {

    @NotNull
    private Long companyId;

    @NotBlank
    private String contactName;

    private String organizationName;

    @Email
    private String email;

    private String phone;

    @NotNull
    private LeadSource source;

    private Long assignedToUserId;

    @DecimalMin(value = "0", message = "Estimated value cannot be negative")
    private BigDecimal estimatedValue;

    private String notes;

    // ── Deal-specific fields, absorbed from the old Opportunity entity —
    // all optional, typically filled in once the lead progresses past NEW.
    private String dealName;

    @DecimalMin(value = "0", message = "Amount cannot be negative")
    private BigDecimal amount;

    @Min(0)
    @Max(100)
    private Integer probability;

    private LocalDate expectedCloseDate;

    // Set for an upsell-style deal against an existing customer.
    private Long customerId;

    // Which board column the lead starts in. Omitted for the plain "New lead"
    // button, which means NEW; set when adding straight into a column.
    // WON and LOST are refused — those are closed states reached by working a
    // lead, and the entity blocks further edits once in them, so creating one
    // there would produce a lead that can never be touched again.
    private LeadStatus status;
}
