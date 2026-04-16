package com.lighthouse.dao;

import com.lighthouse.database.DatabaseConnection;
import com.lighthouse.model.Property;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TIER 3 — DAO
 * Handles all CRUD operations for the 'properties' table.
 */
public class PropertyDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Property mapRow(ResultSet rs) throws SQLException {
        Property p = new Property();
        p.setPropertyId(rs.getInt("property_id"));
        p.setPropertyName(rs.getString("property_name"));
        p.setLocation(rs.getString("location"));
        p.setPurchasePrice(rs.getDouble("purchase_price"));
        p.setRentalIncome(rs.getDouble("rental_income"));
        p.setExpenses(rs.getDouble("expenses"));
        p.setLocationRating(rs.getInt("location_rating"));
        p.setRiskLevel(Property.RiskLevel.valueOf(rs.getString("risk_level")));
        p.setCreatedBy(rs.getInt("created_by"));
        return p;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Property> findAll() throws SQLException {
        List<Property> list = new ArrayList<>();
        String sql = "SELECT * FROM properties ORDER BY property_id";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Property> findById(int propertyId) throws SQLException {
        String sql = "SELECT * FROM properties WHERE property_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, propertyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    /**
     * Search properties by location (partial match) OR price range.
     * Either parameter can be null to be ignored.
     */
    public List<Property> search(String location, Double minPrice, Double maxPrice) throws SQLException {
        List<Property> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM properties WHERE 1=1");

        if (location != null && !location.isBlank())
            sql.append(" AND LOWER(location) LIKE ?");
        if (minPrice != null)
            sql.append(" AND purchase_price >= ?");
        if (maxPrice != null)
            sql.append(" AND purchase_price <= ?");
        sql.append(" ORDER BY property_id");

        try (PreparedStatement ps = conn().prepareStatement(sql.toString())) {
            int idx = 1;
            if (location != null && !location.isBlank())
                ps.setString(idx++, "%" + location.toLowerCase() + "%");
            if (minPrice != null)
                ps.setDouble(idx++, minPrice);
            if (maxPrice != null)
                ps.setDouble(idx, maxPrice);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    public int insert(Property p) throws SQLException {
        String sql = """
            INSERT INTO properties
              (property_name, location, purchase_price, rental_income, expenses,
               location_rating, risk_level, created_by)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getPropertyName());
            ps.setString(2, p.getLocation());
            ps.setDouble(3, p.getPurchasePrice());
            ps.setDouble(4, p.getRentalIncome());
            ps.setDouble(5, p.getExpenses());
            ps.setInt(6, p.getLocationRating());
            ps.setString(7, p.getRiskLevel().name());
            ps.setInt(8, p.getCreatedBy());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public boolean update(Property p) throws SQLException {
        String sql = """
            UPDATE properties SET
              property_name = ?, location = ?, purchase_price = ?,
              rental_income = ?, expenses = ?, location_rating = ?, risk_level = ?
            WHERE property_id = ?
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.getPropertyName());
            ps.setString(2, p.getLocation());
            ps.setDouble(3, p.getPurchasePrice());
            ps.setDouble(4, p.getRentalIncome());
            ps.setDouble(5, p.getExpenses());
            ps.setInt(6, p.getLocationRating());
            ps.setString(7, p.getRiskLevel().name());
            ps.setInt(8, p.getPropertyId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int propertyId) throws SQLException {
        String sql = "DELETE FROM properties WHERE property_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, propertyId);
            return ps.executeUpdate() > 0;
        }
    }
}