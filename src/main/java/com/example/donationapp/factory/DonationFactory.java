package com.example.donationapp.factory;

import com.example.donationapp.exceptions.InvalidDonationException;
import com.example.donationapp.models.*;

import java.util.Map;

public class DonationFactory
{

    public static Donation createDonation(String donationType, Map<String, Object> params) 
            throws InvalidDonationException
    {
        
        if (donationType == null || donationType.trim().isEmpty())
        {
            throw new InvalidDonationException("Donation type cannot be null or empty");
        }
        
        switch (donationType.toLowerCase())
        {
            case "financial":
                return createFinancialDonation(params);
            case "medical":
                return createMedicalDonation(params);
            case "other":
                return createOtherDonation(params);
            default:
                throw new InvalidDonationException("Invalid donation type: " + donationType);
        }
    }
    

    private static FinancialDonation createFinancialDonation(Map<String, Object> params) 
            throws InvalidDonationException
    {
        
        if (!params.containsKey("amount"))
        {
            throw new InvalidDonationException("Financial donation requires 'amount' parameter");
        }
        
        double amount;
        try
        {
            Object amountObj = params.get("amount");
            if (amountObj instanceof Double)
            {
                amount = (Double) amountObj;
            }
            else if (amountObj instanceof Integer)
            {
                amount = ((Integer) amountObj).doubleValue();
            }
            else if (amountObj instanceof String)
            {
                amount = Double.parseDouble((String) amountObj);
            }
            else
            {
                throw new InvalidDonationException("Invalid amount format");
            }
        }
        catch (NumberFormatException e)
        {
            throw new InvalidDonationException("Invalid amount format: " + e.getMessage());
        }
        
        if (amount <= 0)
        {
            throw new InvalidDonationException("Donation amount must be greater than 0");
        }
        
        String description = params.containsKey("description") ? 
                            (String) params.get("description") : "Financial";
        
        return new FinancialDonation(description, amount);
    }
    

    private static MedicalDonation createMedicalDonation(Map<String, Object> params) 
            throws InvalidDonationException
    {
        
        if (!params.containsKey("quantity") || !params.containsKey("equipment"))
        {
            throw new InvalidDonationException("Medical donation requires 'quantity' and 'equipment' parameters");
        }
        
        int quantity;
        try
        {
            Object quantityObj = params.get("quantity");
            if (quantityObj instanceof Integer)
            {
                quantity = (Integer) quantityObj;
            }
            else if (quantityObj instanceof String)
            {
                quantity = Integer.parseInt((String) quantityObj);
            }
            else
            {
                throw new InvalidDonationException("Invalid quantity format");
            }
        }
        catch (NumberFormatException e)
        {
            throw new InvalidDonationException("Invalid quantity format: " + e.getMessage());
        }
        
        if (quantity <= 0)
        {
            throw new InvalidDonationException("Quantity must be greater than 0");
        }
        
        String equipment = (String) params.get("equipment");
        if (equipment == null || equipment.trim().isEmpty())
        {
            throw new InvalidDonationException("Equipment description cannot be empty");
        }
        
        String type = params.containsKey("type") ? (String) params.get("type") : "Medical";
        
        return new MedicalDonation(type, quantity, equipment);
    }
    

    private static OtherDonation createOtherDonation(Map<String, Object> params) 
            throws InvalidDonationException
    {
        
        if (!params.containsKey("category"))
        {
            throw new InvalidDonationException("Other donation requires 'category' parameter");
        }
        
        int quantity = 1;
        if (params.containsKey("quantity"))
        {
            try
            {
                Object quantityObj = params.get("quantity");
                if (quantityObj instanceof Integer)
                {
                    quantity = (Integer) quantityObj;
                }
                else if (quantityObj instanceof String)
                {
                    quantity = Integer.parseInt((String) quantityObj);
                }
            }
            catch (NumberFormatException e)
            {
                throw new InvalidDonationException("Invalid quantity format: " + e.getMessage());
            }
        }
        
        double amount = 0.0;
        if (params.containsKey("amount"))
        {
            try
            {
                Object amountObj = params.get("amount");
                if (amountObj instanceof Double)
                {
                    amount = (Double) amountObj;
                }
                else if (amountObj instanceof Integer)
                {
                    amount = ((Integer) amountObj).doubleValue();
                }
                else if (amountObj instanceof String)
                {
                    amount = Double.parseDouble((String) amountObj);
                }
            }
            catch (NumberFormatException e)
            {
                throw new InvalidDonationException("Invalid amount format: " + e.getMessage());
            }
        }
        
        String category = (String) params.get("category");
        if (category == null || category.trim().isEmpty())
        {
            throw new InvalidDonationException("Category cannot be empty");
        }
        
        String type = params.containsKey("type") ? (String) params.get("type") : "Other";
        
        return new OtherDonation(type, quantity, amount, category);
    }
    

    //Overloaded method - creates a financial donation directly.
    public static FinancialDonation createFinancialDonation(double amount) throws InvalidDonationException
    {
        if (amount <= 0)
        {
            throw new InvalidDonationException("Donation amount must be greater than 0");
        }
        return new FinancialDonation("Financial", amount);
    }
    

    //Overloaded method - creates a financial donation with description.
    public static FinancialDonation createFinancialDonation(double amount, String description) 
            throws InvalidDonationException
    {
        if (amount <= 0)
        {
            throw new InvalidDonationException("Donation amount must be greater than 0");
        }
        return new FinancialDonation(description, amount);
    }

    //Overloaded method - creates a medical donation directly.
    public static MedicalDonation createMedicalDonation(int quantity, String equipment) 
            throws InvalidDonationException
    {
        if (quantity <= 0)
        {
            throw new InvalidDonationException("Quantity must be greater than 0");
        }
        if (equipment == null || equipment.trim().isEmpty())
        {
            throw new InvalidDonationException("Equipment description cannot be empty");
        }
        return new MedicalDonation("Medical", quantity, equipment);
    }
    

    //Overloaded method - creates an other donation directly.
    public static OtherDonation createOtherDonation(int quantity, double amount, String category) 
            throws InvalidDonationException
    {
        if (category == null || category.trim().isEmpty())
        {
            throw new InvalidDonationException("Category cannot be empty");
        }
        return new OtherDonation("Other", quantity, amount, category);
    }
}
