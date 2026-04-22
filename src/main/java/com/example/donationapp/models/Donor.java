package com.example.donationapp.models;

import com.example.donationapp.interfaces.Loginable;
import com.example.donationapp.interfaces.Notifiable;
import com.example.donationapp.util.UtilitySupport;

import java.util.ArrayList;

public class Donor extends User implements Loginable, Notifiable
{
    
    private ArrayList<Donation> donations;
    private boolean isVerified;

    public Donor(String name, String email, String phone)
    {
        super(name, email, phone);
        this.donations = new ArrayList<>();
        this.isVerified = false;
    }

    public Donor(int id, String name, String email, String phone, boolean isVerified)
    {
        super(id, name, email, phone);
        this.donations = new ArrayList<>();
        this.isVerified = isVerified;
    }

    public ArrayList<Donation> getDonations() {
        return donations;
    }

    public void addDonation(Donation donation) {
        donations.add(donation);
    }

    public int getDonationCount() {
        return donations.size();
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean status) {
        this.isVerified = status;
    }

    public double getTotalDonationAmount()
    {
        double total = 0;
        for (Donation donation : donations)
        {
            total += donation.getAmount();
        }
        return total;
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
        return "Donor{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", isVerified=" + isVerified +
                ", donationCount=" + getDonationCount() +
                '}';
    }
}
