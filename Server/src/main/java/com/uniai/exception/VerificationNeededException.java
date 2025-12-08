package com.uniai.exception;

public class VerificationNeededException extends RuntimeException{
    public VerificationNeededException(String msg)
    {
        super(msg);
    }
}
