package com.example.donationapp.email;

/**
 * Email Template Manager.
 * Contains all HTML email templates for the Donation Management System.
 * Templates use placeholders like {{DONOR_NAME}}, {{AMOUNT}}, etc.
 */
public class EmailTemplates {
    
    // Color scheme (customize as needed)
    private static final String PRIMARY_COLOR = "#4CAF50";      // Green
    private static final String SECONDARY_COLOR = "#2196F3";    // Blue
    private static final String SUCCESS_COLOR = "#4CAF50";      // Green
    private static final String WARNING_COLOR = "#FF9800";      // Orange
    private static final String DANGER_COLOR = "#f44336";       // Red
    private static final String TEXT_COLOR = "#333333";
    private static final String LIGHT_BG = "#f5f5f5";
    
    /**
     * Gets the base HTML wrapper with header and footer
     */
    public static String getBaseTemplate(String title, String content) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        line-height: 1.6;
                        color: %s;
                        background-color: %s;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                    }
                    .email-wrapper {
                        background-color: #ffffff;
                        border-radius: 10px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(135deg, %s, %s);
                        color: white;
                        padding: 30px;
                        text-align: center;
                    }
                    .header h1 {
                        font-size: 24px;
                        margin-bottom: 5px;
                    }
                    .header p {
                        opacity: 0.9;
                        font-size: 14px;
                    }
                    .content {
                        padding: 30px;
                    }
                    .greeting {
                        font-size: 20px;
                        color: %s;
                        margin-bottom: 20px;
                    }
                    .message {
                        margin-bottom: 25px;
                        font-size: 15px;
                    }
                    .highlight-box {
                        background-color: %s;
                        border-left: 4px solid %s;
                        padding: 20px;
                        margin: 25px 0;
                        border-radius: 0 8px 8px 0;
                    }
                    .detail-row {
                        display: flex;
                        justify-content: space-between;
                        padding: 10px 0;
                        border-bottom: 1px solid #eee;
                    }
                    .detail-label {
                        font-weight: 600;
                        color: #666;
                    }
                    .detail-value {
                        color: %s;
                    }
                    .amount {
                        font-size: 28px;
                        font-weight: bold;
                        color: %s;
                        text-align: center;
                        padding: 20px;
                    }
                    .button {
                        display: inline-block;
                        padding: 14px 30px;
                        background-color: %s;
                        color: white !important;
                        text-decoration: none;
                        border-radius: 25px;
                        font-weight: 600;
                        text-align: center;
                        margin: 20px 0;
                    }
                    .button:hover {
                        opacity: 0.9;
                    }
                    .footer {
                        background-color: #333;
                        color: #aaa;
                        padding: 25px;
                        text-align: center;
                        font-size: 12px;
                    }
                    .footer a {
                        color: %s;
                        text-decoration: none;
                    }
                    .status-badge {
                        display: inline-block;
                        padding: 8px 16px;
                        border-radius: 20px;
                        font-weight: 600;
                        font-size: 14px;
                    }
                    .status-success { background-color: #e8f5e9; color: %s; }
                    .status-pending { background-color: #fff3e0; color: %s; }
                    .status-declined { background-color: #ffebee; color: %s; }
                    .icon {
                        font-size: 48px;
                        margin-bottom: 10px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="email-wrapper">
                        <div class="header">
                            <div class="icon">💝</div>
                            <h1>Donation Management System</h1>
                            <p>Making a difference, one donation at a time</p>
                        </div>
                        <div class="content">
                            %s
                        </div>
                        <div class="footer">
                            <p>© 2024 Donation Management System</p>
                            <p style="margin-top: 10px;">
                                This is an automated message. Please do not reply directly to this email.
                            </p>
                            <p style="margin-top: 10px;">
                                <a href="#">Privacy Policy</a> | 
                                <a href="#">Terms of Service</a> | 
                                <a href="#">Contact Us</a>
                            </p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                title,              // Title
                TEXT_COLOR,         // Body text color
                LIGHT_BG,           // Body background
                PRIMARY_COLOR,      // Header gradient start
                SECONDARY_COLOR,    // Header gradient end
                PRIMARY_COLOR,      // Greeting color
                LIGHT_BG,           // Highlight box background
                PRIMARY_COLOR,      // Highlight box border
                TEXT_COLOR,         // Detail value color
                SUCCESS_COLOR,      // Amount color
                PRIMARY_COLOR,      // Button background
                PRIMARY_COLOR,      // Footer link color
                SUCCESS_COLOR,      // Success badge
                WARNING_COLOR,      // Pending badge
                DANGER_COLOR,       // Declined badge
                content             // Main content
            );
    }
    
    // ==================== DONATION CONFIRMATION ====================
    
    /**
     * Email sent to donor after successful donation
     */
    public static String getDonationConfirmationTemplate(
            String donorName,
            String donationType,
            String amount,
            String caseName,
            String transactionId,
            String date
    ) {
        String content = """
            <h2 class="greeting">Dear %s,</h2>
            
            <p class="message">
                Thank you for your generous donation! Your kindness makes a real difference 
                in the lives of those in need. We are deeply grateful for your support.
            </p>
            
            <div class="highlight-box">
                <h3 style="margin-bottom: 15px; color: #333;">📋 Donation Details</h3>
                
                <div class="detail-row">
                    <span class="detail-label">Transaction ID:</span>
                    <span class="detail-value"><strong>%s</strong></span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Donation Type:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Amount/Value:</span>
                    <span class="detail-value" style="color: #4CAF50; font-weight: bold;">%s</span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Beneficiary Case:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Date:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row" style="border-bottom: none;">
                    <span class="detail-label">Status:</span>
                    <span class="status-badge status-success">✓ Confirmed</span>
                </div>
            </div>
            
            <p class="message">
                Your donation will be processed and directed to the intended cause. 
                You will receive updates on how your contribution is making an impact.
            </p>
            
            <div style="text-align: center;">
                <p style="color: #666; font-style: italic; margin-top: 20px;">
                    "No act of kindness, no matter how small, is ever wasted." - Aesop
                </p>
            </div>
            
            <p class="message" style="margin-top: 25px;">
                If you have any questions about your donation, please don't hesitate to contact us.
            </p>
            
            <p style="margin-top: 20px;">
                With gratitude,<br>
                <strong>The Donation Management Team</strong>
            </p>
            """.formatted(donorName, transactionId, donationType, amount, caseName, date);
        
        return getBaseTemplate("Donation Confirmation", content);
    }
    
    // ==================== CASE SUBMISSION CONFIRMATION ====================
    
    /**
     * Email sent to receiver after submitting a charity case
     */
    public static String getCaseSubmissionTemplate(
            String receiverName,
            String caseTitle,
            String caseDescription,
            String targetAmount,
            String caseId,
            String submissionDate
    ) {
        String content = """
            <h2 class="greeting">Dear %s,</h2>
            
            <p class="message">
                Thank you for submitting your charity case to our platform. We have received 
                your request and it is now under review by our administrative team.
            </p>
            
            <div class="highlight-box">
                <h3 style="margin-bottom: 15px; color: #333;">📝 Case Submission Details</h3>
                
                <div class="detail-row">
                    <span class="detail-label">Case ID:</span>
                    <span class="detail-value"><strong>%s</strong></span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Case Title:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Target Amount:</span>
                    <span class="detail-value" style="color: #2196F3; font-weight: bold;">%s</span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Submission Date:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row" style="border-bottom: none;">
                    <span class="detail-label">Status:</span>
                    <span class="status-badge status-pending">⏳ Pending Review</span>
                </div>
            </div>
            
            <div style="background-color: #e3f2fd; padding: 15px; border-radius: 8px; margin: 20px 0;">
                <h4 style="color: #1976D2; margin-bottom: 10px;">📄 Case Description:</h4>
                <p style="color: #333; font-size: 14px;">%s</p>
            </div>
            
            <h3 style="color: #333; margin-top: 25px;">What happens next?</h3>
            <ol style="margin: 15px 0; padding-left: 20px; color: #666;">
                <li style="margin-bottom: 10px;">Our team will review your case within 2-3 business days</li>
                <li style="margin-bottom: 10px;">You will receive an email notification about the decision</li>
                <li style="margin-bottom: 10px;">If approved, your case will be visible to potential donors</li>
                <li style="margin-bottom: 10px;">You'll be notified whenever donations are received</li>
            </ol>
            
            <p class="message">
                We appreciate your patience during the review process. If you need to provide 
                additional information, please contact our support team.
            </p>
            
            <p style="margin-top: 20px;">
                Best regards,<br>
                <strong>The Donation Management Team</strong>
            </p>
            """.formatted(receiverName, caseId, caseTitle, targetAmount, submissionDate, caseDescription);
        
        return getBaseTemplate("Case Submission Received", content);
    }
    
    // ==================== CASE ACCEPTANCE NOTIFICATION ====================
    
    /**
     * Email sent to receiver when their case is approved
     */
    public static String getCaseAcceptanceTemplate(
            String receiverName,
            String caseTitle,
            String caseId,
            String targetAmount,
            String approvalDate
    ) {
        String content = """
            <h2 class="greeting">🎉 Great News, %s!</h2>
            
            <p class="message" style="font-size: 16px;">
                We are pleased to inform you that your charity case has been 
                <strong style="color: #4CAF50;">APPROVED</strong> and is now live on our platform!
            </p>
            
            <div style="text-align: center; margin: 30px 0;">
                <div style="display: inline-block; background: linear-gradient(135deg, #4CAF50, #8BC34A); 
                            color: white; padding: 20px 40px; border-radius: 50px; font-size: 18px;">
                    ✅ Case Approved!
                </div>
            </div>
            
            <div class="highlight-box" style="border-left-color: #4CAF50;">
                <h3 style="margin-bottom: 15px; color: #333;">📋 Approved Case Details</h3>
                
                <div class="detail-row">
                    <span class="detail-label">Case ID:</span>
                    <span class="detail-value"><strong>%s</strong></span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Case Title:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Target Amount:</span>
                    <span class="detail-value" style="color: #4CAF50; font-weight: bold;">%s</span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Approval Date:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row" style="border-bottom: none;">
                    <span class="detail-label">Status:</span>
                    <span class="status-badge status-success">✓ Active</span>
                </div>
            </div>
            
            <h3 style="color: #333; margin-top: 25px;">🚀 What's Next?</h3>
            <ul style="margin: 15px 0; padding-left: 20px; color: #666;">
                <li style="margin-bottom: 10px;">Your case is now visible to all registered donors</li>
                <li style="margin-bottom: 10px;">You will receive email notifications for each donation received</li>
                <li style="margin-bottom: 10px;">Track your case progress through your dashboard</li>
                <li style="margin-bottom: 10px;">Share your case link to reach more potential donors</li>
            </ul>
            
            <div style="background-color: #e8f5e9; padding: 20px; border-radius: 8px; margin: 25px 0; text-align: center;">
                <p style="color: #2E7D32; font-size: 14px; margin-bottom: 10px;">
                    💡 <strong>Pro Tip:</strong> Complete your profile and add more details 
                    to increase donor confidence!
                </p>
            </div>
            
            <p class="message">
                We wish you success in reaching your fundraising goal. Our team is here 
                to support you throughout this journey.
            </p>
            
            <p style="margin-top: 20px;">
                Warm regards,<br>
                <strong>The Donation Management Team</strong>
            </p>
            """.formatted(receiverName, caseId, caseTitle, targetAmount, approvalDate);
        
        return getBaseTemplate("Case Approved!", content);
    }
    
    // ==================== CASE DECLINE NOTIFICATION ====================
    
    /**
     * Email sent to receiver when their case is declined
     */
    public static String getCaseDeclineTemplate(
            String receiverName,
            String caseTitle,
            String caseId,
            String declineReason,
            String declineDate
    ) {
        String content = """
            <h2 class="greeting">Dear %s,</h2>
            
            <p class="message">
                Thank you for submitting your charity case to our platform. After careful review 
                by our administrative team, we regret to inform you that your case could not be 
                approved at this time.
            </p>
            
            <div class="highlight-box" style="border-left-color: #f44336; background-color: #fff5f5;">
                <h3 style="margin-bottom: 15px; color: #333;">📋 Case Details</h3>
                
                <div class="detail-row">
                    <span class="detail-label">Case ID:</span>
                    <span class="detail-value"><strong>%s</strong></span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Case Title:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Review Date:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row" style="border-bottom: none;">
                    <span class="detail-label">Status:</span>
                    <span class="status-badge status-declined">✗ Not Approved</span>
                </div>
            </div>
            
            <div style="background-color: #fff3e0; padding: 20px; border-radius: 8px; margin: 25px 0; 
                        border: 1px solid #ffcc80;">
                <h4 style="color: #E65100; margin-bottom: 10px;">📝 Reason for Decision:</h4>
                <p style="color: #333; font-size: 14px;">%s</p>
            </div>
            
            <h3 style="color: #333; margin-top: 25px;">What can you do?</h3>
            <ul style="margin: 15px 0; padding-left: 20px; color: #666;">
                <li style="margin-bottom: 10px;">Review the feedback provided above</li>
                <li style="margin-bottom: 10px;">Address the concerns mentioned in the reason</li>
                <li style="margin-bottom: 10px;">Gather any additional documentation if needed</li>
                <li style="margin-bottom: 10px;">Submit a new application with updated information</li>
                <li style="margin-bottom: 10px;">Contact our support team if you have questions</li>
            </ul>
            
            <div style="background-color: #e3f2fd; padding: 15px; border-radius: 8px; margin: 20px 0;">
                <p style="color: #1565C0; font-size: 14px;">
                    💙 <strong>We're Here to Help:</strong> If you believe this decision was made 
                    in error or need clarification, please don't hesitate to reach out to our 
                    support team. We're committed to helping those in need.
                </p>
            </div>
            
            <p class="message">
                We understand this may be disappointing news, but we encourage you to reapply 
                once you've addressed the concerns mentioned above. We're rooting for you!
            </p>
            
            <p style="margin-top: 20px;">
                With support,<br>
                <strong>The Donation Management Team</strong>
            </p>
            """.formatted(receiverName, caseId, caseTitle, declineDate, declineReason);
        
        return getBaseTemplate("Case Review Update", content);
    }
    
    // ==================== WELCOME EMAIL ====================
    
    /**
     * Welcome email for new user registration
     */
    public static String getWelcomeTemplate(String userName, String userRole) {
        String roleSpecificMessage = switch (userRole.toLowerCase()) {
            case "donor" -> """
                As a donor, you can:
                <ul style="margin: 10px 0; padding-left: 20px;">
                    <li>Browse verified charity cases</li>
                    <li>Make secure donations</li>
                    <li>Track your donation history</li>
                    <li>Receive updates on cases you've supported</li>
                </ul>
                """;
            case "receiver" -> """
                As a receiver, you can:
                <ul style="margin: 10px 0; padding-left: 20px;">
                    <li>Submit charity cases for review</li>
                    <li>Track donations received</li>
                    <li>Communicate with donors</li>
                    <li>Update your case progress</li>
                </ul>
                """;
            case "volunteer" -> """
                As a volunteer, you can:
                <ul style="margin: 10px 0; padding-left: 20px;">
                    <li>Help verify charity cases</li>
                    <li>Assist with donation distribution</li>
                    <li>Support administrative tasks</li>
                    <li>Make a difference in your community</li>
                </ul>
                """;
            default -> """
                You now have full access to our platform features.
                """;
        };
        
        String content = """
            <h2 class="greeting">Welcome to the Family, %s! 🎉</h2>
            
            <p class="message" style="font-size: 16px;">
                Congratulations! Your account has been successfully created. 
                We're thrilled to have you join our community of changemakers.
            </p>
            
            <div style="text-align: center; margin: 30px 0;">
                <div style="display: inline-block; background: linear-gradient(135deg, #2196F3, #21CBF3); 
                            color: white; padding: 15px 30px; border-radius: 25px;">
                    🏷️ Your Role: <strong>%s</strong>
                </div>
            </div>
            
            <div class="highlight-box">
                <h3 style="margin-bottom: 15px; color: #333;">✨ What You Can Do</h3>
                %s
            </div>
            
            <h3 style="color: #333; margin-top: 25px;">🚀 Getting Started</h3>
            <ol style="margin: 15px 0; padding-left: 20px; color: #666;">
                <li style="margin-bottom: 10px;">Complete your profile for better experience</li>
                <li style="margin-bottom: 10px;">Explore available charity cases</li>
                <li style="margin-bottom: 10px;">Start making a difference today!</li>
            </ol>
            
            <p class="message">
                Together, we can create positive change in our community. 
                Thank you for being part of this journey!
            </p>
            
            <p style="margin-top: 20px;">
                Welcome aboard,<br>
                <strong>The Donation Management Team</strong>
            </p>
            """.formatted(userName, userRole, roleSpecificMessage);
        
        return getBaseTemplate("Welcome!", content);
    }
    
    // ==================== DONATION RECEIVED (FOR RECEIVER) ====================
    
    /**
     * Notification to receiver when they receive a donation
     */
    public static String getDonationReceivedTemplate(
            String receiverName,
            String donorName,
            String caseName,
            String donationType,
            String amount,
            String currentTotal,
            String targetAmount,
            String date
    ) {
        String content = """
            <h2 class="greeting">Great News, %s! 🎁</h2>
            
            <p class="message" style="font-size: 16px;">
                You've received a new donation for your charity case! 
                Someone believes in your cause and wants to help.
            </p>
            
            <div style="text-align: center; margin: 30px 0;">
                <div class="amount">%s</div>
                <p style="color: #666;">Donation Received!</p>
            </div>
            
            <div class="highlight-box">
                <h3 style="margin-bottom: 15px; color: #333;">📦 Donation Details</h3>
                
                <div class="detail-row">
                    <span class="detail-label">From:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Case:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Type:</span>
                    <span class="detail-value">%s</span>
                </div>
                
                <div class="detail-row">
                    <span class="detail-label">Date:</span>
                    <span class="detail-value">%s</span>
                </div>
            </div>
            
            <div style="background-color: #e8f5e9; padding: 20px; border-radius: 8px; margin: 25px 0;">
                <h4 style="color: #2E7D32; margin-bottom: 15px;">📊 Fundraising Progress</h4>
                <div style="background-color: #c8e6c9; border-radius: 10px; height: 20px; overflow: hidden;">
                    <div style="background: linear-gradient(90deg, #4CAF50, #8BC34A); height: 100%%; 
                                width: 60%%; border-radius: 10px;"></div>
                </div>
                <p style="text-align: center; margin-top: 10px; color: #333;">
                    <strong>%s</strong> raised of <strong>%s</strong> goal
                </p>
            </div>
            
            <p class="message">
                Keep sharing your story to reach more potential donors. 
                Every contribution brings you closer to your goal!
            </p>
            
            <p style="margin-top: 20px;">
                Cheers,<br>
                <strong>The Donation Management Team</strong>
            </p>
            """.formatted(receiverName, amount, donorName, caseName, donationType, date, currentTotal, targetAmount);
        
        return getBaseTemplate("New Donation Received!", content);
    }
    
    // ==================== PASSWORD RESET ====================
    
    /**
     * Password reset email template
     */
    public static String getPasswordResetTemplate(String userName, String resetCode, String expiryTime) {
        String content = """
            <h2 class="greeting">Password Reset Request</h2>
            
            <p class="message">
                Hello %s,<br><br>
                We received a request to reset your password. Use the code below to 
                complete the reset process.
            </p>
            
            <div style="text-align: center; margin: 30px 0;">
                <div style="display: inline-block; background-color: #f5f5f5; 
                            padding: 20px 40px; border-radius: 10px; 
                            border: 2px dashed #ccc;">
                    <p style="color: #666; font-size: 14px; margin-bottom: 10px;">Your Reset Code:</p>
                    <p style="font-size: 32px; font-weight: bold; letter-spacing: 8px; 
                              color: #333; margin: 0;">%s</p>
                </div>
            </div>
            
            <p style="text-align: center; color: #ff9800; font-size: 14px;">
                ⏰ This code expires in <strong>%s</strong>
            </p>
            
            <div style="background-color: #fff3e0; padding: 15px; border-radius: 8px; 
                        margin: 25px 0; border-left: 4px solid #ff9800;">
                <p style="color: #E65100; font-size: 14px; margin: 0;">
                    ⚠️ <strong>Security Notice:</strong> If you didn't request this reset, 
                    please ignore this email or contact support immediately.
                </p>
            </div>
            
            <p class="message">
                For security reasons, never share this code with anyone.
            </p>
            
            <p style="margin-top: 20px;">
                Stay secure,<br>
                <strong>The Donation Management Team</strong>
            </p>
            """.formatted(userName, resetCode, expiryTime);
        
        return getBaseTemplate("Password Reset", content);
    }
}
