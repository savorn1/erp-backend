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

@Entity
@Table(name = "product_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which kind of thing this type represents. A closed enum, not free text, so
    // the rest of the system can reason about it; the name beside it stays
    // user-editable for display. At most one type per code (see the service).
    //
    // Nullable at the column level even though the API requires it: a NOT NULL
    // column can't add itself to a table that already has rows under
    // ddl-auto=update. Types created before this read back with a null code
    // until they're edited.
    @Enumerated(EnumType.STRING)
    private ProductTypeCode code;

    @Column(nullable = false)
    private String name;

    @Builder.Default
    private boolean active = true;
}
