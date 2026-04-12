package com.uniai.shared.exception;

import java.util.List;

import lombok.Getter;

@Getter
public class PersonalInfoValidationException extends RuntimeException {

    private final List<String> missingFields;

    public PersonalInfoValidationException(List<String> missingFields) {
        super("Phone, address, bio, and at least one skill are required");
        this.missingFields = missingFields;
    }
}
