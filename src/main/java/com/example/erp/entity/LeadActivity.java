package com.example.erp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

// An append-only activity/audit entry for a Lead ("Lead follow-up" and
// history) — written automatically on creation, status changes, assignment,
// and conversion, or manually as a free-form follow-up note.
@Entity
@Table(name = "lead_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeadActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeadActivityType type;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
