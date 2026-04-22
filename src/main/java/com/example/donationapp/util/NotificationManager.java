package com.example.donationapp.util;

import com.example.donationapp.interfaces.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Notification manager implementing the Observer design pattern.
 * Manages observers and broadcasts notifications.
 */
public class NotificationManager {
    
    private static final Logger LOGGER = Logger.getLogger(NotificationManager.class.getName());
    
    private static NotificationManager instance;
    private final List<Observer> observers;
    
    /**
     * Private constructor for singleton pattern.
     */
    private NotificationManager() {
        // Using CopyOnWriteArrayList for thread safety
        this.observers = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Gets the singleton instance of NotificationManager.
     * 
     * @return The NotificationManager instance
     */
    public static NotificationManager getInstance() {
        if (instance == null) {
            synchronized (NotificationManager.class) {
                if (instance == null) {
                    instance = new NotificationManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Adds an observer to receive notifications.
     * 
     * @param observer The observer to add
     */
    public void addObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
            LOGGER.info("Observer added. Total observers: " + observers.size());
        }
    }
    
    /**
     * Removes an observer from the notification list.
     * 
     * @param observer The observer to remove
     */
    public void removeObserver(Observer observer) {
        if (observer != null) {
            observers.remove(observer);
            LOGGER.info("Observer removed. Total observers: " + observers.size());
        }
    }
    
    /**
     * Notifies all registered observers with a message.
     * 
     * @param message The notification message
     */
    public void notifyObservers(String message) {
        LOGGER.info("Notifying " + observers.size() + " observers: " + message);
        for (Observer observer : observers) {
            try {
                observer.update(message);
            } catch (Exception e) {
                LOGGER.warning("Error notifying observer: " + e.getMessage());
            }
        }
    }
    
    /**
     * Gets the number of registered observers.
     * 
     * @return The observer count
     */
    public int getObserverCount() {
        return observers.size();
    }
    
    /**
     * Clears all registered observers.
     */
    public void clearObservers() {
        observers.clear();
        LOGGER.info("All observers cleared");
    }
    
    /**
     * Sends a notification about a new donation.
     * 
     * @param donorName The name of the donor
     * @param amount The donation amount
     * @param caseId The case ID (or null if general donation)
     */
    public void notifyNewDonation(String donorName, double amount, Integer caseId) {
        String message;
        if (caseId != null) {
            message = String.format("New donation: %s donated $%.2f to Case #%d", donorName, amount, caseId);
        } else {
            message = String.format("New donation: %s donated $%.2f", donorName, amount);
        }
        notifyObservers(message);
    }
    
    /**
     * Sends a notification about a case status change.
     * 
     * @param caseId The case ID
     * @param newStatus The new status
     */
    public void notifyCaseStatusChange(int caseId, String newStatus) {
        String message = String.format("Case #%d status changed to: %s", caseId, newStatus);
        notifyObservers(message);
    }
    
    /**
     * Sends a notification about a new case submission.
     * 
     * @param caseId The case ID
     * @param caseType The type of case
     */
    public void notifyNewCaseSubmission(int caseId, String caseType) {
        String message = String.format("New charity case submitted: Case #%d (%s)", caseId, caseType);
        notifyObservers(message);
    }
    
    /**
     * Sends a notification about a new user registration.
     * 
     * @param userName The user's name
     * @param userType The type of user
     */
    public void notifyNewUserRegistration(String userName, String userType) {
        String message = String.format("New %s registered: %s", userType.toLowerCase(), userName);
        notifyObservers(message);
    }
}
