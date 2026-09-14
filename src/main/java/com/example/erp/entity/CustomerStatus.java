package com.example.erp.entity;

public enum CustomerStatus {
    ACTIVE,
    INACTIVE,
    // Credit hold — can't transact, distinct from a normal INACTIVE (which
    // might just mean "not currently trading with us" rather than "blocked").
    BLOCKED
}
