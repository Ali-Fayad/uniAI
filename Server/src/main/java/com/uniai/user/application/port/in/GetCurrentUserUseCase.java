package com.uniai.user.application.port.in;

import com.uniai.user.application.dto.response.AuthResponseDto;

public interface GetCurrentUserUseCase {
    /** Returns the profile of the authenticated user identified by email. */
    AuthResponseDto getMe(String email);
}
