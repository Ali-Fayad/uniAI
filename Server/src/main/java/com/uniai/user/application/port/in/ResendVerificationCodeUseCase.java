package com.uniai.user.application.port.in;

import com.uniai.user.application.dto.command.EmailRequestCommand;

public interface ResendVerificationCodeUseCase {
    String resendVerificationCode(EmailRequestCommand command);
}
