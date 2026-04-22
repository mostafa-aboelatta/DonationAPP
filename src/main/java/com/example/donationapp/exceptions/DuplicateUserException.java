package com.example.donationapp.exceptions;

public class DuplicateUserException extends Exception
{
    
    public DuplicateUserException(String message) {
        super(message);
    }
    
    public DuplicateUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
