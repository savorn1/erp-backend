package com.example.erp.entity;

public enum FiscalYearStatus {
    OPEN,
    // Closing a year closes every period in it — nothing can be posted into
    // it until it's reopened. See FiscalYearServiceImpl.
    CLOSED
}
