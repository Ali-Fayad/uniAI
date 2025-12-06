package com.uniai.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Entity
@Table (name = "users")
@Data
@Builder
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
