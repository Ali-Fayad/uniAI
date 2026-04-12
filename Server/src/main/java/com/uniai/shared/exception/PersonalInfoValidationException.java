package com.uniai.shared.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class PersonalInfoValidationException extends RuntimeException {

    private final List<String> missingFields;

    public PersonalInfoValidationException(List<String> missingFields) {
        super("Phone, address, bio, and at least one skill are required");
        this.missingFields = missingFields;
    }
}
