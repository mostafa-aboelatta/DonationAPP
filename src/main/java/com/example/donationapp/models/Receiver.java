package com.example.donationapp.models;

import com.example.donationapp.interfaces.Loginable;
import com.example.donationapp.interfaces.Notifiable;
import com.example.donationapp.util.UtilitySupport;

import java.util.ArrayList;

public class Receiver extends User implements Loginable, Notifiable
{
    private ArrayList<CharityCase> cases;

    public Receiver(String name, String email, String phone)
    {
        super(name, email, phone);
        this.cases = new ArrayList<>();
    }

    public Receiver(int id, String name, String email, String phone)
    {
        super(id, name, email, phone);
        this.cases = new ArrayList<>();
    }

    public void submitCase(CharityCase charityCase) {
        cases.add(charityCase);
    }

    public ArrayList<CharityCase> getCases() {
        return cases;
    }

    public String checkCaseStatus(int caseId)
    {
        for (CharityCase c : cases)
        {
            if (c.getCaseId() == caseId)
            {
                return c.getStatus();
            }
        }
        return "Case not found";
    }

    public int getCaseCount() {
        return cases.size();
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
        return "Receiver{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", phone='" + getPhone() + '\'' +
                ", caseCount=" + getCaseCount() +
                '}';
    }
}
