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

import java.time.LocalDateTime;

// A cashier-suspended cart — no stock or accounting effect (unlike a
// PosSale, nothing has been decided yet: not the tenders, sometimes not even
// the final quantities). Resuming reloads it into the checkout cart and
// deletes this row and its lines; there's no other lifecycle state.
@Entity
@Table(name = "pos_held_sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosHeldSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "warehouse_id", nullable = false)
    private Long warehouseId;

    @Column(name = "register_id", nullable = false)
    private Long registerId;

    @Column(name = "pos_session_id", nullable = false)
    private Long posSessionId;

    // Null = the cart hadn't picked a customer (walk-in) yet when held.
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "held_number", unique = true)
    private String heldNumber;

    @Column(name = "held_at", nullable = false)
    private LocalDateTime heldAt;

    @Column(name = "held_by")
    private String heldBy;

    // Cashier's own label to tell tickets apart at a glance, e.g. "Window
    // customer" or "Table 4" — purely descriptive, never parsed.
    private String note;
}
