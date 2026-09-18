package com.example.erp.repository;

import com.example.erp.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(String ownerType, Long ownerId);
}
