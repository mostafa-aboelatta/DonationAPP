module com.example.donationapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires java.mail;
    requires jbcrypt;
    requires java.logging;

    opens com.example.donationapp to javafx.fxml;
    opens com.example.donationapp.models to javafx.base;
    opens com.example.donationapp.controllers to javafx.fxml;
    
    exports com.example.donationapp;
    exports com.example.donationapp.models;
    exports com.example.donationapp.interfaces;
    exports com.example.donationapp.exceptions;
    exports com.example.donationapp.dao;
    exports com.example.donationapp.database;
    exports com.example.donationapp.factory;
    exports com.example.donationapp.util;
    exports com.example.donationapp.controllers;
    exports com.example.donationapp.email;
}