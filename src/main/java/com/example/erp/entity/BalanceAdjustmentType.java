package com.example.erp.entity;

public enum BalanceAdjustmentType {
    // Increases what the customer owes (e.g. a new invoice).
    CHARGE,
    // Decreases what the customer owes (e.g. a payment received).
    PAYMENT
}
