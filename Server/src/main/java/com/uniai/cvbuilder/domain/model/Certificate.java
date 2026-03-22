package com.uniai.cvbuilder.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * JPA entity capturing certificate achievements linked to a CV.
 */
@Entity
@Table(name = "certificates")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cv_id", nullable = false)
    private Long cvId;

    @Column(nullable = false)
    private String name;

    private String issuer;

    @Column(name = "issued_date")
    private LocalDate date;

    @Column(name = "credential_url")
    private String credentialUrl;
}
