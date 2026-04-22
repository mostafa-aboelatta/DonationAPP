package com.example.donationapp.exceptions;

public class CaseNotFoundException extends Exception
{
    
    public CaseNotFoundException(String message) {
        super(message);
    }
    
    public CaseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
