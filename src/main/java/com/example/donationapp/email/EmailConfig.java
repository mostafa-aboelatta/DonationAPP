package com.example.donationapp.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Email Configuration Manager.
 * Loads email settings from properties file for security.
 * Implements Singleton pattern.
 */
public class EmailConfig {
    
    private static final Logger LOGGER = Logger.getLogger(EmailConfig.class.getName());
    private static EmailConfig instance;
    
    // Default values (can be overridden by properties file)
    private String smtpHost = "smtp.gmail.com";
    private String smtpPort = "587";
    private String username = "";
    private String password = "";
    private String fromEmail = "";
    private String fromName = "Donation Management System";
    private boolean enabled = false;
    private boolean debugMode = false;
    private int connectionTimeout = 10000;
    private int readTimeout = 10000;
    
    /**
     * Private constructor - loads configuration from file
     */
    private EmailConfig() {
        loadConfiguration();
    }
    
    /**
     * Gets the singleton instance
     */
    public static EmailConfig getInstance() {
        if (instance == null) {
            synchronized (EmailConfig.class) {
                if (instance == null) {
                    instance = new EmailConfig();
                }
            }
        }
        return instance;
    }
    
    /**
     * Loads configuration from email.properties file
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        
        // Try to load from classpath
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("email.properties")) {
            
            if (input != null) {
                props.load(input);
                
                this.smtpHost = props.getProperty("email.smtp.host", smtpHost);
                this.smtpPort = props.getProperty("email.smtp.port", smtpPort);
                this.username = props.getProperty("email.username", username);
                this.password = props.getProperty("email.password", password);
                this.fromEmail = props.getProperty("email.from.address", username);
                this.fromName = props.getProperty("email.from.name", fromName);
                this.enabled = Boolean.parseBoolean(props.getProperty("email.enabled", "false"));
                this.debugMode = Boolean.parseBoolean(props.getProperty("email.debug", "false"));
                this.connectionTimeout = Integer.parseInt(props.getProperty("email.timeout.connection", "10000"));
                this.readTimeout = Integer.parseInt(props.getProperty("email.timeout.read", "10000"));
                
                LOGGER.info("Email configuration loaded successfully. Enabled: " + enabled);
            } else {
                LOGGER.warning("email.properties not found. Using default (disabled) configuration.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading email configuration", e);
        }
    }
    
    /**
     * Reloads configuration from file
     */
    public void reload() {
        loadConfiguration();
    }
    
    /**
     * Manually configure email settings (for testing or programmatic setup)
     */
    public void configure(String host, String port, String username, String password) {
        this.smtpHost = host;
        this.smtpPort = port;
        this.username = username;
        this.password = password;
        this.fromEmail = username;
        this.enabled = true;
        LOGGER.info("Email manually configured for: " + username);
    }
    
    /**
     * Gets SMTP properties for JavaMail session
     */
    public Properties getSmtpProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.connectiontimeout", String.valueOf(connectionTimeout));
        props.put("mail.smtp.timeout", String.valueOf(readTimeout));
        props.put("mail.smtp.writetimeout", String.valueOf(readTimeout));
        
        // For Gmail with SSL (alternative)
        if ("465".equals(smtpPort)) {
            props.put("mail.smtp.socketFactory.port", smtpPort);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.ssl.enable", "true");
        }
        
        if (debugMode) {
            props.put("mail.debug", "true");
        }
        
        return props;
    }
    
    // Getters
    public String getSmtpHost() { return smtpHost; }
    public String getSmtpPort() { return smtpPort; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFromEmail() { return fromEmail; }
    public String getFromName() { return fromName; }
    public boolean isEnabled() { return enabled; }
    public boolean isDebugMode() { return debugMode; }
    
    // Setters for programmatic configuration
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setDebugMode(boolean debugMode) { this.debugMode = debugMode; }
}
