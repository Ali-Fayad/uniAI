package com.uniai.user.domain.model;

import com.uniai.user.domain.valueobject.VerificationCodeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_code")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationCodeType type;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean used;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public void saveCode(Long userId, String code, VerificationCodeType type) {
        this.userId = userId;
        this.code = code;
        this.type = type;
        this.expiresAt = LocalDateTime.now().plusMinutes(15);
        this.used = false;
    }
}
