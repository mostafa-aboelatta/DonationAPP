package com.example.donationapp.models;


public class OtherDonation extends Donation
{
    private int quantity;
    private double amount;
    private String category;
    

    public OtherDonation(String type, int quantity, double amount, String category)
    {
        super(type);
        this.quantity = quantity;
        this.amount = amount;
        this.category = category;
    }
    

    public OtherDonation(int donationId, String type, String date, int quantity, double amount, String category)
    {
        super(donationId, type, date);
        this.quantity = quantity;
        this.amount = amount;
        this.category = category;
    }
    
    @Override
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public String toString()
    {
        return "OtherDonation{" +
                "donationId=" + getDonationId() +
                ", type='" + getType() + '\'' +
                ", date='" + getDate() + '\'' +
                ", quantity=" + quantity +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                '}';
    }
}
