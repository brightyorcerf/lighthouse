package com.lighthouse.dao;

import com.lighthouse.database.DatabaseConnection;
import com.lighthouse.model.Property;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.lighthouse.model.Location;

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
        p.setLocationId(rs.getInt("location_id"));
        p.setPrice(rs.getDouble("price"));
        p.setRent(rs.getDouble("rent"));
        p.setCost(rs.getDouble("cost"));
        p.setCreatedBy(rs.getInt("created_by"));

        Location loc = new Location();
        loc.setLocationId(rs.getInt("location_id"));
        try {
            loc.setLocationName(rs.getString("location_name"));
            loc.setRating(rs.getInt("rating"));
            loc.setRisk(rs.getInt("risk"));
        } catch (SQLException e) {
            // Some queries might not join location, handle gracefully
        }
        p.setLocationObj(loc);

        return p;
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Property> findAll() throws SQLException {
        List<Property> list = new ArrayList<>();
        String sql = "SELECT p.*, l.location_name, l.rating, l.risk FROM properties p JOIN locations l ON p.location_id = l.location_id ORDER BY p.property_id";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public Optional<Property> findById(int propertyId) throws SQLException {
        String sql = "SELECT p.*, l.location_name, l.rating, l.risk FROM properties p JOIN locations l ON p.location_id = l.location_id WHERE p.property_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, propertyId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(mapRow(rs));
        }
        return Optional.empty();
    }

    /**
     * Search properties by locationId OR price range.
     * Either parameter can be null to be ignored.
     */
    public List<Property> search(Integer locationId, Double minPrice, Double maxPrice) throws SQLException {
        List<Property> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT p.*, l.location_name, l.rating, l.risk FROM properties p JOIN locations l ON p.location_id = l.location_id WHERE 1=1");

        if (locationId != null)
            sql.append(" AND p.location_id = ?");
        if (minPrice != null)
            sql.append(" AND p.price >= ?");
        if (maxPrice != null)
            sql.append(" AND p.price <= ?");
        sql.append(" ORDER BY p.property_id");

        try (PreparedStatement ps = conn().prepareStatement(sql.toString())) {
            int idx = 1;
            if (locationId != null)
                ps.setInt(idx++, locationId);
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
              (property_name, location_id, price, rent, cost, created_by)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getPropertyName());
            ps.setInt(2, p.getLocationId());
            ps.setDouble(3, p.getPrice());
            ps.setDouble(4, p.getRent());
            ps.setDouble(5, p.getCost());
            ps.setInt(6, p.getCreatedBy());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        }
        return -1;
    }

    public boolean update(Property p) throws SQLException {
        String sql = """
            UPDATE properties SET
              property_name = ?, location_id = ?, price = ?,
              rent = ?, cost = ?
            WHERE property_id = ?
            """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p.getPropertyName());
            ps.setInt(2, p.getLocationId());
            ps.setDouble(3, p.getPrice());
            ps.setDouble(4, p.getRent());
            ps.setDouble(5, p.getCost());
            ps.setInt(6, p.getPropertyId());
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