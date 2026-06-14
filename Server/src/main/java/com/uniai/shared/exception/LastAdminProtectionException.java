package com.uniai.shared.exception;

public class LastAdminProtectionException extends RuntimeException {
    public LastAdminProtectionException() {
        super("The last admin user cannot be deleted");
    }
}
