package com.uniai.shared.exception;

public class PersonalInfoGoneException extends RuntimeException {
    public PersonalInfoGoneException() {
        super("Personal information incomplete. Please complete your profile at /personal-info");
    }
}
