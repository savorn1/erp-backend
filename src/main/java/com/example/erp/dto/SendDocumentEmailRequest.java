package com.example.erp.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

// Every field optional — an empty request means "send to whichever email is
// already on file for this document's customer/supplier, with the default
// subject/body." Shared by Invoice/Quotation/PurchaseOrder's /email endpoints.
@Data
public class SendDocumentEmailRequest {

    @Email
    private String to;

    private String subject;

    private String message;
}
