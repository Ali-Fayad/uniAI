package com.uniai.catalog.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "campus")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "university_id", nullable = false)
    private Long universityId;

    @Column(nullable = false)
    private String name;

    @Column(name = "campus_type")
    private String campusType;

    @Column(nullable = false)
    private String city;

    private String locality;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
