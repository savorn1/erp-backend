package com.example.erp.entity;

public enum PaymentType {
    PAYMENT,
    // Reverses part or all of an earlier PAYMENT — see
    // PaymentServiceImpl.refundPayment. relatedPaymentId points back to it.
    REFUND
}
