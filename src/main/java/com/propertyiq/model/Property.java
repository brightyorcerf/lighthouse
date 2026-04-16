package com.propertyiq.model;

/**
 * MODEL — Property
 * Represents a real-estate property record stored in the database.
 */
public class Property {

    public enum RiskLevel { LOW, MEDIUM, HIGH }

    private int        propertyId;
    private String     propertyName;
    private String     location;
    private double     purchasePrice;
    private double     rentalIncome;   // monthly
    private double     expenses;       // monthly
    private int        locationRating; // 1–10
    private RiskLevel  riskLevel;
    private int        createdBy;      // user_id FK

    // ── Constructors ──────────────────────────────────────────────────────────

    public Property() {}

    public Property(String propertyName, String location, double purchasePrice,
                    double rentalIncome, double expenses,
                    int locationRating, RiskLevel riskLevel, int createdBy) {
        this.propertyName   = propertyName;
        this.location       = location;
        this.purchasePrice  = purchasePrice;
        this.rentalIncome   = rentalIncome;
        this.expenses       = expenses;
        this.locationRating = locationRating;
        this.riskLevel      = riskLevel;
        this.createdBy      = createdBy;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int    getPropertyId()              { return propertyId; }
    public void   setPropertyId(int id)        { this.propertyId = id; }

    public String getPropertyName()            { return propertyName; }
    public void   setPropertyName(String n)    { this.propertyName = n; }

    public String getLocation()                { return location; }
    public void   setLocation(String l)        { this.location = l; }

    public double getPurchasePrice()           { return purchasePrice; }
    public void   setPurchasePrice(double p)   { this.purchasePrice = p; }

    public double getRentalIncome()            { return rentalIncome; }
    public void   setRentalIncome(double r)    { this.rentalIncome = r; }

    public double getExpenses()                { return expenses; }
    public void   setExpenses(double e)        { this.expenses = e; }

    public int    getLocationRating()          { return locationRating; }
    public void   setLocationRating(int r)     { this.locationRating = r; }

    public RiskLevel getRiskLevel()            { return riskLevel; }
    public void      setRiskLevel(RiskLevel r) { this.riskLevel = r; }

    public int  getCreatedBy()             { return createdBy; }
    public void setCreatedBy(int uid)      { this.createdBy = uid; }

    /** Convenience: monthly net profit */
    public double getMonthlyProfit() { return rentalIncome - expenses; }

    /** Convenience: annual net profit */
    public double getAnnualProfit() { return getMonthlyProfit() * 12; }

    @Override
    public String toString() {
        return "Property{id=" + propertyId + ", name='" + propertyName
             + "', location='" + location + "', price=" + purchasePrice + "}";
    }
}