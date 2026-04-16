package com.propertyiq.presentation.screens;

import com.propertyiq.model.Property;
import com.propertyiq.model.User;
import com.propertyiq.presentation.theme.SoftTheme;
import com.propertyiq.service.PropertyService;
import com.propertyiq.service.PropertyService.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

/**
 * TIER 1 — PRESENTATION
 * Property Form Screen — Add new property or Edit existing one.
 * Contains full input validation before delegating to PropertyService.
 */
public class PropertyFormScreen extends JPanel {

    private final DashboardScreen dashboard;
    private final User            currentUser;
    private final Property        editTarget; // null = add mode, non-null = edit mode
    private final PropertyService propertyService;

    // Form fields
    private JTextField  nameField, locationField, priceField, rentalField, expensesField;
    private JSpinner    ratingSpinner;
    private JComboBox<Property.RiskLevel> riskCombo;
    private JLabel      errorLabel;

    public PropertyFormScreen(DashboardScreen dashboard, User currentUser, Property editTarget) {
        this.dashboard       = dashboard;
        this.currentUser     = currentUser;
        this.editTarget      = editTarget;
        this.propertyService = new PropertyService();

        setLayout(new BorderLayout());
        setOpaque(false);
        buildUI();
        if (editTarget != null) populateFields();
    }

