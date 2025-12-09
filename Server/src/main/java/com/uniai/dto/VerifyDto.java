package com.uniai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyDto {
	private String email;
	private String verificationCode;

	public VerifyDto(String email, String verificationCode) {
		this.email = email.toLowerCase();
		this.verificationCode = verificationCode;
	}
}
