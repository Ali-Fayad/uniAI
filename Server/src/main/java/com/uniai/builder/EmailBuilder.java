package com.uniai.builder;

import java.time.LocalDateTime;

import com.uniai.model.VerifyCode;

public class EmailBuilder {

	public static VerifyCode getVerifyCode(String email, String code, com.uniai.domain.VerificationCodeType type, int EXPIRY_MINUTES) {
		return VerifyCode.builder()
				.email(email)
				.code(code)
				.type(type)
				.expirationTime(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES))
				.build();
	}

}
