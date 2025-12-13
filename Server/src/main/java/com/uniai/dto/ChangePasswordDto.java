package com.uniai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDto {
    private String email;
    private String verificationCode;
    private String newPassword;
}
