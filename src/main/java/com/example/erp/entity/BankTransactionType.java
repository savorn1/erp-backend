package com.example.erp.entity;

public enum BankTransactionType {
    DEPOSIT,
    WITHDRAWAL,
    // A transfer between two of this company's bank accounts is two linked
    // rows, one per side — see BankAccountServiceImpl.transfer.
    TRANSFER_IN,
    TRANSFER_OUT
}
