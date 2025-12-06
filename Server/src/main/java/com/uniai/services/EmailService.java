// package com.uniai.services;

// import java.security.SecureRandom;

// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.stereotype.Service;

// @Service
// public class EmailService {

//     private final JavaMailSender mailSender;
//     private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
//     private static final SecureRandom RANDOM = new SecureRandom();
//     private static final int CODE_LENGTH = 6;

//     public void sendEmail(String to, String subject, String body) {
//         SimpleMailMessage message = new SimpleMailMessage();
//         message.setTo(to);
//         message.setSubject(subject);
//         message.setText(body);
//         mailSender.send(message);
//     }

//     public String generateVerificationCode() {
//         StringBuilder sb = new StringBuilder(CODE_LENGTH);
//         for (int i = 0; i < CODE_LENGTH; i++) {
//             sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
//         }
//         return sb.toString();
//     }
// }
