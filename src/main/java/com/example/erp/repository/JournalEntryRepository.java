package com.example.erp.repository;

import com.example.erp.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long>, JpaSpecificationExecutor<JournalEntry> {
    Optional<JournalEntry> findBySourceTypeAndSourceId(String sourceType, Long sourceId);
}
