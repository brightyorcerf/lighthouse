package com.lighthouse.service;

import com.lighthouse.dao.AnalysisDAO;
import com.lighthouse.model.AnalysisResult;
import com.lighthouse.model.AnalysisResult.Recommendation;
import com.lighthouse.model.Property;
import com.lighthouse.dao.SettingsDAO;
import com.lighthouse.model.Location;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * TIER 2 — SERVICE
 * Investment analysis engine. Computes ROI, annual yield, monthly profit,
 * and a composite Investment Score for any given property.
 *
 * ═══════════════════════════════════════════════════════════════════════════
 *  FORMULA DOCUMENTATION (for examiner / viva)
 * ═══════════════════════════════════════════════════════════════════════════
 *
 *  1. MONTHLY PROFIT
 *     = Rental Income (monthly) − Expenses (monthly)
 *
 *  2. ANNUAL NET INCOME
 *     = Monthly Profit × 12
 *
 *  3. ROI (Return on Investment) — expressed as a percentage
 *     = (Annual Net Income / Purchase Price) × 100
 *     Rationale: Standard gross-yield style ROI; shows how much the investor
 *     earns annually relative to what they paid for the asset.
 *
 *  4. ANNUAL YIELD — same as ROI here, presented as a named metric
 *     (some reports prefer to see both labeled separately)
 *
 *  5. INVESTMENT SCORE (0 – 100 composite index)
 *     Combines three normalised dimensions:
 *
 *     a) ROI Score (weight 50%)
 *        = min(roi / MAX_ROI_BENCHMARK, 1.0) × 50
 *        MAX_ROI_BENCHMARK = 15% (typical upper threshold for residential)
 *
 *     b) Location Score (weight 30%)
 *        = (locationRating / 10.0) × 30
 *        User rates location 1–10 (accessibility, amenities, demand)
 *
 *     c) Risk Score (weight 20%) — INVERSE: lower risk → higher score
 *        LOW    → 20 pts
 *        MEDIUM → 12 pts
 *        HIGH   →  4 pts
 *
 *     Total = ROI Score + Location Score + Risk Score  (max = 100)
 *
 *  6. RECOMMENDATION thresholds
 *     Score ≥ 65  → HIGH_PROFIT
 *     Score ≥ 40  → MODERATE
 *     Score <  40 → RISKY
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class InvestmentAnalysisService {

    // Benchmark ROI: properties above this are considered excellent performers
    private static final double MAX_ROI_BENCHMARK = 15.0;

    // Recommendation thresholds
    private static final double HIGH_PROFIT_THRESHOLD = 65.0;
    private static final double MODERATE_THRESHOLD    = 40.0;

    private final AnalysisDAO analysisDAO;
    private final SettingsDAO settingsDAO;

    public InvestmentAnalysisService() {
        this.analysisDAO = new AnalysisDAO();
        this.settingsDAO = new SettingsDAO();
    }

    // ── Core Computation ──────────────────────────────────────────────────────

    /**
     * Computes an AnalysisResult for the given property (in-memory, not persisted).
     * Call {@link #analyseAndSave(Property)} to also persist to the DB.
     */
    public AnalysisResult compute(Property p) {
        double monthlyProfit = p.getRent() - p.getCost();
        double annualIncome  = monthlyProfit * 12;
        double roi           = (annualIncome / p.getPrice()) * 100.0;
        double annualYield   = roi; // same metric, dual label

        int rating = p.getLocationObj() != null ? p.getLocationObj().getRating() : 5;
        int risk   = computeRisk(p);

        double investmentScore = computeScore(roi, rating, risk);
        Recommendation rec     = classify(investmentScore);

        return new AnalysisResult(
            p.getPropertyId(),
            p.getPropertyName(),
            round(roi, 2),
            round(monthlyProfit, 2),
            round(annualYield, 2),
            round(investmentScore, 2),
            rec
        );
    }

    /**
     * Computes and persists the analysis result to the database.
     * Returns the saved AnalysisResult with its generated analysis_id.
     */
    public AnalysisResult analyseAndSave(Property p) throws SQLException {
        AnalysisResult result = compute(p);
        int id = analysisDAO.save(result);
        result.setAnalysisId(id);
        return result;
    }

    /** Retrieves saved analysis results for all properties. */
    public List<AnalysisResult> getAllResults() throws SQLException {
        return analysisDAO.findAll();
    }

    /** Retrieves analysis for a specific property. */
    public Optional<AnalysisResult> getResultForProperty(int propertyId) throws SQLException {
        return analysisDAO.findByPropertyId(propertyId);
    }

    /**
     * Identifies the best investment from a list of AnalysisResults
     * by comparing investment scores.
     */
    public Optional<AnalysisResult> findBestInvestment(List<AnalysisResult> results) {
        return results.stream()
                      .max(Comparator.comparingDouble(AnalysisResult::getInvestmentScore));
    }

    private int computeRisk(Property p) {
        int baseRisk = p.getLocationObj() != null ? p.getLocationObj().getRisk() : 5;
        int risk = baseRisk;

        try {
            double highPriceThreshold = Double.parseDouble(settingsDAO.getSetting("rule_high_price_threshold", "500000"));
            double lowRentThreshold   = Double.parseDouble(settingsDAO.getSetting("rule_low_rent_threshold", "2000"));
            int penalty               = Integer.parseInt(settingsDAO.getSetting("rule_high_price_low_rent_risk_penalty", "3"));

            if (p.getPrice() > highPriceThreshold && p.getRent() < lowRentThreshold) {
                risk += penalty;
            }

            int demandRatingThreshold = Integer.parseInt(settingsDAO.getSetting("rule_low_demand_rating_threshold", "5"));
            int demandPenalty         = Integer.parseInt(settingsDAO.getSetting("rule_low_demand_risk_penalty", "2"));

            int rating = p.getLocationObj() != null ? p.getLocationObj().getRating() : 5;
            if (rating < demandRatingThreshold) {
                risk += demandPenalty;
            }

        } catch (Exception e) {
            System.err.println("Error parsing risk rules: " + e.getMessage());
        }

        if (risk > 10) risk = 10;
        if (risk < 1) risk = 1;
        return risk;
    }

    // ── Algorithm Helpers ─────────────────────────────────────────────────────

    private double computeScore(double roi, int locationRating, int risk) {
        // Score = (ROI × weight1) + (location × weight2) - (risk × weight3)
        double weight1 = 4.0;
        double weight2 = 3.0;
        double weight3 = 2.0;

        double score = (roi * weight1) + (locationRating * weight2) - (risk * weight3);
        
        // Normalize score between 0 and 100 approximately
        if (score > 100) score = 100;
        if (score < 0) score = 0;
        
        return score;
    }

    private Recommendation classify(double score) {
        if (score >= HIGH_PROFIT_THRESHOLD) return Recommendation.HIGH_PROFIT;
        if (score >= MODERATE_THRESHOLD)    return Recommendation.MODERATE;
        return Recommendation.RISKY;
    }

    private double round(double val, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(val * factor) / factor;
    }
}