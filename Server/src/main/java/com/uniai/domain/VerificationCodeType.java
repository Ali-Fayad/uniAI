package com.uniai.domain;

public enum VerificationCodeType {
	//TODO: Move these as per request
	VERIFY(
			"uniAI — Email Verification",
			"Verify Your Email Address",
			"Thanks for signing up for uniAI! To complete your registration, please use the verification code below."),
	TWO_FACT_AUTH(
			"uniAI — Two-Factor Authentication Code",
			"Two-factor Authentication Code",
			"Use the following code to complete your sign-in."),
	CHANGE_PASSWORD(
			"uniAI — Password Reset",
			"Reset Your Password",
			"We received a request to reset your password. Use the verification code below to set a new password.");

	private final String subject;
	private final String title;
	private final String paragraph;

	VerificationCodeType(String subject, String title, String paragraph) {
		this.subject = subject;
		this.title = title;
		this.paragraph = paragraph;
	}

	public String getSubject() {
		return subject;
	}

	public String getTitle() {
		return title;
	}

	public String getParagraph() {
		return paragraph;
	}
}
