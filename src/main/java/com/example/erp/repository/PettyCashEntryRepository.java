package com.example.erp.repository;

import com.example.erp.entity.PettyCashEntry;
import com.example.erp.entity.PettyCashEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface PettyCashEntryRepository extends JpaRepository<PettyCashEntry, Long>, JpaSpecificationExecutor<PettyCashEntry> {

    @Query("select coalesce(sum(e.amount), 0) from PettyCashEntry e where e.companyId = :companyId and e.type = :type")
    BigDecimal sumAmount(@Param("companyId") Long companyId, @Param("type") PettyCashEntryType type);
}
