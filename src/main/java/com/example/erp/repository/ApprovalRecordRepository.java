package com.example.erp.repository;

import com.example.erp.entity.ApprovalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord, Long> {

    List<ApprovalRecord> findByDocumentTypeAndDocumentIdOrderByLevelAsc(String documentType, Long documentId);

    boolean existsByDocumentTypeAndDocumentIdAndApprovedBy(String documentType, Long documentId, String approvedBy);

    void deleteByDocumentTypeAndDocumentId(String documentType, Long documentId);
}
