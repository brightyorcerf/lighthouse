package com.lighthouse.model;

/**
 * MODEL — Property
 * Represents a real-estate property record stored in the database.
 */
public class Property {

    public enum RiskLevel { LOW, MEDIUM, HIGH }

    private int        propertyId;
    private String     propertyName;
    private int        locationId;     // FK
    private Location   locationObj;    // from JOIN
    private double     price;
    private double     rent;           // monthly
    private double     cost;           // monthly
    private int        createdBy;      // user_id FK

    // ── Constructors ──────────────────────────────────────────────────────────

    public Property() {}

    public Property(String propertyName, int locationId, double price,
                    double rent, double cost, int createdBy) {
        this.propertyName   = propertyName;
        this.locationId     = locationId;
        this.price          = price;
        this.rent           = rent;
        this.cost           = cost;
        this.createdBy      = createdBy;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int    getPropertyId()              { return propertyId; }
    public void   setPropertyId(int id)        { this.propertyId = id; }

    public String getPropertyName()            { return propertyName; }
    public void   setPropertyName(String n)    { this.propertyName = n; }

    public int    getLocationId()              { return locationId; }
    public void   setLocationId(int l)         { this.locationId = l; }

    public Location getLocationObj()             { return locationObj; }
    public void   setLocationObj(Location loc)   { this.locationObj = loc; }

    public double getPrice()                   { return price; }
    public void   setPrice(double p)           { this.price = p; }

    public double getRent()                    { return rent; }
    public void   setRent(double r)            { this.rent = r; }

    public double getCost()                    { return cost; }
    public void   setCost(double c)            { this.cost = c; }

    public int  getCreatedBy()                 { return createdBy; }
    public void setCreatedBy(int uid)          { this.createdBy = uid; }

    /** Convenience: monthly net profit */
    public double getMonthlyProfit() { return rent - cost; }

    /** Convenience: annual net profit */
    public double getAnnualProfit() { return getMonthlyProfit() * 12; }

    @Override
    public String toString() {
        return "Property{id=" + propertyId + ", name='" + propertyName
             + "', locationId=" + locationId + ", price=" + price + "}";
    }
}