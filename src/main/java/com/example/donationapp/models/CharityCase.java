package com.example.donationapp.models;


public class CharityCase
{
    private int caseId;
    private int userId;
    private String title;
    private String type;
    private String description;
    private String category;
    private double goalAmount;
    private double currentAmount;
    private String status;
    private String priorityLevel;
    private static int nextId = 1;
    

    public CharityCase(int userId, String title, String description)
    {
        this.caseId = generateUniqueId();
        this.userId = userId;
        this.title = title;
        this.type = title;
        this.description = description;
        this.category = "Other";
        this.goalAmount = 0.0;
        this.currentAmount = 0.0;
        this.status = "Pending";
        this.priorityLevel = "None";
    }
    

    public CharityCase(int caseId, int userId, String title, String description)
    {
        this.caseId = caseId;
        this.userId = userId;
        this.title = title;
        this.type = title;
        this.description = description;
        this.category = "Other";
        this.goalAmount = 0.0;
        this.currentAmount = 0.0;
        this.status = "Pending";
        this.priorityLevel = "None";

        if (caseId >= nextId) { nextId = caseId + 1; }
    }
    

    public CharityCase(int caseId, int userId, String title, String description, 
                       double goalAmount, double currentAmount, String category,
                       String status, String priorityLevel)
    {
        this.caseId = caseId;
        this.userId = userId;
        this.title = title;
        this.type = title;
        this.description = description;
        this.goalAmount = goalAmount;
        this.currentAmount = currentAmount;
        this.category = category;
        this.status = status;
        this.priorityLevel = priorityLevel;

        if (caseId >= nextId) { nextId = caseId + 1; }
    }
    

    public int getCaseId() {
        return caseId;
    }
    
    private void setCaseId(int id) {
        this.caseId = id;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
        this.type = title;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public double getGoalAmount() {
        return goalAmount;
    }
    
    public void setGoalAmount(double goalAmount) {
        this.goalAmount = goalAmount;
    }
    
    public double getCurrentAmount() {
        return currentAmount;
    }
    
    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public void addDonation(double amount) {
        this.currentAmount += amount;
    }

    public double getProgressPercentage()
    {
        if (goalAmount <= 0) return 0;
        return (currentAmount / goalAmount) * 100;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getPriorityLevel() {
        return priorityLevel;
    }
    
    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    private int generateUniqueId() {
        return nextId++;
    }

    public static void resetIdCounter() {
        nextId = 1;
    }

    public static void setNextId(int id) {
        nextId = id;
    }

    public boolean isPending() {
        return "Pending".equals(status);
    }

    public boolean isAccepted() {
        return "Active".equals(status) || (status != null && status.startsWith("Accepted"));
    }

    public boolean isDeclined() {
        return "Declined".equals(status) || "Rejected".equals(status);
    }

    public boolean isCompleted() {
        return "Completed".equals(status);
    }
    
    @Override
    public String toString() {
        return title != null ? title : "Case #" + caseId;
    }
}
