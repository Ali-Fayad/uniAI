package com.uniai.user.application.dto.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestPasswordCommand {
    private String email;
    private String verificationCode;
    private String newPassword;
}
