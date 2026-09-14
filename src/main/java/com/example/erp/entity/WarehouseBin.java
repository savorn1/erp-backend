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

// A shelf/bin — the smallest storage location, nested under a WarehouseZone.
@Entity
@Table(name = "warehouse_bins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseBin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "zone_id", nullable = false)
    private Long zoneId;

    // e.g. "A-01-03" (aisle-shelf-bin) or a plain label like "Bin 12".
    @Column(nullable = false)
    private String name;

    @Builder.Default
    private boolean active = true;
}
