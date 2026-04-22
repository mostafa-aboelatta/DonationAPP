package com.example.donationapp.util;

import com.example.donationapp.models.CharityCase;
import com.example.donationapp.models.User;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Utility class providing static helper methods for the donation system.
 * Contains overloaded methods for various operations.
 */
public class UtilitySupport {
    
    private static final Logger LOGGER = Logger.getLogger(UtilitySupport.class.getName());
    
    // Email configuration - modify these for your SMTP server
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = "your-email@gmail.com"; // Change this
    private static final String EMAIL_PASSWORD = "your-app-password"; // Change this
    private static final boolean EMAIL_ENABLED = false; // Set to true when email is configured
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    // Phone validation pattern
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[(]?[0-9]{1,3}[)]?[-\\s./0-9]{7,14}$"
    );
    
    /**
     * Gets the case ID from a CharityCase object.
     * 
     * @param charityCase The charity case
     * @return The case ID
     */
    public static int getCaseId(CharityCase charityCase) {
        return charityCase.getCaseId();
    }
    
    /**
     * Gets the user ID from a User object.
     * 
     * @param user The user
     * @return The user ID
     */
    public static int getUserId(User user) {
        return user.getId();
    }
    
    /**
     * Gets the type from a CharityCase object.
     * 
     * @param charityCase The charity case
     * @return The case type
     */
    public static String getType(CharityCase charityCase) {
        return charityCase.getType();
    }
    
    /**
     * Gets the status from a CharityCase object.
     * 
     * @param charityCase The charity case
     * @return The case status
     */
    public static String getStatus(CharityCase charityCase) {
        return charityCase.getStatus();
    }
    
    /**
     * Generates a unique verification code.
     * 
     * @return An 8-character uppercase verification code
     */
    public static String generateUniqueVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    /**
     * Sends a donation confirmation email.
     * 
     * @param donorEmail The donor's email address
     * @param donationId The donation ID
     * @param amount The donation amount
     */
    public static void sendDonationConfirmation(String donorEmail, int donationId, double amount) {
        String subject = "Donation Confirmation - #" + donationId;
        String message = String.format(
            "Thank you for your generous donation!\n\n" +
            "Donation ID: %d\n" +
            "Amount: $%.2f\n\n" +
            "Your contribution will help make a difference in someone's life.\n\n" +
            "Best regards,\nDonation Management System",
            donationId, amount
        );
        sendEmail(donorEmail, subject, message);
    }
    
    /**
     * Sends a case approval notification email.
     * 
     * @param receiverEmail The receiver's email address
     * @param caseId The case ID
     * @param priorityLevel The priority level assigned
     */
    public static void sendCaseApprovalNotification(String receiverEmail, int caseId, String priorityLevel) {
        String subject = "Case Approved - #" + caseId;
        String message = String.format(
            "Great news! Your charity case has been approved.\n\n" +
            "Case ID: %d\n" +
            "Priority Level: %s\n\n" +
            "Your case is now visible to donors and you may receive donations soon.\n\n" +
            "Best regards,\nDonation Management System",
            caseId, priorityLevel
        );
        sendEmail(receiverEmail, subject, message);
    }
    
    /**
     * Sends a verification email to a user.
     * 
     * @param userEmail The user's email address
     * @param verificationCode The verification code
     */
    public static void sendVerificationEmail(String userEmail, String verificationCode) {
        String subject = "Verify Your Email - Donation Management System";
        String message = String.format(
            "Welcome to the Donation Management System!\n\n" +
            "Please use the following verification code to verify your email:\n\n" +
            "Verification Code: %s\n\n" +
            "This code will expire in 24 hours.\n\n" +
            "Best regards,\nDonation Management System",
            verificationCode
        );
        sendEmail(userEmail, subject, message);
    }
    
    /**
     * Sends a general email.
     * Overloaded method with basic parameters.
     * 
     * @param email The recipient's email address
     * @param message The message content
     */
    public static void sendEmail(String email, String message) {
        sendEmail(email, "Notification - Donation Management System", message);
    }
    
    /**
     * Sends a general email with subject.
     * Overloaded method with subject parameter.
     * 
     * @param email The recipient's email address
     * @param subject The email subject
     * @param message The message content
     */
    public static void sendEmail(String email, String subject, String message) {
        sendEmail(email, subject, message, false);
    }
    
    /**
     * Sends a general email with HTML option.
     * Overloaded method with HTML parameter.
     * 
     * @param email The recipient's email address
     * @param subject The email subject
     * @param message The message content
     * @param isHtml Whether the message is HTML formatted
     */
    public static void sendEmail(String email, String subject, String message, boolean isHtml) {
        if (!EMAIL_ENABLED) {
            LOGGER.info("Email disabled. Would have sent to: " + email + " Subject: " + subject);
            return;
        }
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });
        
        try {
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress(EMAIL_USERNAME));
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            mimeMessage.setSubject(subject);
            
            if (isHtml) {
                mimeMessage.setContent(message, "text/html; charset=utf-8");
            } else {
                mimeMessage.setText(message);
            }
            
            Transport.send(mimeMessage);
            LOGGER.info("Email sent successfully to: " + email);
            
        } catch (MessagingException e) {
            LOGGER.log(Level.WARNING, "Failed to send email to: " + email, e);
        }
    }
    
    /**
     * Alias for sendEmail with subject.
     * 
     * @param userEmail The recipient's email address
     * @param subject The email subject
     * @param message The message content
     */
    public static void sendGeneralEmail(String userEmail, String subject, String message) {
        sendEmail(userEmail, subject, message);
    }
    
    /**
     * Validates an email address format.
     * 
     * @param email The email to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates a phone number format.
     * 
     * @param phone The phone number to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }
    
    /**
     * Validates password strength.
     * Password must be at least 8 characters with at least one letter and one number.
     * 
     * @param password The password to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        return hasLetter && hasDigit;
    }
    
    /**
     * Validates that a string is not null or empty.
     * 
     * @param str The string to validate
     * @return true if valid (not null and not empty), false otherwise
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Formats a monetary amount for display.
     * 
     * @param amount The amount to format
     * @return Formatted string with currency symbol
     */
    public static String formatCurrency(double amount) {
        return String.format("$%.2f", amount);
    }
    
    /**
     * Truncates a string to a maximum length with ellipsis.
     * 
     * @param str The string to truncate
     * @param maxLength The maximum length
     * @return The truncated string
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
