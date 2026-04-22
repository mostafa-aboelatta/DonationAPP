package com.example.donationapp.models;

public abstract class User
{
    
    private String name;
    private int id;
    private String email;
    private String phone;
    private String password;
    private String userType;
    private boolean verified;
    private static int nextId = 1;

    public User(String name, String email, String phone)
    {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.id = generateUniqueId();
    }

    public User(int id, String name, String email, String phone)
    {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;

        if (id >= nextId) { nextId = id + 1; }
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getId() {
        return id;
    }
    
    private void setId(int id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getUserType() {
        return userType;
    }
    
    public void setUserType(String userType) {
        this.userType = userType;
    }
    
    public boolean isVerified() {
        return verified;
    }
    
    public void setVerified(boolean verified) {
        this.verified = verified;
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
    
    @Override
    public String toString()
    {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
