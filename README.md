# Lighthouse (Investment Analysis System)

(saathvika's homework)

A Java Swing desktop application for property investment management and analysis.

> Soft aesthetic · 4-Tier Layered Architecture · BCrypt security · JFreeChart visualisations.

![img.jpg](img.jpg)

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│  TIER 1 — PRESENTATION (com.lighthouse.presentation)            │
│                                                                 │
│   LoginScreen → DashboardScreen (CardLayout shell)              │
│     ├── PropertyListScreen   (table, edit/delete)               │
│     ├── PropertyFormScreen   (add / edit form)                  │
│     ├── AnalysisScreen       (metric cards + score bars)        │
│     ├── GraphScreen          (4 × JFreeChart pastel charts)     │
│     └── SearchScreen         (location + price range filter)    │
│                                                                 │
│   theme/SoftTheme.java     (all colours, fonts, factories)      │
└──────────────────────────┬──────────────────────────────────────┘
                           │ calls
┌──────────────────────────▼──────────────────────────────────────┐
│  TIER 2 — SERVICE (com.lighthouse.service)                      │
│                                                                 │
│   AuthService          — login/logout, BCrypt password hashing  │
│   PropertyService      — validation, CRUD, auto-analysis on save│
│   InvestmentAnalysis   — ROI, annual yield, investment score,   │
│   Service                recommendation classification          │
└──────────────────────────┬──────────────────────────────────────┘
                           │ calls
┌──────────────────────────▼──────────────────────────────────────┐
│  TIER 3 — DAO (com.lighthouse.dao)                              │
│                                                                 │
│   UserDAO        — CRUD on users table                          │
│   PropertyDAO    — CRUD + search on properties table            │
│   AnalysisDAO    — upsert/fetch on analysis table               │
└──────────────────────────┬──────────────────────────────────────┘
                           │ calls
┌──────────────────────────▼──────────────────────────────────────┐
│  TIER 4 — DATABASE (com.lighthouse.database)                    │
│                                                                 │
│   DatabaseConnection   — Singleton JDBC connection manager      │
│   MySQL 8.x            — users / properties / analysis tables   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Investment Algorithms

### ROI Formula
```
Monthly Profit  = Rental Income (monthly) − Expenses (monthly)
Annual Income   = Monthly Profit × 12
ROI             = (Annual Income / Purchase Price) × 100    [%]
Annual Yield    = ROI  (same metric, reported separately)
```

### Investment Score (0–100 composite)
```
Score = (ROI × 4.0) + (Location Rating × 3.0) - (Risk × 2.0)
Normalized to 0 - 100.
```

### Example Property Data
To test the evaluation parameters, here are 5 realistic properties to input.

| Property Name               | Location      | Price (RM) | Rent/mo (RM) | Cost/mo (RM) | Profile               |
|-----------------------------|---------------|------------|--------------|--------------|-----------------------|
| Prime Studio Suite          | Kuala Lumpur  | 450,000    | 2,500        | 350          | High profit, low risk |
| Bayview Family Condo        | Penang        | 650,000    | 3,800        | 500          | Excellent yields      |
| Suburban Corner Lot         | Johor Bahru   | 550,000    | 2,000        | 250          | Moderate / Stable     |
| Heritage Walk-Up            | Melaka        | 300,000    | 1,800        | 200          | Low Cost, Med ROI     |
| University Tech Duplex      | Cyberjaya     | 400,000    | 2,300        | 300          | High Demand           |

### Recommendation Thresholds
| Score    | Classification |
|----------|----------------|
| ≥ 65     | High Profit    |
| 40 – 64  | Moderate       |
| < 40     | Risky          |

---

## Project Structure

```
PropertyInvestmentSystem/
├── pom.xml
├── database_schema.sql
└── src/main/java/com/lighthouse/
    ├── Main.java
    ├── database/
    │   └── DatabaseConnection.java          (Singleton)
    ├── model/
    │   ├── User.java
    │   ├── Property.java
    │   └── AnalysisResult.java
    ├── dao/
    │   ├── UserDAO.java
    │   ├── PropertyDAO.java
    │   └── AnalysisDAO.java
    ├── service/
    │   ├── AuthService.java                 (BCrypt)
    │   ├── PropertyService.java
    │   └── InvestmentAnalysisService.java
    └── presentation/
        ├── theme/
        │   └── SoftTheme.java             (design system)
        └── screens/
            ├── LoginScreen.java
            ├── DashboardScreen.java
            ├── PropertyListScreen.java
            ├── PropertyFormScreen.java
            ├── AnalysisScreen.java
            ├── GraphScreen.java
            └── SearchScreen.java
```

---

## Setup & Installation

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- NetBeans IDE (recommended) or IntelliJ IDEA

### Step 1 — Database
```sql
-- In MySQL Workbench or CLI:
SOURCE /path/to/database_schema.sql;
```

### Step 2 — Configure DB credentials
Edit `DatabaseConnection.java`:
```java
private static final String PASSWORD = "your_mysql_password";
private static final String USERNAME = "root"; // or your MySQL user
```

### Step 3 — Build
```bash
mvn clean package
```

### Step 4 — Run
```bash
java -jar target/lighthouse-1.0.0-jar-with-dependencies.jar
```

Or open the project in NetBeans → Run Project.

---

## Default Login Credentials
| Username  | Password   | Role     |
|-----------|------------|----------|
| admin     | admin123   | Admin    |
| investor1 | admin123   | Investor |

> Passwords are stored as BCrypt hashes (work factor 12).  
> Plain-text passwords are **never** stored or logged.

---

## Dependencies
| Library           | Version | Purpose                        |
|-------------------|---------|--------------------------------|
| mysql-connector-j | 8.0.33  | JDBC database connectivity     |
| FlatLaf           | 3.4.1   | Modern flat Look & Feel        |
| JFreeChart        | 1.5.4   | Investment performance charts  |
| jBCrypt           | 0.4     | Secure password hashing        |

---

## Data Privacy (BCrypt Explanation for Examiner)

BCrypt is an adaptive password hashing algorithm:

1. **Salt**: A unique random salt is generated per password. Two identical passwords produce completely different hashes.
2. **Work factor**: Cost factor of 12 means 2¹² = 4096 iterations. Makes brute-force attacks computationally expensive.
3. **One-way**: There is no known way to reverse a BCrypt hash to the original password.
4. **Verification**: `BCrypt.checkpw(inputPassword, storedHash)` — the plain-text password is tested against the stored hash without decryption.

Even if the entire database is leaked, attacker cannot recover user passwords.

---

## User Roles
| Feature              | Admin | Investor |
|----------------------|-------|----------|
| View properties      | ✅    | ✅       |
| Add property         | ✅    | ❌       |
| Edit property        | ✅    | ❌       |
| Delete property      | ✅    | ❌       |
| View analysis        | ✅    | ✅       |
| View graphs          | ✅    | ✅       |
| Search properties    | ✅    | ✅       |