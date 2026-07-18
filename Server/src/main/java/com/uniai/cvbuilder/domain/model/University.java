package com.uniai.cvbuilder.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Institution entity used by CV education references. Physical locations live in campus.
 */
@Entity
@Table(name = "universities")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_ar")
    private String nameAr;

    private String acronym;

    @Transient
    private BigDecimal latitude;

    @Transient
    private BigDecimal longitude;

    /** Deprecated response compatibility fields; campus data is served by the catalog API. */
    @Transient
    private String campusName;

    @Transient
    private String campusType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
