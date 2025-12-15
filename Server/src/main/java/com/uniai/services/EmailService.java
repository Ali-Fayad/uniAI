package com.uniai.services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.uniai.builder.EmailBuilder;
import com.uniai.domain.VerificationCodeType;
import com.uniai.exception.InvalidVerificationCodeException;
import com.uniai.model.User;
import com.uniai.model.VerifyCode;
import com.uniai.repository.UserRepository;
import com.uniai.repository.VerifyCodeRepository;
import com.uniai.security.email.EmailProperties;
import com.uniai.security.email.EmailUtil;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;
	private final VerifyCodeRepository verifyCodeRepository;
	private final UserRepository userRepository;
	private final EmailProperties emailProperties;

	public String sendVerificationCode(String userEmail, VerificationCodeType type) {

		String code = EmailUtil.generateVerificationCode(
				emailProperties.getCodeLength());

		EmailProperties.EmailMessage message = getEmailMessage(type);
		Context context = createEmailContext(code, message);

		saveVerificationCode(userEmail, code, type);
		sendEmail(userEmail, message.getSubject(), context);

		return code;
	}

	public User verifyCode(String email, String code, VerificationCodeType type) {

		VerifyCode stored = verifyCodeRepository
				.findTopByEmailAndTypeOrderByExpirationTimeDesc(email, type);

		if (stored == null ||
				EmailUtil.isExpired(stored.getExpirationTime()) ||
				!stored.getCode().equals(code)) {
			throw new InvalidVerificationCodeException();
		}

		User user = userRepository.findByEmail(email.toLowerCase());
		if (user == null)
			throw new InvalidVerificationCodeException();

		if (type == VerificationCodeType.VERIFY) {
			user.setVerified(true);
			userRepository.save(user);
		}

		verifyCodeRepository.delete(stored);
		return user;
	}

	private EmailProperties.EmailMessage getEmailMessage(VerificationCodeType type) {
		String key = getTypeKey(type);
		EmailProperties.EmailMessage message = emailProperties.getMessages().get(key);

		if (message == null)
			throw new IllegalStateException("Missing email config for type: " + key);

		return message;
	}

	private Context createEmailContext(String code, EmailProperties.EmailMessage message) {
		return EmailBuilder.buildEmailContext(
				message,
				code,
				emailProperties);
	}

	private void saveVerificationCode(String email, String code, VerificationCodeType type) {
		verifyCodeRepository.deleteByEmailAndType(email, type);

		VerifyCode newCode = EmailBuilder.buildVerifyCode(
				email,
				code,
				type,
				emailProperties.getCodeExpiryMinutes());

		verifyCodeRepository.save(newCode);
	}

	private void sendEmail(String to, String subject, Context context) {
		try {
			String html = templateEngine.process("verification_email", context);

			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

			helper.setTo(to);
			helper.setFrom(emailProperties.getFrom());
			helper.setSubject(subject);
			helper.setText(html, true);

			mailSender.send(mimeMessage);

		} catch (MessagingException e) {
			log.error("Failed to send email to {}", to, e);
			throw new IllegalStateException("Email sending failed");
		}
	}

	private String getTypeKey(VerificationCodeType type) {
		return switch (type) {
			case VERIFY -> "verify";
			case TWO_FACT_AUTH -> "two-factor";
			case CHANGE_PASSWORD -> "change-password";
		};
	}
}
