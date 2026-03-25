package com.uniai.user.application.port.in;

public interface CheckEmailAvailabilityUseCase {
    boolean isEmailAvailable(String email);
}
