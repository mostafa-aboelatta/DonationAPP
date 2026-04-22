package com.example.donationapp.models;


public class MedicalDonation extends Donation
{
    
    private int quantity;
    private String medicalEquipment;

    public MedicalDonation(String type, int quantity, String medicalEquipment)
    {
        super(type);
        this.quantity = quantity;
        this.medicalEquipment = medicalEquipment;
    }
    

    public MedicalDonation(int donationId, String type, String date, int quantity, String medicalEquipment)
    {
        super(donationId, type, date);
        this.quantity = quantity;
        this.medicalEquipment = medicalEquipment;
    }
    
    @Override
    public double getAmount()
    {
        // Medical donations don't have a direct monetary amount
        return 0.0;
    }
    

    public String getMedicalEquipment() {
        return medicalEquipment;
    }

    public void setMedicalEquipment(String medicalEquipment) {
        this.medicalEquipment = medicalEquipment;
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
        return "MedicalDonation{" +
                "donationId=" + getDonationId() +
                ", type='" + getType() + '\'' +
                ", date='" + getDate() + '\'' +
                ", quantity=" + quantity +
                ", medicalEquipment='" + medicalEquipment + '\'' +
                '}';
    }
}
