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

import java.math.BigDecimal;

@Entity
@Table(name = "machines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "work_center_id", nullable = false)
    private Long workCenterId;

    @Column(nullable = false)
    private String name;

    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MachineStatus status = MachineStatus.OPERATIONAL;

    // Optional — feeds ManufacturingReportServiceImpl's Machine Cost report
    // (hours logged on this machine's completed work orders * this rate).
    // Null reads as "no rate set", not zero.
    @Column(name = "cost_per_hour", precision = 19, scale = 4)
    private BigDecimal costPerHour;
}
