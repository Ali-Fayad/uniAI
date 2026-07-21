package com.uniai.user.application.port.in;

public interface CheckProfileUsernameAvailabilityUseCase {
    boolean isUsernameAvailableForUser(String email, String username);
}
