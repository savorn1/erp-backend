package com.example.erp.repository;

import com.example.erp.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

    List<JournalEntryLine> findByJournalEntryId(Long journalEntryId);

    List<JournalEntryLine> findByJournalEntryIdIn(List<Long> journalEntryIds);

    List<JournalEntryLine> findByAccountId(Long accountId);

    boolean existsByCostCenterId(Long costCenterId);

    void deleteByJournalEntryId(Long journalEntryId);
}
