package com.example.donationapp.email;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Email Service - Main service class for sending emails.
 * Implements Singleton pattern for resource efficiency.
 * Supports both synchronous and asynchronous email sending.
 * 
 * Features:
 * - Async email sending (non-blocking UI)
 * - HTML email support
 * - Retry mechanism
 * - Connection pooling via thread pool
 * - Comprehensive error handling
 */
public class EmailService {
    
    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static EmailService instance;
    
    private final EmailConfig config;
    private final ExecutorService emailExecutor;
    private Session mailSession;
    
    // Retry configuration
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000;
    
    /**
     * Private constructor - initializes email service
     */
    private EmailService() {
        this.config = EmailConfig.getInstance();
        // Thread pool for async email operations
        this.emailExecutor = Executors.newFixedThreadPool(3);
        initializeSession();
    }
    
    /**
     * Gets the singleton instance
     */
    public static EmailService getInstance() {
        if (instance == null) {
            synchronized (EmailService.class) {
                if (instance == null) {
                    instance = new EmailService();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initializes the JavaMail session with authentication
     */
    private void initializeSession() {
        if (!config.isEnabled()) {
            LOGGER.info("Email service is disabled. Enable it in email.properties");
            return;
        }
        
        this.mailSession = Session.getInstance(config.getSmtpProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getUsername(), config.getPassword());
            }
        });
        
        mailSession.setDebug(config.isDebugMode());
        LOGGER.info("Email session initialized successfully");
    }
    
    /**
     * Reinitializes the session (useful after config changes)
     */
    public void reinitialize() {
        config.reload();
        initializeSession();
    }
    
    // ================== CORE SENDING METHODS ==================
    
    /**
     * Sends an email asynchronously (non-blocking)
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML content of the email
     * @return CompletableFuture that completes when email is sent
     */
    public CompletableFuture<EmailResult> sendAsync(String to, String subject, String htmlContent) {
        return CompletableFuture.supplyAsync(() -> {
            return sendWithRetry(to, subject, htmlContent);
        }, emailExecutor);
    }
    
    /**
     * Sends an email synchronously (blocking)
     * 
     * @param to Recipient email address
     * @param subject Email subject
     * @param htmlContent HTML content of the email
     * @return EmailResult indicating success or failure
     */
    public EmailResult send(String to, String subject, String htmlContent) {
        return sendWithRetry(to, subject, htmlContent);
    }
    
    /**
     * Sends email with retry mechanism
     */
    private EmailResult sendWithRetry(String to, String subject, String htmlContent) {
        if (!config.isEnabled()) {
            LOGGER.warning("Email service disabled. Would have sent to: " + to);
            return new EmailResult(false, "Email service is disabled", null);
        }
        
        if (mailSession == null) {
            initializeSession();
            if (mailSession == null) {
                return new EmailResult(false, "Failed to initialize email session", null);
            }
        }
        
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                sendEmail(to, subject, htmlContent);
                LOGGER.info("Email sent successfully to: " + to + " (attempt " + attempt + ")");
                return new EmailResult(true, "Email sent successfully", null);
                
            } catch (MessagingException e) {
                lastException = e;
                LOGGER.log(Level.WARNING, "Email send attempt " + attempt + " failed", e);
                
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        
        LOGGER.log(Level.SEVERE, "Failed to send email after " + MAX_RETRIES + " attempts", lastException);
        return new EmailResult(false, "Failed after " + MAX_RETRIES + " attempts: " + 
                              (lastException != null ? lastException.getMessage() : "Unknown error"), lastException);
    }
    
    /**
     * Core email sending logic
     */
    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        Message message = new MimeMessage(mailSession);
        
        // Set From
        try {
            message.setFrom(new InternetAddress(config.getFromEmail(), config.getFromName(), "UTF-8"));
        } catch (java.io.UnsupportedEncodingException e) {
            // Fallback without encoding
            message.setFrom(new InternetAddress(config.getFromEmail()));
        }
        
        // Set To
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        
        // Set Subject
        message.setSubject(subject);
        
        // Set Date
        message.setSentDate(new Date());
        
        // Set HTML Content
        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
        
        Multipart multipart = new MimeMultipart("alternative");
        multipart.addBodyPart(htmlPart);
        
        message.setContent(multipart);
        
        // Send
        Transport.send(message);
    }
    
    // ================== CONVENIENCE METHODS FOR SPECIFIC EMAILS ==================
    
    /**
     * Sends donation confirmation email to donor (async)
     */
    public CompletableFuture<EmailResult> sendDonationConfirmation(
            String donorEmail,
            String donorName,
            String donationType,
            String amount,
            String caseName,
            String transactionId,
            String date
    ) {
        String subject = "✅ Donation Confirmed - Thank You for Your Generosity!";
        String html = EmailTemplates.getDonationConfirmationTemplate(
            donorName, donationType, amount, caseName, transactionId, date
        );
        return sendAsync(donorEmail, subject, html);
    }
    
