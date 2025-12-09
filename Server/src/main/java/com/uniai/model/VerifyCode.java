package com.uniai.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "verify_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String code;

    private LocalDateTime expirationTime;

    public void saveCode(String email, String code) {
        this.email = email;
        this.code = code;
        this.expirationTime = LocalDateTime.now().plusMinutes(15);
    }
}
