package com.example.erp.service.impl;

import com.example.erp.dto.AttachmentResponse;
import com.example.erp.dto.CreateAttachmentRequest;
import com.example.erp.entity.Attachment;
import com.example.erp.exception.AppException;
import com.example.erp.repository.AttachmentRepository;
import com.example.erp.service.AttachmentService;
import com.example.erp.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    private static final Set<String> VALID_OWNER_TYPES = Set.of(
            "INVOICE", "PURCHASE_ORDER", "SALES_ORDER", "CUSTOMER", "SUPPLIER"
    );

    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> list(String ownerType, Long ownerId) {
        requireValidOwnerType(ownerType);
        return attachmentRepository.findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(ownerType, ownerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AttachmentResponse create(CreateAttachmentRequest request, String actingUsername) {
        requireValidOwnerType(request.getOwnerType());

        Attachment attachment = Attachment.builder()
                .ownerType(request.getOwnerType())
                .ownerId(request.getOwnerId())
                .key(request.getKey())
                .url(request.getUrl())
                .fileName(request.getFileName())
                .contentType(request.getContentType())
                .size(request.getSize())
                .uploadedBy(actingUsername)
                .build();
        attachmentRepository.save(attachment);
        return toResponse(attachment);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Attachment attachment = attachmentRepository.findById(id)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "Attachment not found with id: " + id));
        try {
            fileStorageService.delete(attachment.getKey());
        } catch (Exception e) {
            log.warn("Failed to delete S3 object for attachment {} (key {}): {}", id, attachment.getKey(), e.getMessage());
        }
        attachmentRepository.delete(attachment);
    }

    private void requireValidOwnerType(String ownerType) {
        if (!VALID_OWNER_TYPES.contains(ownerType)) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Unsupported owner type: " + ownerType);
        }
    }

    private AttachmentResponse toResponse(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .ownerType(attachment.getOwnerType())
                .ownerId(attachment.getOwnerId())
                .key(attachment.getKey())
                .url(attachment.getUrl())
                .fileName(attachment.getFileName())
                .contentType(attachment.getContentType())
                .size(attachment.getSize())
                .uploadedBy(attachment.getUploadedBy())
                .uploadedAt(attachment.getUploadedAt())
                .build();
    }
}
