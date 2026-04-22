package com.example.donationapp.models;


public class FinancialDonation extends Donation
{
    
    private double amount;

    public FinancialDonation(String type, double amount)
    {
        super(type);
        this.amount = amount;
    }
    

    public FinancialDonation(int donationId, String type, String date, double amount)
    {
        super(donationId, type, date);
        this.amount = amount;
    }
    
    @Override
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    @Override
    public String toString()
    {
        return "FinancialDonation{" +
                "donationId=" + getDonationId() +
                ", type='" + getType() + '\'' +
                ", date='" + getDate() + '\'' +
                ", amount=" + amount +
                '}';
    }
}
