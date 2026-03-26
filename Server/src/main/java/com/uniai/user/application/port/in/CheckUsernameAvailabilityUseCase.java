package com.uniai.user.application.port.in;

public interface CheckUsernameAvailabilityUseCase {
    boolean isUsernameAvailable(String username);
}