    /**
     * Sends case submission confirmation email to receiver (async)
     */
    public CompletableFuture<EmailResult> sendCaseSubmissionConfirmation(
            String receiverEmail,
            String receiverName,
            String caseTitle,
            String caseDescription,
            String targetAmount,
            String caseId,
            String submissionDate
    ) {
        String subject = "📝 Case Submission Received - Under Review";
        String html = EmailTemplates.getCaseSubmissionTemplate(
            receiverName, caseTitle, caseDescription, targetAmount, caseId, submissionDate
        );
        return sendAsync(receiverEmail, subject, html);
    }
    
    /**
     * Sends case approval notification email to receiver (async)
     */
    public CompletableFuture<EmailResult> sendCaseApprovalNotification(
            String receiverEmail,
            String receiverName,
            String caseTitle,
            String caseId,
            String targetAmount,
            String approvalDate
    ) {
        String subject = "🎉 Great News! Your Case Has Been Approved!";
        String html = EmailTemplates.getCaseAcceptanceTemplate(
            receiverName, caseTitle, caseId, targetAmount, approvalDate
        );
        return sendAsync(receiverEmail, subject, html);
    }
    
    /**
     * Sends case decline notification email to receiver (async)
     */
    public CompletableFuture<EmailResult> sendCaseDeclineNotification(
            String receiverEmail,
            String receiverName,
            String caseTitle,
            String caseId,
            String declineReason,
            String declineDate
    ) {
        String subject = "📋 Case Review Update - Action Required";
        String html = EmailTemplates.getCaseDeclineTemplate(
            receiverName, caseTitle, caseId, declineReason, declineDate
        );
        return sendAsync(receiverEmail, subject, html);
    }
    
    /**
     * Sends welcome email to new user (async)
     */
    public CompletableFuture<EmailResult> sendWelcomeEmail(
            String userEmail,
            String userName,
            String userRole
    ) {
        String subject = "🎉 Welcome to Donation Management System!";
        String html = EmailTemplates.getWelcomeTemplate(userName, userRole);
        return sendAsync(userEmail, subject, html);
    }
    
    /**
     * Sends donation received notification to receiver (async)
     */
    public CompletableFuture<EmailResult> sendDonationReceivedNotification(
            String receiverEmail,
            String receiverName,
            String donorName,
            String caseName,
            String donationType,
            String amount,
            String currentTotal,
            String targetAmount,
            String date
    ) {
        String subject = "🎁 You Received a New Donation!";
        String html = EmailTemplates.getDonationReceivedTemplate(
            receiverName, donorName, caseName, donationType, amount, currentTotal, targetAmount, date
        );
        return sendAsync(receiverEmail, subject, html);
    }
    
    /**
     * Sends password reset email (async)
     */
    public CompletableFuture<EmailResult> sendPasswordResetEmail(
            String userEmail,
            String userName,
            String resetCode,
            String expiryTime
    ) {
        String subject = "🔐 Password Reset Request";
        String html = EmailTemplates.getPasswordResetTemplate(userName, resetCode, expiryTime);
        return sendAsync(userEmail, subject, html);
    }
    
    // ================== UTILITY METHODS ==================
    
    /**
     * Tests the email configuration by sending a test email
     */
    public CompletableFuture<EmailResult> sendTestEmail(String to) {
        String subject = "🧪 Test Email - Donation Management System";
        String html = EmailTemplates.getBaseTemplate("Test Email", """
            <h2 class="greeting">Email Configuration Test</h2>
            <p class="message">
                If you're reading this, your email configuration is working correctly! 🎉
            </p>
            <div class="highlight-box">
                <p><strong>SMTP Host:</strong> %s</p>
                <p><strong>SMTP Port:</strong> %s</p>
                <p><strong>From:</strong> %s</p>
                <p><strong>Time:</strong> %s</p>
            </div>
            """.formatted(
                config.getSmtpHost(),
                config.getSmtpPort(),
                config.getFromEmail(),
                new Date().toString()
            ));
        
        return sendAsync(to, subject, html);
    }
    
    /**
     * Checks if email service is properly configured and enabled
     */
    public boolean isConfigured() {
        return config.isEnabled() && 
               config.getUsername() != null && !config.getUsername().isEmpty() &&
               config.getPassword() != null && !config.getPassword().isEmpty();
    }
    
    /**
     * Gets the current status of email service
     */
    public String getStatus() {
        if (!config.isEnabled()) {
            return "DISABLED - Email service is turned off";
        }
        if (!isConfigured()) {
            return "NOT CONFIGURED - Missing credentials";
        }
        return "READY - Email service is operational";
    }
    
    /**
     * Gracefully shuts down the email executor
     */
    public void shutdown() {
        emailExecutor.shutdown();
        LOGGER.info("Email service shut down");
    }
    
    // ================== RESULT CLASS ==================
    
    /**
     * Result class for email operations
     */
    public static class EmailResult {
        private final boolean success;
        private final String message;
        private final Exception exception;
        
        public EmailResult(boolean success, String message, Exception exception) {
            this.success = success;
            this.message = message;
            this.exception = exception;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Exception getException() { return exception; }
        
        @Override
        public String toString() {
            return "EmailResult{success=" + success + ", message='" + message + "'}";
        }
    }
}
