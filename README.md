# 💝 Donation Management System

A comprehensive JavaFX desktop application for managing charitable donations, connecting donors with receivers, and facilitating the donation process through an intuitive graphical interface.

![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.6-blue?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)
![Maven](https://img.shields.io/badge/Maven-Build-red?style=flat-square&logo=apache-maven)

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Technologies Used](#-technologies-used)
- [Design Patterns](#-design-patterns)
- [Project Structure](#-project-structure)
- [Installation & Setup](#-installation--setup)
- [Email Configuration](#-email-configuration)
- [How to Run](#-how-to-run)
- [User Roles](#-user-roles)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Overview

The **Donation Management System** is a full-featured desktop application designed to streamline the process of charitable giving. It provides a platform where:

- **Donors** can browse verified charity cases and make financial, medical, or other donations
- **Receivers** can submit charity cases requesting assistance
- **Volunteers** can assist with case verification and donation distribution
- **Administrators** can manage users, review cases, and oversee the entire system

The application features a modern JavaFX GUI, secure MySQL database integration, and an automated email notification system to keep all parties informed throughout the donation process.

---

## ✨ Features

### Core Functionality
| Feature | Description |
|---------|-------------|
| ✅ User Authentication | Secure login/signup with BCrypt password hashing |
| ✅ Role-Based Access | Different dashboards for Donors, Receivers, Volunteers, Admins |
| ✅ Charity Case Management | Submit, review, approve/decline cases |
| ✅ Multi-Type Donations | Support for Financial, Medical, and Other donation types |
| ✅ Real-Time Notifications | In-app notification system using Observer pattern |

### 📧 Email Notification System
| Email Type | Trigger |
|------------|---------|
| Welcome Email | New user registration |
| Donation Confirmation | After successful donation |
| Case Submission | When receiver submits a case |
| Case Approval | When admin approves a case |
| Case Decline | When admin declines a case (with reason) |

**Email Features:**
- Async sending (non-blocking UI)
- Beautiful HTML templates
- Retry mechanism with exponential backoff
- Gmail SMTP integration

### Security Features
- 🔐 BCrypt password hashing
- 🔐 Gmail App Password authentication
- 🔐 TLS/STARTTLS encryption for emails
- 🔐 Prepared statements (SQL injection prevention)

---

## 🛠 Technologies Used

### Core Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 25 | Primary programming language |
| **JavaFX** | 21.0.6 | GUI framework |
| **MySQL** | 8.0 | Relational database |
| **Maven** | 3.x | Build automation & dependency management |

### Libraries & Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| `mysql-connector-java` | 8.0.33 | MySQL JDBC driver |
| `jbcrypt` | 0.4 | Password hashing |
| `javax.mail` | 1.6.2 | Email functionality (SMTP) |
| `javafx-controls` | 21.0.6 | JavaFX UI controls |
| `javafx-fxml` | 21.0.6 | FXML support for UI |

### Email Technologies

| Technology | Purpose |
|------------|---------|
| JavaMail API | SMTP email sending |
| Gmail SMTP | Email delivery service |
| CompletableFuture | Asynchronous operations |
| ExecutorService | Thread pool management |

---

## 🏗 Design Patterns

### 1. Singleton Pattern
Ensures single instance for shared resources:
- `DatabaseManager` - Database connection management
- `NotificationManager` - In-app notifications
- `EmailConfig` - Email configuration
- `EmailService` - Email operations

### 2. Factory Pattern
Creates objects without specifying exact class:
- `DonationFactory` - Creates FinancialDonation, MedicalDonation, or OtherDonation

### 3. Observer Pattern
Notification system for real-time updates:
- `NotificationManager` (Subject) notifies `Controllers` (Observers)

### 4. DAO Pattern
Separates data access logic:
- `UserDAO` - User CRUD operations
- `DonationDAO` - Donation CRUD operations
- `CharityCaseDAO` - Case CRUD operations

### 5. MVC Pattern
Separates concerns:
- **Models** - `User`, `Donation`, `CharityCase`
- **Views** - FXML files
- **Controllers** - Handle user interactions

---

## 📁 Project Structure

```
DonationAPP/
├── pom.xml                          # Maven configuration
├── README.md                        # This file
├── EMAIL_IMPLEMENTATION_GUIDE.md    # Email setup guide
├── database_schema.sql              # Database script
│
├── src/main/java/
│   ├── module-info.java
│   └── com/example/donationapp/
│       ├── HelloApplication.java    # Main app
│       ├── Launcher.java            # Entry point
│       │
│       ├── controllers/             # UI Controllers
│       │   ├── LoginController.java
│       │   ├── SignupController.java
│       │   ├── AdminController.java
│       │   ├── DonorController.java
│       │   ├── ReceiverController.java
│       │   └── VolunteerController.java
│       │
│       ├── models/                  # Data Models
│       │   ├── User.java (abstract)
│       │   ├── Admin.java
│       │   ├── Donor.java
│       │   ├── Receiver.java
│       │   ├── Volunteer.java
│       │   ├── Donation.java (abstract)
│       │   ├── FinancialDonation.java
│       │   ├── MedicalDonation.java
│       │   ├── OtherDonation.java
│       │   └── CharityCase.java
│       │
│       ├── dao/                     # Data Access Objects
│       │   ├── UserDAO.java
│       │   ├── DonationDAO.java
│       │   └── CharityCaseDAO.java
│       │
│       ├── database/
│       │   └── DatabaseManager.java # Singleton
│       │
│       ├── factory/
│       │   └── DonationFactory.java
│       │
│       ├── interfaces/
│       │   ├── Loginable.java
│       │   ├── Notifiable.java
│       │   └── Observer.java
│       │
│       ├── exceptions/              # Custom Exceptions
│       │   ├── DatabaseConnectionException.java
│       │   ├── DuplicateUserException.java
│       │   ├── InvalidCredentialsException.java
│       │   ├── UserNotFoundException.java
│       │   ├── CaseNotFoundException.java
│       │   ├── InvalidDonationException.java
│       │   └── UnverifiedDonorException.java
│       │
│       ├── util/
│       │   ├── NotificationManager.java
│       │   └── UtilitySupport.java
│       │
│       └── email/                   # Email System
│           ├── EmailConfig.java
│           ├── EmailService.java
│           ├── EmailTemplates.java
│           └── EmailTest.java
│
└── src/main/resources/
    ├── email.properties             # Email config
    └── com/example/donationapp/    # FXML Views
        ├── Login.fxml
        ├── Signup.fxml
        ├── admin.fxml
        ├── Donor.fxml
        ├── reciever.fxml
        └── volunteer.fxml
```

---

## ⚙ Installation & Setup

### Prerequisites

- Java JDK 21+ 
- MySQL 8.0+
- Maven 3.6+

### Step 1: Database Setup

```sql
CREATE DATABASE donation_system;
```

Then run `database_schema.sql` or let the app create tables automatically.

### Step 2: Configure Database

Edit `DatabaseManager.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/donation_system";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

### Step 3: Install Dependencies

```bash
mvn clean install
```

---

## 📧 Email Configuration

### Get Gmail App Password

1. Enable 2-Step Verification at [Google Security](https://myaccount.google.com/security)
2. Generate App Password at [App Passwords](https://myaccount.google.com/apppasswords)
3. Select "Mail" → Generate → Copy 16-character code

### Configure email.properties

```properties
email.enabled=true
email.smtp.host=smtp.gmail.com
email.smtp.port=587
email.username=your-email@gmail.com
email.password=your-16-char-app-password
email.from.address=your-email@gmail.com
email.from.name=Donation Management System
```

> ⚠️ Add `email.properties` to `.gitignore`!

---

## 🚀 How to Run

### Using Maven (Recommended)
```bash
mvn clean javafx:run
```

### Using Maven Wrapper
```bash
./mvnw javafx:run        # Linux/Mac
.\mvnw.cmd javafx:run    # Windows
```

### Default Login
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@donation.com | admin123 |

---

## 👥 User Roles

### 🔑 Admin
- Manage all users
- Review and approve/decline charity cases
- View system statistics
- Full system access

### 💰 Donor
- Browse verified charity cases
- Make donations (Financial, Medical, Other)
- View donation history
- Receive confirmation emails

### 🙏 Receiver
- Submit charity cases
- Track case status
- View received donations
- Receive status notification emails

### 🤝 Volunteer
- Assist with case verification
- Help with donation distribution
- Track working hours

---

## 🔧 Troubleshooting

| Issue | Solution |
|-------|----------|
| Database connection failed | Check MySQL is running, verify credentials |
| Email not sending | Verify App Password (not regular password) |
| JavaFX not loading | Use `mvn javafx:run` instead of direct run |
| Module not found | Run `mvn clean install` |
| FXML load error | Check file names match exactly (case-sensitive) |

---

## 📚 Additional Documentation

- [EMAIL_IMPLEMENTATION_GUIDE.md](EMAIL_IMPLEMENTATION_GUIDE.md) - Detailed email setup
- [PROJECT_DOCUMENTATION.md](PROJECT_DOCUMENTATION.md) - Complete project docs

---

## 🎓 Academic Information

**Course:** Java Programming - Term 5  
**Semester:** Fall 2025  
**Project Type:** Desktop Application

---

<div align="center">

**Made with ❤️ for charitable giving**

*Donation Management System - Making a difference, one donation at a time*

</div>
