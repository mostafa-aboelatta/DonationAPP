package com.example.donationapp.models;

import java.time.LocalDate;

public abstract class Donation
{

    private String type;
    private int donationId;
    private String date;
    private int donorId;
    private Integer caseId;
    private static int nextId = 1;


    public Donation(String type)
    {
        this.type = type;
        this.donationId = generateUniqueId();
        this.date = LocalDate.now().toString();
    }


    public Donation(int donationId, String type, String date)
    {
        this.donationId = donationId;
        this.type = type;
        this.date = date;
        if (donationId >= nextId)
        {
            nextId = donationId + 1;
        }
    }


    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getDonationId() {
        return donationId;
    }
    
    private void setDonationId(int id) {
        this.donationId = id;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public int getDonorId() {
        return donorId;
    }
    
    public void setDonorId(int donorId) {
        this.donorId = donorId;
    }
    
    public Integer getCaseId() {
        return caseId;
    }
    
    public void setCaseId(Integer caseId) {
        this.caseId = caseId;
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

    public abstract double getAmount();
    
    @Override
    public String toString()
    {
        return "Donation{" +
                "donationId=" + donationId +
                ", type='" + type + '\'' +
                ", date='" + date + '\'' +
                ", donorId=" + donorId +
                ", caseId=" + caseId +
                '}';
    }
}
