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

// The sequence of shop-floor operations used to build one BillOfMaterial's
// finished good. Operation lines live in the separate RoutingOperation
// entity/table, looked up by routingId — no JPA relationship mapping, same
// plain-FK convention as everywhere else. ManufacturingOrderServiceImpl
// picks up a BOM's ACTIVE routing (if any) at order-creation time to
// generate that order's WorkOrders; a BOM with no routing simply produces
// orders with no work orders (this whole layer is additive, not required).
@Entity
@Table(name = "routings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Routing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "bom_id", nullable = false)
    private Long bomId;

    @Column(name = "routing_number", unique = true)
    private String routingNumber;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoutingStatus status = RoutingStatus.ACTIVE;

    @Column(columnDefinition = "text")
    private String notes;

    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
