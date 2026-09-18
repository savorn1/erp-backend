package com.example.erp.repository;

import com.example.erp.entity.CommissionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface CommissionEntryRepository extends JpaRepository<CommissionEntry, Long>, JpaSpecificationExecutor<CommissionEntry> {

    List<CommissionEntry> findBySalesRepUserIdAndPaidOutFalseAndEarnedDateLessThanEqual(Long salesRepUserId, LocalDate throughDate);
}
