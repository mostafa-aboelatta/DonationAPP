package com.example.donationapp.models;

import com.example.donationapp.interfaces.Loginable;
import com.example.donationapp.interfaces.Notifiable;
import com.example.donationapp.util.UtilitySupport;

public class Volunteer extends User implements Loginable, Notifiable
{
    
    private String role;
    private int workingHours;
    private String location;

    public Volunteer(String name, String email, String phone, String location)
    {
        super(name, email, phone);
        this.workingHours = 0;
        this.location = location;
        this.role = "Unassigned";
    }
    

    public Volunteer(int id, String name, String email, String phone, String location, String role, int workingHours)
    {
        super(id, name, email, phone);
        this.location = location;
        this.role = role != null ? role : "Unassigned";
        this.workingHours = workingHours;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public int getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(int hours) {
        this.workingHours = hours;
    }

    public void addWorkingHours(int hours) {
        this.workingHours += hours;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    @Override
    public boolean login(String email, String password)
    {
        // Authentication is handled by the database layer
        return this.getEmail().equals(email);
    }
    
    @Override
    public void sendNotification(String receiverEmail, String subject, String message)
    {
        UtilitySupport.sendGeneralEmail(receiverEmail, subject, message);
    }
    
    @Override
    public String toString()
    {
        return "Volunteer{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", location='" + location + '\'' +
                ", role='" + role + '\'' +
                ", workingHours=" + workingHours +
                '}';
    }
}
