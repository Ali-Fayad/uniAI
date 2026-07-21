package com.uniai.user.application.service;

import com.uniai.shared.exception.InvalidUsernameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UsernameNormalizerTest {

    @Test
    void trimsAndLowercasesUsername() {
        assertEquals("alice_1", UsernameNormalizer.normalize(" Alice_1 "));
    }

    @Test
    void rejectsInvalidUsername() {
        assertThrows(InvalidUsernameException.class, () -> UsernameNormalizer.normalize("alice user"));
    }
}
