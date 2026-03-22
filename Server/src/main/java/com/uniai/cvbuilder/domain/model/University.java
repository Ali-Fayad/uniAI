package com.uniai.cvbuilder.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity storing university and campus metadata used for education lookups and seeding.
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

    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(name = "campus_name")
    private String campusName;

    @Column(name = "campus_type")
    private String campusType;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
