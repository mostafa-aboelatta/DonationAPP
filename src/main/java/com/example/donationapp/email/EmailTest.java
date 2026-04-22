package com.example.donationapp.email;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Email Service Test Class.
 * Use this to test your email configuration before integrating into the main application.
 * 
 * INSTRUCTIONS:
 * 1. First configure email.properties with your Gmail and App Password
 * 2. Change testEmail below to your actual test email address
 * 3. Run this class as a Java application
 * 4. Check your inbox (and spam folder) for test emails
 */
public class EmailTest {
    
    public static void main(String[] args) {
        EmailService service = EmailService.getInstance();
        
        // ========================================
        // CHANGE THIS TO YOUR TEST EMAIL ADDRESS
        // ========================================
        String testEmail = "your-test-email@gmail.com";
        // ========================================
        
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        
        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           EMAIL SERVICE TEST - Donation System           ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("📧 Email Service Status: " + service.getStatus());
        System.out.println("📧 Is Configured: " + service.isConfigured());
        System.out.println();
        
        if (!service.isConfigured()) {
            System.err.println("❌ Email not configured! Please check email.properties:");
            System.err.println("   1. Set email.enabled=true");
            System.err.println("   2. Set email.username=your-email@gmail.com");
            System.err.println("   3. Set email.password=your-16-char-app-password");
            System.err.println();
            System.err.println("📖 See EMAIL_IMPLEMENTATION_GUIDE.md for App Password setup instructions.");
            return;
        }
        
        if (testEmail.equals("your-test-email@gmail.com")) {
            System.err.println("⚠️  Please change the 'testEmail' variable to your actual email address!");
            return;
        }
        
        System.out.println("🚀 Starting email tests to: " + testEmail);
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println();
        
        // Test 1: Welcome Email
        System.out.println("📬 Test 1: Sending Welcome Email...");
        service.sendWelcomeEmail(testEmail, "John Doe", "Donor")
            .thenAccept(r -> System.out.println("   " + (r.isSuccess() ? "✅" : "❌") + " Result: " + r.getMessage()));
        
        sleep(2000);
        
        // Test 2: Donation Confirmation
        System.out.println("📬 Test 2: Sending Donation Confirmation...");
        service.sendDonationConfirmation(
            testEmail, "John Doe", "Financial", "$100.00", 
            "Medical Aid Fund", "TXN123456", today
        ).thenAccept(r -> System.out.println("   " + (r.isSuccess() ? "✅" : "❌") + " Result: " + r.getMessage()));
        
        sleep(2000);
        
        // Test 3: Case Submission Confirmation
        System.out.println("📬 Test 3: Sending Case Submission Confirmation...");
        service.sendCaseSubmissionConfirmation(
            testEmail, "Jane Receiver", "Medical Emergency",
            "Need help with medical bills for surgery", "$5000.00", "CASE-00001", today
        ).thenAccept(r -> System.out.println("   " + (r.isSuccess() ? "✅" : "❌") + " Result: " + r.getMessage()));
        
        sleep(2000);
        
        // Test 4: Case Approval
        System.out.println("📬 Test 4: Sending Case Approval Notification...");
        service.sendCaseApprovalNotification(
            testEmail, "Jane Receiver", "Medical Emergency",
            "CASE-00001", "$5000.00", today
        ).thenAccept(r -> System.out.println("   " + (r.isSuccess() ? "✅" : "❌") + " Result: " + r.getMessage()));
        
        sleep(2000);
        
        // Test 5: Case Decline
        System.out.println("📬 Test 5: Sending Case Decline Notification...");
        service.sendCaseDeclineNotification(
            testEmail, "Jane Receiver", "Sample Case",
            "CASE-00002", "Insufficient documentation provided. Please resubmit with required documents.", today
        ).thenAccept(r -> System.out.println("   " + (r.isSuccess() ? "✅" : "❌") + " Result: " + r.getMessage()));
        
        sleep(2000);
        
        // Test 6: Donation Received (for receivers)
        System.out.println("📬 Test 6: Sending Donation Received Notification...");
        service.sendDonationReceivedNotification(
            testEmail, "Jane Receiver", "Anonymous Donor", "Medical Emergency",
            "Financial", "$250.00", "$1,250.00", "$5,000.00", today
        ).thenAccept(r -> System.out.println("   " + (r.isSuccess() ? "✅" : "❌") + " Result: " + r.getMessage()));
        
        sleep(2000);
        
        // Test 7: Password Reset
        System.out.println("📬 Test 7: Sending Password Reset Email...");
        service.sendPasswordResetEmail(testEmail, "John Doe", "ABC123", "15 minutes")
            .thenAccept(r -> System.out.println("   " + (r.isSuccess() ? "✅" : "❌") + " Result: " + r.getMessage()));
        
        // Wait for all async operations to complete
        System.out.println();
        System.out.println("⏳ Waiting for emails to be sent...");
        sleep(10000);
        
        System.out.println();
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("✅ Test Complete!");
        System.out.println();
        System.out.println("📥 Check your inbox at: " + testEmail);
        System.out.println("📁 Also check your SPAM folder if emails are not visible");
        System.out.println();
        
        service.shutdown();
    }
    
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
