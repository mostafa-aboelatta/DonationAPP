package com.example.donationapp.database;

import com.example.donationapp.exceptions.DatabaseConnectionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;


public class DatabaseManager
{
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    
    private static DatabaseManager instance;
    private Connection connection;

    // Database configuration - modify these values according to your setup
    private static final String DB_URL = "jdbc:mysql://localhost:3306/donation_system";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "mysqldatabase"; // Change this to your MySQL password
    
    // For creating database if it doesn't exist
    private static final String DB_URL_BASE = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "donation_system";
    

    private DatabaseManager()
    {
        try
        {
            initializeDatabase();
        }
        catch (DatabaseConnectionException e)
        {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
        }
    }
    

    //Gets the singleton instance of DatabaseManager.
    public static DatabaseManager getInstance()
    {
        if (instance == null)
        {
            synchronized (DatabaseManager.class)
            {
                if (instance == null)
                {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Gets the database connection.
     * Creates a new connection if the current one is closed.
     * @return The database connection
     * @throws DatabaseConnectionException if connection cannot be established
     */
    public Connection getConnection() throws DatabaseConnectionException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
            return connection;
        } catch (SQLException e) {
            throw new DatabaseConnectionException("Failed to establish database connection: " + e.getMessage(), e);
        }
    }
    

     // Initializes the database by creating it if it doesn't exist and setting up tables.
     // @throws DatabaseConnectionException if initialization fails

    private void initializeDatabase() throws DatabaseConnectionException {
        try
        {
            // First, try to create the database if it doesn't exist
            try (Connection baseConn = DriverManager.getConnection(DB_URL_BASE, DB_USER, DB_PASSWORD);
                 Statement stmt = baseConn.createStatement())
            {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }
            
            // Now connect to the database and create tables
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            createTables();
            
            LOGGER.info("Database initialized successfully");
        }
        catch (SQLException e)
        {
            throw new DatabaseConnectionException("Failed to initialize database: " + e.getMessage(), e);
        }
    }
    

//      Creates all required database tables.
//      @throws SQLException if table creation fails

    private void createTables() throws SQLException
    {
        try (Statement stmt = connection.createStatement())
        {
            
            // Users Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    phone VARCHAR(20) NOT NULL,
                    password VARCHAR(255) NOT NULL,
                    user_type ENUM('DONOR', 'RECEIVER', 'VOLUNTEER', 'ADMIN') NOT NULL,
                    location VARCHAR(100),
                    is_verified BOOLEAN DEFAULT FALSE,
                    role VARCHAR(50),
                    working_hours INT DEFAULT 0,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);
            
            // Charity Cases Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS charity_cases (
                    case_id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    type VARCHAR(50) NOT NULL,
                    description TEXT NOT NULL,
                    status VARCHAR(50) DEFAULT 'Pending',
                    priority_level VARCHAR(20) DEFAULT 'None',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    reviewed_at TIMESTAMP NULL,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);
            
            // Donations Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS donations (
                    donation_id INT PRIMARY KEY AUTO_INCREMENT,
                    donor_id INT NOT NULL,
                    case_id INT,
                    donation_type VARCHAR(20) NOT NULL,
                    amount DOUBLE DEFAULT 0.0,
                    quantity INT DEFAULT 0,
                    medical_equipment VARCHAR(100),
                    category VARCHAR(50),
                    date DATE NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (case_id) REFERENCES charity_cases(case_id) ON DELETE SET NULL
                )
            """);
            
            // Notifications Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS notifications (
                    notification_id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    subject VARCHAR(200) NOT NULL,
                    message TEXT NOT NULL,
                    is_read BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);
            
            // Verification Codes Table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS verification_codes (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    code VARCHAR(20) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    expires_at TIMESTAMP NOT NULL,
                    is_used BOOLEAN DEFAULT FALSE,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                )
            """);
            
            LOGGER.info("Database tables created successfully");
        }
    }
    

    //Closes the database connection.

    public void closeConnection()
    {
        try
        {
            if (connection != null && !connection.isClosed())
            {
                connection.close();
                LOGGER.info("Database connection closed");
            }
        }
        catch (SQLException e)
        {
            LOGGER.log(Level.WARNING, "Error closing database connection", e);
        }
    }
    
    //test Database connection
    public boolean testConnection()
    {
        try
        {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed();
        }
        catch (DatabaseConnectionException | SQLException e)
        {
            LOGGER.log(Level.WARNING, "Database connection test failed", e);
            return false;
        }
    }
}
