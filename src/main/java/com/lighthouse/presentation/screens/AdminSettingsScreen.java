package com.lighthouse.presentation.screens;

import com.lighthouse.dao.LocationDAO;
import com.lighthouse.dao.SettingsDAO;
import com.lighthouse.model.Location;
import com.lighthouse.model.User;
import com.lighthouse.presentation.theme.SoftTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class AdminSettingsScreen extends JPanel {

    private final DashboardScreen dashboard;
    private final User currentUser;
    
    private final LocationDAO locationDAO = new LocationDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();

    // Locations Form
    private JTextField locNameField;
    private JSpinner locRatingSpinner;
    private JSpinner locRiskSpinner;
    private JPanel locListPanel;

    // Rules Form
    private JTextField highPriceField;
    private JTextField lowRentField;
    private JTextField priceRentPenaltyField;

    public AdminSettingsScreen(DashboardScreen dashboard, User currentUser) {
        this.dashboard = dashboard;
        this.currentUser = currentUser;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel scrollContent = new JPanel();
        scrollContent.setLayout(new BoxLayout(scrollContent, BoxLayout.Y_AXIS));
        scrollContent.setOpaque(false);

        scrollContent.add(buildLocationsPanel());
        scrollContent.add(Box.createVerticalStrut(20));
        scrollContent.add(buildRulesPanel());

        JScrollPane scroll = new JScrollPane(scrollContent);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);

        add(SoftTheme.headingLabel("Admin System Management"), BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildLocationsPanel() {
        JPanel panel = SoftTheme.cardPanel();
        panel.setLayout(new BorderLayout(10, 10));

        JLabel title = SoftTheme.headingLabel("Locations Management");
        panel.add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        formPanel.setOpaque(false);

        locNameField = SoftTheme.styledField(15);
        locRatingSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 10, 1));
        locRiskSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 10, 1));

        formPanel.add(SoftTheme.bodyLabel("Name:"));
        formPanel.add(locNameField);
        formPanel.add(SoftTheme.bodyLabel("Rating (1-10):"));
        formPanel.add(locRatingSpinner);
        formPanel.add(SoftTheme.bodyLabel("Base Risk (1-10):"));
        formPanel.add(locRiskSpinner);

        JButton addLocBtn = SoftTheme.successButton("Add Location");
        addLocBtn.addActionListener(e -> addLocation());
        formPanel.add(addLocBtn);

        locListPanel = new JPanel();
        locListPanel.setLayout(new BoxLayout(locListPanel, BoxLayout.Y_AXIS));
        locListPanel.setOpaque(false);

        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(new JScrollPane(locListPanel), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildRulesPanel() {
        JPanel panel = SoftTheme.cardPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));

        panel.add(SoftTheme.headingLabel("Risk Rules Configuration"));
        panel.add(new JLabel("")); // spacer

        highPriceField = SoftTheme.styledField(15);
        lowRentField = SoftTheme.styledField(15);
        priceRentPenaltyField = SoftTheme.styledField(15);

        panel.add(SoftTheme.bodyLabel("High Price Threshold (RM):"));
        panel.add(highPriceField);
        panel.add(SoftTheme.bodyLabel("Low Rent Threshold (RM):"));
        panel.add(lowRentField);
        panel.add(SoftTheme.bodyLabel("Risk Penalty (+ points):"));
        panel.add(priceRentPenaltyField);

        JButton saveRulesBtn = SoftTheme.primaryButton("Save Rules");
        saveRulesBtn.addActionListener(e -> saveRules());
        panel.add(new JLabel(""));
        panel.add(saveRulesBtn);

        return panel;
    }

    private void loadData() {
        try {
            // Load Rules
            highPriceField.setText(settingsDAO.getSetting("rule_high_price_threshold", "500000"));
            lowRentField.setText(settingsDAO.getSetting("rule_low_rent_threshold", "2000"));
            priceRentPenaltyField.setText(settingsDAO.getSetting("rule_high_price_low_rent_risk_penalty", "3"));

            // Load Locations
            refreshLocationsList();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void refreshLocationsList() throws Exception {
        locListPanel.removeAll();
        List<Location> locs = locationDAO.findAll();
        for (Location loc : locs) {
            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT));
            item.setOpaque(false);
            item.add(SoftTheme.bodyLabel("• " + loc.getLocationName() + " (Rating: " + loc.getRating() + ", Risk: " + loc.getRisk() + ")"));
            locListPanel.add(item);
        }
        locListPanel.revalidate();
        locListPanel.repaint();
    }

    private void addLocation() {
        try {
            String name = locNameField.getText().trim();
            if (name.isEmpty()) return;
            int rating = (int) locRatingSpinner.getValue();
            int risk = (int) locRiskSpinner.getValue();

            Location l = new Location(0, name, rating, risk);
            locationDAO.save(l);
            locNameField.setText("");
            refreshLocationsList();
            JOptionPane.showMessageDialog(this, "Location added.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error adding location: " + ex.getMessage());
        }
    }

    private void saveRules() {
        try {
            settingsDAO.setSetting("rule_high_price_threshold", highPriceField.getText().trim());
            settingsDAO.setSetting("rule_low_rent_threshold", lowRentField.getText().trim());
            settingsDAO.setSetting("rule_high_price_low_rent_risk_penalty", priceRentPenaltyField.getText().trim());
            JOptionPane.showMessageDialog(this, "Rules updated securely.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving rules: " + ex.getMessage());
        }
    }
}
