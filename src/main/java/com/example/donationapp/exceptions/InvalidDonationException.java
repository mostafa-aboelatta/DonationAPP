package com.example.donationapp.exceptions;

public class InvalidDonationException extends Exception
{
    
    public InvalidDonationException(String message) {
        super(message);
    }
    
    public InvalidDonationException(String message, Throwable cause) {
        super(message, cause);
    }
}
