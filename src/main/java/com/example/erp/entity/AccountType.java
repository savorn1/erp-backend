package com.example.erp.entity;

// The five fundamental accounting classifications — every Account carries
// one, and it must match its parent's (see AccountServiceImpl) so a group
// like "1000 Assets" never ends up with a Liability account nested under it.
public enum AccountType {
    ASSET,
    LIABILITY,
    EQUITY,
    REVENUE,
    EXPENSE
}
