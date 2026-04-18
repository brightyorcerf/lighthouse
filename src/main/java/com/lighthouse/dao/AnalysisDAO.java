package com.lighthouse.dao;

import com.lighthouse.database.DatabaseConnection;
import com.lighthouse.model.AnalysisResult;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TIER 3 — DAO
 * Handles all CRUD operations for the 'analysis' table.
 */
public class AnalysisDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AnalysisResult mapRow(ResultSet rs) throws SQLException {
        AnalysisResult a = new AnalysisResult();
        a.setAnalysisId(rs.getInt("analysis_id"));
        a.setPropertyId(rs.getInt("property_id"));
        a.setRoi(rs.getDouble("roi"));
        a.setMonthlyProfit(rs.getDouble("monthly_profit"));
        a.setAnnualYield(rs.getDouble("annual_yield") != 0
                         ? rs.getDouble("annual_yield")
                         : a.getMonthlyProfit() * 12);
        a.setInvestmentScore(rs.getDouble("investment_score"));
        a.setRecommendation(AnalysisResult.Recommendation.valueOf(rs.getString("recommendation")));
        return a;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<AnalysisResult> findAll() throws SQLException {
        List<AnalysisResult> list = new ArrayList<>();
        // Join with properties to get property names for display
        String sql = """
            SELECT a.*, p.property_name
            FROM analysis a
            JOIN properties p ON a.property_id = p.property_id
            ORDER BY a.investment_score DESC
            """;
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                AnalysisResult ar = mapRow(rs);
                ar.setPropertyName(rs.getString("property_name"));
                list.add(ar);
            }
        }
        return list;
    }

    public Optional<AnalysisResult> findByPropertyId(int propertyId) throws SQLException {
        String sql = """
            SELECT a.*, p.property_name
            FROM analysis a
            JOIN properties p ON a.property_id = p.property_id
            WHERE a.property_id = ?
            ORDER BY a.analysis_id DESC
            LIMIT 1
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, propertyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                AnalysisResult ar = mapRow(rs);
                ar.setPropertyName(rs.getString("property_name"));
                return Optional.of(ar);
            }
        }
        return Optional.empty();
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    /**
     * Upsert: deletes any prior analysis for this property, then inserts fresh.
     * Keeps the table clean — one current analysis record per property.
     */
    public int save(AnalysisResult a) throws SQLException {
        // Remove stale records first
        try (PreparedStatement del = conn().prepareStatement(
                "DELETE FROM analysis WHERE property_id = ?")) {
            del.setInt(1, a.getPropertyId());
            del.executeUpdate();
        }
        // Insert fresh
        String sql = """
            INSERT INTO analysis
              (property_id, roi, monthly_profit, annual_yield, investment_score, recommendation)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getPropertyId());
            ps.setDouble(2, a.getRoi());
            ps.setDouble(3, a.getMonthlyProfit());
            ps.setDouble(4, a.getAnnualYield());
            ps.setDouble(5, a.getInvestmentScore());
            ps.setString(6, a.getRecommendation().name());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public boolean deleteByPropertyId(int propertyId) throws SQLException {
        String sql = "DELETE FROM analysis WHERE property_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, propertyId);
            return ps.executeUpdate() > 0;
        }
    }
}