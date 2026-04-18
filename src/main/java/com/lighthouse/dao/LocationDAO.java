package com.lighthouse.dao;

import com.lighthouse.database.DatabaseConnection;
import com.lighthouse.model.Location;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocationDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Location> findAll() throws SQLException {
        List<Location> list = new ArrayList<>();
        String sql = "SELECT * FROM locations ORDER BY location_name ASC";
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Location(
                    rs.getInt("location_id"),
                    rs.getString("location_name"),
                    rs.getInt("rating"),
                    rs.getInt("risk")
                ));
            }
        }
        return list;
    }

    public Location findById(int id) throws SQLException {
        String sql = "SELECT * FROM locations WHERE location_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Location(
                        rs.getInt("location_id"),
                        rs.getString("location_name"),
                        rs.getInt("rating"),
                        rs.getInt("risk")
                    );
                }
            }
        }
        return null;
    }

    public int save(Location loc) throws SQLException {
        String sql = "INSERT INTO locations (location_name, rating, risk) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, loc.getLocationName());
            ps.setInt(2, loc.getRating());
            ps.setInt(3, loc.getRisk());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean update(Location loc) throws SQLException {
        String sql = "UPDATE locations SET location_name = ?, rating = ?, risk = ? WHERE location_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, loc.getLocationName());
            ps.setInt(2, loc.getRating());
            ps.setInt(3, loc.getRisk());
            ps.setInt(4, loc.getLocationId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM locations WHERE location_id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}
