package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// A file attached to some other record — Invoice, PurchaseOrder, SalesOrder,
// Customer, or Supplier (see AttachmentServiceImpl's owner-type allow-list).
// One polymorphic table rather than five near-identical ones: ownerType +
// ownerId together locate the owning record, the same way RolePermission's
// own comment justifies a plain String module column over an enum. The
// actual file bytes already live in S3 by the time this row is created —
// FileStorageService.upload() (via POST /api/files/upload) runs first on
// the frontend, and this just records the resulting key/url against an
// owner. Deleting this row also deletes the S3 object (see
// AttachmentServiceImpl.delete).
@Entity
@Table(name = "attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_type", nullable = false)
    private String ownerType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    // S3 key — needed to delete the object later.
    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String url;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type")
    private String contentType;

    @Column(nullable = false)
    private long size;

    @Column(name = "uploaded_by")
    private String uploadedBy;

    @CreationTimestamp
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}
