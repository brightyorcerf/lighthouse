package com.lighthouse.presentation.screens;

import com.lighthouse.model.Property;
import com.lighthouse.model.User;
import com.lighthouse.presentation.theme.SoftTheme;
import com.lighthouse.service.PropertyService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * TIER 1 — PRESENTATION
 * Property List Screen — shows all properties in a styled JTable.
 * Admin: can Edit and Delete. Investor: view-only.
 */
public class PropertyListScreen extends JPanel {

    private final DashboardScreen dashboard;
    private final User            currentUser;
    private final PropertyService propertyService;
    private final NumberFormat    currency;

    private JTable         table;
    private DefaultTableModel tableModel;
    private List<Property> properties;

    private static final String[] COLUMNS = {
        "#", "Property Name", "Location", "Price",
        "Rent (mo)", "Cost (mo)", "Location ★", "Risk"
    };

    public PropertyListScreen(DashboardScreen dashboard, User currentUser) {
        this.dashboard       = dashboard;
        this.currentUser     = currentUser;
        this.propertyService = new PropertyService();
        this.currency        = NumberFormat.getCurrencyInstance(new Locale("en", "MY"));
        currency.setMinimumFractionDigits(0);

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ── Header row ────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = SoftTheme.headingLabel("All Properties");
        header.add(title, BorderLayout.WEST);

        if (currentUser.isAdmin()) {
            JButton addBtn = SoftTheme.successButton("+ Add Property");
            addBtn.addActionListener(e -> dashboard.navigateTo("ADD_PROPERTY"));
            header.add(addBtn, BorderLayout.EAST);
        }
        add(header, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(SoftTheme.BORDER_SOFT, 1, true));
        scroll.getViewport().setBackground(SoftTheme.BG_CARD);
        scroll.setBackground(SoftTheme.BG_CARD);
        add(scroll, BorderLayout.CENTER);

        // ── Action buttons (admin only) ───────────────────────────────────
        if (currentUser.isAdmin()) {
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            actions.setOpaque(false);

            JButton editBtn = SoftTheme.warningButton("✏ Edit Selected");
            editBtn.addActionListener(e -> handleEdit());

            JButton deleteBtn = SoftTheme.dangerButton("🗑 Delete Selected");
            deleteBtn.addActionListener(e -> handleDelete());

            JButton analyseBtn = SoftTheme.successButton("📊 Analyse Selected");
            analyseBtn.addActionListener(e -> handleAnalyse());

            actions.add(analyseBtn);
            actions.add(editBtn);
            actions.add(deleteBtn);
            add(actions, BorderLayout.SOUTH);
        }
    }

    private void styleTable() {
        table.setFont(SoftTheme.FONT_BODY);
        table.setForeground(SoftTheme.TEXT_PRIMARY);
        table.setBackground(SoftTheme.BG_CARD);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xBAE6FD));
        table.setSelectionForeground(SoftTheme.TEXT_PRIMARY);
        table.setFillsViewportHeight(true);

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(SoftTheme.FONT_SUBHEAD);
        header.setBackground(SoftTheme.BG_SIDEBAR);
        header.setForeground(SoftTheme.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SoftTheme.BORDER_SOFT));
        header.setReorderingAllowed(false);

        // Column widths
        int[] widths = { 40, 180, 130, 130, 120, 110, 90, 80 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Risk column: colour-coded renderer
        table.getColumnModel().getColumn(7).setCellRenderer(riskRenderer());

        // Alternating row renderer
        table.setDefaultRenderer(Object.class, alternatingRenderer());
    }

    // ── Data ─────────────────────────────────────────────────────────────────

    public void refresh() { loadData(); }

    private void loadData() {
        SwingWorker<List<Property>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Property> doInBackground() throws SQLException {
                return propertyService.getAllProperties();
            }

            @Override
            protected void done() {
                try {
                    properties = get();
                    tableModel.setRowCount(0);
                    int i = 1;
                    for (Property p : properties) {
                        tableModel.addRow(new Object[]{
                            i++,
                            p.getPropertyName(),
                            p.getLocationObj() != null ? p.getLocationObj().getLocationName() : "Unknown",
                            currency.format(p.getPrice()),
                            currency.format(p.getRent()),
                            currency.format(p.getCost()),
                            (p.getLocationObj() != null ? p.getLocationObj().getRating() : "-") + "/10",
                            p.getLocationObj() != null ? String.valueOf(p.getLocationObj().getRisk()) : "Unknown"
                        });
                    }
                } catch (Exception ex) {
                    showError("Failed to load properties: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Please select a property to edit."); return; }
        Property selected = properties.get(row);
        PropertyFormScreen form = new PropertyFormScreen(dashboard, currentUser, selected);
        // Replace the ADD_PROPERTY card with the edit form
        dashboard.navigateTo("ADD_PROPERTY");
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Please select a property to delete."); return; }
        Property selected = properties.get(row);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete \"" + selected.getPropertyName() + "\"?\nThis cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws SQLException {
                return propertyService.deleteProperty(selected.getPropertyId());
            }
            @Override
            protected void done() {
                try {
                    if (get()) {
                        loadData();
                        JOptionPane.showMessageDialog(PropertyListScreen.this,
                            "Property deleted successfully.", "Deleted",
                            JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    showError("Delete failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void handleAnalyse() {
        int row = table.getSelectedRow();
        if (row < 0) { showError("Please select a property to analyse."); return; }
        dashboard.navigateTo("ANALYSIS");
    }

    // ── Renderers ─────────────────────────────────────────────────────────────

    private TableCellRenderer riskRenderer() {
        return (tbl, value, isSelected, hasFocus, row, col) -> {
            JLabel lbl = new JLabel(String.valueOf(value), SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setFont(SoftTheme.FONT_SMALL);
            if (isSelected) {
                lbl.setBackground(tbl.getSelectionBackground());
            } else {
                int riskVal = 5;
                try {
                    riskVal = Integer.parseInt(String.valueOf(value));
                } catch(Exception e){}

                if (riskVal <= 3) {
                    lbl.setBackground(SoftTheme.HIGHLIGHT_GREEN);
                } else if (riskVal <= 6) {
                    lbl.setBackground(SoftTheme.HIGHLIGHT_AMBER);
                } else {
                    lbl.setBackground(SoftTheme.HIGHLIGHT_RED);
                }
            }
            lbl.setForeground(SoftTheme.TEXT_PRIMARY);
            return lbl;
        };
    }

    private TableCellRenderer alternatingRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) setBackground(row % 2 == 0 ? SoftTheme.BG_CARD : new Color(0xF1F5F9));
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setForeground(SoftTheme.TEXT_PRIMARY);
                return this;
            }
        };
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.WARNING_MESSAGE);
    }
}