    private void buildUI() {
        boolean isEdit = (editTarget != null);

        // ── Page header ──────────────────────────────────────────────────
        JLabel pageTitle = SoftTheme.headingLabel(isEdit ? "Edit Property" : "Add New Property");
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        titlePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        titlePanel.add(pageTitle, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        // ── Card wrapper ─────────────────────────────────────────────────
        JPanel card = SoftTheme.cardPanel();
        card.setLayout(new GridBagLayout());
        add(card, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(8, 12, 8, 12);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.anchor  = GridBagConstraints.WEST;

        // Helper: adds label + field pair on a single row
        int row = 0;

        // Row 0: Property Name
        row = addFormRow(card, gbc, row, "Property Name *", nameField = SoftTheme.styledField(25));

        // Row 1: Location
        row = addFormRow(card, gbc, row, "Location *", locationField = SoftTheme.styledField(25));

        // Row 2 & 3: Price / Rental Income on same line
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        card.add(makeLabelPanel("Purchase Price (RM) *"), gbc);
        priceField = SoftTheme.styledField(14);
        gbc.gridx = 1; gbc.weightx = 0.5;
        card.add(priceField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        card.add(makeLabelPanel("Monthly Rental (RM) *"), gbc);
        rentalField = SoftTheme.styledField(14);
        gbc.gridx = 3; gbc.weightx = 0.5;
        card.add(rentalField, gbc);
        row++;

        // Row 4: Expenses / Location Rating
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        card.add(makeLabelPanel("Monthly Expenses (RM) *"), gbc);
        expensesField = SoftTheme.styledField(14);
        gbc.gridx = 1; gbc.weightx = 0.5;
        card.add(expensesField, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        card.add(makeLabelPanel("Location Rating (1–10) *"), gbc);
        ratingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 10, 1));
        ratingSpinner.setFont(SoftTheme.FONT_BODY);
        ((JSpinner.DefaultEditor) ratingSpinner.getEditor()).getTextField().setFont(SoftTheme.FONT_BODY);
        gbc.gridx = 3; gbc.weightx = 0.5;
        card.add(ratingSpinner, gbc);
        row++;

        // Row 5: Risk Level
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        card.add(makeLabelPanel("Risk Level *"), gbc);
        riskCombo = new JComboBox<>(Property.RiskLevel.values());
        riskCombo.setFont(SoftTheme.FONT_BODY);
        riskCombo.setBackground(SoftTheme.BG_CARD);
        gbc.gridx = 1; gbc.weightx = 0.5;
        card.add(riskCombo, gbc);
        row++;

        // Rating hint label
        gbc.gridx = 2; gbc.gridy = row - 1; gbc.gridwidth = 2;
        JLabel ratingHint = new JLabel("1 = Poor area,  10 = Prime location");
        ratingHint.setFont(SoftTheme.FONT_SMALL);
        ratingHint.setForeground(SoftTheme.TEXT_SECONDARY);
        card.add(ratingHint, gbc);
        gbc.gridwidth = 1;

        // Error label
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        errorLabel = new JLabel(" ");
        errorLabel.setFont(SoftTheme.FONT_SMALL);
        errorLabel.setForeground(new Color(0xEF4444));
        card.add(errorLabel, gbc);
        row++;

        // Buttons
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = SoftTheme.primaryButton("Cancel");
        cancelBtn.addActionListener(e -> dashboard.navigateTo("PROPERTIES"));

        String submitLabel = isEdit ? "💾 Save Changes" : "✅ Add Property";
        JButton submitBtn = SoftTheme.successButton(submitLabel);
        submitBtn.addActionListener(e -> handleSubmit());

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);
        card.add(btnPanel, gbc);

        // Push remaining space to top
        gbc.gridy++; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.VERTICAL;
        card.add(Box.createVerticalGlue(), gbc);
    }

    private int addFormRow(JPanel card, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.weightx = 0;
        card.add(makeLabelPanel(label), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1.0;
        card.add(field, gbc);
        return row + 1;
    }

    private JPanel makeLabelPanel(String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        JLabel l = SoftTheme.bodyLabel(text);
        p.add(l);
        return p;
    }

    private void populateFields() {
        nameField.setText(editTarget.getPropertyName());
        locationField.setText(editTarget.getLocation());
        priceField.setText(String.valueOf(editTarget.getPurchasePrice()));
        rentalField.setText(String.valueOf(editTarget.getRentalIncome()));
        expensesField.setText(String.valueOf(editTarget.getExpenses()));
        ratingSpinner.setValue(editTarget.getLocationRating());
        riskCombo.setSelectedItem(editTarget.getRiskLevel());
    }

    private void handleSubmit() {
        // ── Input parsing with validation ─────────────────────────────────
        String name     = nameField.getText().trim();
        String location = locationField.getText().trim();
        String priceStr   = priceField.getText().trim();
        String rentalStr  = rentalField.getText().trim();
        String expenseStr = expensesField.getText().trim();

        if (name.isEmpty() || location.isEmpty() || priceStr.isEmpty()
            || rentalStr.isEmpty() || expenseStr.isEmpty()) {
            showError("All fields marked * are required.");
            return;
        }

        double price, rental, expenses;
        try {
            price    = Double.parseDouble(priceStr);
            rental   = Double.parseDouble(rentalStr);
            expenses = Double.parseDouble(expenseStr);
        } catch (NumberFormatException ex) {
            showError("Price, Rental, and Expenses must be valid numbers.");
            return;
        }

        Property p;
        if (editTarget != null) {
            p = editTarget;
        } else {
            p = new Property();
            p.setCreatedBy(currentUser.getUserId());
        }
        p.setPropertyName(name);
        p.setLocation(location);
        p.setPurchasePrice(price);
        p.setRentalIncome(rental);
        p.setExpenses(expenses);
        p.setLocationRating((int) ratingSpinner.getValue());
        p.setRiskLevel((Property.RiskLevel) riskCombo.getSelectedItem());

        SwingWorker<Property, Void> worker = new SwingWorker<>() {
            @Override
            protected Property doInBackground() throws ValidationException, SQLException {
                if (editTarget != null) {
                    propertyService.updateProperty(p);
                    return p;
                } else {
                    return propertyService.addProperty(p);
                }
            }

            @Override
            protected void done() {
                try {
                    get();
                    JOptionPane.showMessageDialog(PropertyFormScreen.this,
                        editTarget != null
                            ? "Property updated successfully! Analysis has been recalculated."
                            : "Property added successfully! Investment analysis calculated.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    dashboard.navigateTo("PROPERTIES");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    showError(cause != null ? cause.getMessage() : "An error occurred.");
                }
            }
        };
        worker.execute();
    }

    private void showError(String msg) {
        errorLabel.setText("⚠ " + msg);
    }
}