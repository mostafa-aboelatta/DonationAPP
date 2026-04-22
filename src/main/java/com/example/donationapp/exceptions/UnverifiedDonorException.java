package com.example.donationapp.exceptions;

public class UnverifiedDonorException extends Exception
{
    
    public UnverifiedDonorException(String message) {
        super(message);
    }
    
    public UnverifiedDonorException(String message, Throwable cause) {
        super(message, cause);
    }
}
