package com.example.erp.service;

import java.time.LocalDate;

// Whether a given posting date falls inside a CLOSED AccountingPeriod for a
// company. A date outside every defined period (e.g. no fiscal year has been
// set up yet, or it falls before/after all of them) is never locked — period
// locking is opt-in, not a requirement to post at all.
public interface PeriodLockService {

    boolean isLocked(Long companyId, LocalDate date);
}
