package com.uniai.cvbuilder.domain.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity holding user-level personal details reused across CVs.
 */
@Entity
@Table(name = "personal_info")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalInfo {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    private String phone;
    private String address;
    @Column(name = "linkedin_url")
    private String linkedin;
    @Column(name = "github_url")
    private String github;
    @Column(name = "portfolio_url")
    private String portfolio;

    @Column(length = 2000)
    private String summary;

    @Column(name = "job_title")
    private String jobTitle;

    private String company;

    @Column(name = "education_json", columnDefinition = "TEXT")
    private String educationJson;

    @Column(name = "skills_json", columnDefinition = "TEXT")
    private String skillsJson;

    @Column(name = "languages_json", columnDefinition = "TEXT")
    private String languagesJson;

    @Column(name = "experience_json", columnDefinition = "TEXT")
    private String experienceJson;

    @Column(name = "projects_json", columnDefinition = "TEXT")
    private String projectsJson;

    @Column(name = "certificates_json", columnDefinition = "TEXT")
    private String certificatesJson;

    @Column(name = "is_filled")
    private Boolean isFilled;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
