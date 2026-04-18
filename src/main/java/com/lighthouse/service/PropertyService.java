package com.lighthouse.service;

import com.lighthouse.dao.AnalysisDAO;
import com.lighthouse.dao.PropertyDAO;
import com.lighthouse.model.AnalysisResult;
import com.lighthouse.model.Property;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * TIER 2 — SERVICE
 * Property management business logic: validation, CRUD orchestration,
 * and automatic analysis triggering on save/update.
 */
public class PropertyService {

    private final PropertyDAO              propertyDAO;
    private final AnalysisDAO              analysisDAO;
    private final InvestmentAnalysisService analysisService;

    public PropertyService() {
        this.propertyDAO     = new PropertyDAO();
        this.analysisDAO     = new AnalysisDAO();
        this.analysisService = new InvestmentAnalysisService();
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<Property> getAllProperties() throws SQLException {
        return propertyDAO.findAll();
    }

    public Optional<Property> getById(int propertyId) throws SQLException {
        return propertyDAO.findById(propertyId);
    }

    public List<Property> search(Integer locationId, Double minPrice, Double maxPrice)
            throws SQLException {
        return propertyDAO.search(locationId, minPrice, maxPrice);
    }

    // ── Mutations ─────────────────────────────────────────────────────────────

    /**
     * Validates, saves the property, then automatically computes and persists
     * its investment analysis. Implements atomic-style logic: if analysis
     * fails, the property insert still stands (analysis can be re-run).
     */
    public Property addProperty(Property p) throws ValidationException, SQLException {
        validate(p);
        int id = propertyDAO.insert(p);
        p.setPropertyId(id);
        // Trigger automatic analysis
        try {
            analysisService.analyseAndSave(p);
        } catch (SQLException e) {
            System.err.println("[Service] Warning: property saved but analysis failed: " + e.getMessage());
        }
        return p;
    }

    /**
     * Updates a property and re-runs its analysis.
     */
    public boolean updateProperty(Property p) throws ValidationException, SQLException {
        validate(p);
        boolean updated = propertyDAO.update(p);
        if (updated) {
            try {
                analysisService.analyseAndSave(p);
            } catch (SQLException e) {
                System.err.println("[Service] Warning: property updated but analysis re-run failed: " + e.getMessage());
            }
        }
        return updated;
    }

    /**
     * Deletes a property and its associated analysis records.
     */
    public boolean deleteProperty(int propertyId) throws SQLException {
        analysisDAO.deleteByPropertyId(propertyId); // cascade handled by FK, but explicit is clearer
        return propertyDAO.delete(propertyId);
    }

    // ── Validation ───────────────────────────────────────────────────────────

    private void validate(Property p) throws ValidationException {
        if (p.getPropertyName() == null || p.getPropertyName().isBlank())
            throw new ValidationException("Property name is required.");
        if (p.getLocationId() <= 0)
            throw new ValidationException("Location must be selected.");
        if (p.getPrice() <= 0)
            throw new ValidationException("Price must be greater than zero.");
        if (p.getRent() < 0)
            throw new ValidationException("Rent cannot be negative.");
        if (p.getCost() < 0)
            throw new ValidationException("Cost cannot be negative.");
    }

    // ── Custom Exception ──────────────────────────────────────────────────────

    public static class ValidationException extends Exception {
        public ValidationException(String message) { super(message); }
    }
}