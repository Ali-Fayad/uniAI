package com.uniai.catalog.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "university")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_ar")
    private String nameAr;

    private String acronym;

    @Column(nullable = false)
    private String country;

    /** Deprecated in-memory compatibility fields for older callers; not database columns. */
    @jakarta.persistence.Transient
    private String city;
    @jakarta.persistence.Transient
    private String campusName;
    @jakarta.persistence.Transient
    private String campusType;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", referencedColumnName = "id", insertable = false, updatable = false)
    @OrderBy("name ASC")
    private List<CampusCatalog> campuses;
}
