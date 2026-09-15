package com.example.erp.entity;

public enum PaymentMethod {
    CASH,
    BANK_TRANSFER,
    PAYMENT_GATEWAY,
    // POS tender only. Buckets into the "bank" side of
    // AutoPostingServiceImpl.cashOrBankAccountId — only CASH is special-cased
    // there, so this falls into that method's existing else-branch untouched.
    CARD
}
