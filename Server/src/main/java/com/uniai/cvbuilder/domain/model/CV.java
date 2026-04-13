package com.uniai.cvbuilder.domain.model;

import java.time.LocalDateTime;
import java.util.List;

import com.uniai.cvbuilder.infrastructure.persistence.converter.StringListJsonConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JPA entity representing the CV root aggregate that ties user ownership to all section data.
 */
@Entity
@Table(name = "cvs")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CV {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "cv_name", nullable = false)
    private String cvName;

    private String template;

    @Column(name = "template_id")
    private Long templateId;

    @Convert(converter = StringListJsonConverter.class)
    @Column(name = "sections_order", columnDefinition = "jsonb", nullable = false)
    private List<String> sectionsOrder;

    @Convert(converter = com.uniai.cvbuilder.infrastructure.persistence.converter.SelectedItemsJsonConverter.class)
    @Column(name = "selected_items", columnDefinition = "jsonb")
    private SelectedItems selectedItems;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

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
