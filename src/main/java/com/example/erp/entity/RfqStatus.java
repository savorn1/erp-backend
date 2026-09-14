package com.example.erp.entity;

public enum RfqStatus {
    DRAFT,
    // Sent to the invited suppliers — see RfqServiceImpl.sendRfq. Supplier
    // quotations are only recorded once in this state.
    SENT,
    // A supplier has been selected and a purchase order drafted from their
    // quoted prices — see RfqServiceImpl.selectSupplier.
    CLOSED,
    CANCELLED
}
