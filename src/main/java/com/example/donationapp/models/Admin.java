package com.example.donationapp.models;

import com.example.donationapp.interfaces.Loginable;
import com.example.donationapp.interfaces.Notifiable;
import com.example.donationapp.util.UtilitySupport;

import java.util.ArrayList;

public class Admin extends User implements Loginable, Notifiable
{
    
    private ArrayList<CharityCase> acceptedCases_HP;
    private ArrayList<CharityCase> acceptedCases_LP;
    private String location;

    public Admin(String name, String email, String phone, String location)
    {
        super(name, email, phone);
        this.acceptedCases_HP = new ArrayList<>();
        this.acceptedCases_LP = new ArrayList<>();
        this.location = location;
    }

    public Admin(int id, String name, String email, String phone, String location)
    {
        super(id, name, email, phone);
        this.acceptedCases_HP = new ArrayList<>();
        this.acceptedCases_LP = new ArrayList<>();
        this.location = location;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean reviewCase(CharityCase charityCase)
    {
        // This method is typically overridden with business logic
        // Returns true by default to indicate review completion
        return true;
    }
    

    public void assignHighPriority(CharityCase charityCase)
    {
        acceptedCases_HP.add(charityCase);
        charityCase.setStatus("Accepted - High Priority");
    }

    public void assignLowPriority(CharityCase charityCase)
    {
        acceptedCases_LP.add(charityCase);
        charityCase.setStatus("Accepted - Low Priority");
    }

    public ArrayList<CharityCase> getAcceptedHighPriorityCases() {
        return acceptedCases_HP;
    }

    public ArrayList<CharityCase> getAcceptedLowPriorityCases() {
        return acceptedCases_LP;
    }

    public int getTotalAcceptedCases() {
        return acceptedCases_HP.size() + acceptedCases_LP.size();
    }
    
    @Override
    public boolean login(String email, String password)
    {
        // Authentication is handled by the database layer
        return this.getEmail().equals(email);
    }
    
    @Override
    public void sendNotification(String receiverEmail, String subject, String message) {
        UtilitySupport.sendGeneralEmail(receiverEmail, subject, message);
    }
    
    @Override
    public String toString()
    {
        return "Admin{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", location='" + location + '\'' +
                ", highPriorityCases=" + acceptedCases_HP.size() +
                ", lowPriorityCases=" + acceptedCases_LP.size() +
                '}';
    }
}
