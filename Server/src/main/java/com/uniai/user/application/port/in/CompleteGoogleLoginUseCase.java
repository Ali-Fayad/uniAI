package com.uniai.user.application.port.in;

public interface CompleteGoogleLoginUseCase {
    String completeGoogleLogin(String code, String redirectUri);
}
