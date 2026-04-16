package com.lighthouse.model;

/**
 * MODEL — AnalysisResult
 * Holds the computed investment metrics for a single property.
 */
public class AnalysisResult {

    public enum Recommendation { HIGH_PROFIT, MODERATE, RISKY }

    private int            analysisId;
    private int            propertyId;
    private String         propertyName;   // denormalised for display convenience
    private double         roi;            // as percentage, e.g. 8.5 = 8.5%
    private double         monthlyProfit;
    private double         annualYield;    // as percentage
    private double         investmentScore;// 0–100
    private Recommendation recommendation;

    // ── Constructors ──────────────────────────────────────────────────────────

    public AnalysisResult() {}

    public AnalysisResult(int propertyId, String propertyName,
                          double roi, double monthlyProfit, double annualYield,
                          double investmentScore, Recommendation recommendation) {
        this.propertyId      = propertyId;
        this.propertyName    = propertyName;
        this.roi             = roi;
        this.monthlyProfit   = monthlyProfit;
        this.annualYield     = annualYield;
        this.investmentScore = investmentScore;
        this.recommendation  = recommendation;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int    getAnalysisId()              { return analysisId; }
    public void   setAnalysisId(int id)        { this.analysisId = id; }

    public int    getPropertyId()              { return propertyId; }
    public void   setPropertyId(int id)        { this.propertyId = id; }

    public String getPropertyName()            { return propertyName; }
    public void   setPropertyName(String n)    { this.propertyName = n; }

    public double getRoi()                     { return roi; }
    public void   setRoi(double r)             { this.roi = r; }

    public double getMonthlyProfit()           { return monthlyProfit; }
    public void   setMonthlyProfit(double p)   { this.monthlyProfit = p; }

    public double getAnnualYield()             { return annualYield; }
    public void   setAnnualYield(double y)     { this.annualYield = y; }

    public double getInvestmentScore()         { return investmentScore; }
    public void   setInvestmentScore(double s) { this.investmentScore = s; }

    public Recommendation getRecommendation()           { return recommendation; }
    public void           setRecommendation(Recommendation r) { this.recommendation = r; }

    /** Human-readable recommendation label */
    public String getRecommendationLabel() {
        return switch (recommendation) {
            case HIGH_PROFIT -> "⭐ High Profit";
            case MODERATE    -> "🔶 Moderate";
            case RISKY       -> "⚠ Risky";
        };
    }
}