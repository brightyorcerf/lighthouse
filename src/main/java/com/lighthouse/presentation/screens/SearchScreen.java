package com.lighthouse.presentation.screens;

import com.lighthouse.model.Property;
import com.lighthouse.model.User;
import com.lighthouse.presentation.theme.SoftTheme;
import com.lighthouse.service.PropertyService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * TIER 1 — PRESENTATION
 * Search Screen — filter properties by location (partial match)
 * and/or price range. Results shown in a styled table.
 */
public class SearchScreen extends JPanel {

    private final DashboardScreen dashboard;
    private final User            currentUser;
    private final PropertyService propertyService;
    private final NumberFormat    currency;

    private JTextField   locationField;
    private JTextField   minPriceField;
    private JTextField   maxPriceField;
    private JTable       table;
    private DefaultTableModel tableModel;
    private JLabel       resultCountLabel;

    private static final String[] COLUMNS = {
        "#", "Property Name", "Location", "Purchase Price",
        "Rental (mo.)", "Expenses (mo.)", "Rating", "Risk"
    };

    public SearchScreen(DashboardScreen dashboard, User currentUser) {
        this.dashboard       = dashboard;
        this.currentUser     = currentUser;
        this.propertyService = new PropertyService();
        this.currency        = NumberFormat.getCurrencyInstance(new Locale("en", "MY"));
        currency.setMinimumFractionDigits(0);

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        buildUI();
        runSearch(); // load all on open
    }

    private void buildUI() {
        // ── Header ────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        header.add(SoftTheme.headingLabel("Search Properties"), BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ── Search filter card ────────────────────────────────────────────
        JPanel filterCard = SoftTheme.cardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));

        filterCard.add(SoftTheme.bodyLabel("Location:"));
        locationField = SoftTheme.styledField(16);
        locationField.setToolTipText("Partial match, e.g. 'Kuala'");
        filterCard.add(locationField);

        filterCard.add(Box.createHorizontalStrut(8));
        filterCard.add(SoftTheme.bodyLabel("Min Price (RM):"));
        minPriceField = SoftTheme.styledField(10);
        filterCard.add(minPriceField);

        filterCard.add(SoftTheme.bodyLabel("Max Price (RM):"));
        maxPriceField = SoftTheme.styledField(10);
        filterCard.add(maxPriceField);

        filterCard.add(Box.createHorizontalStrut(8));
        JButton searchBtn = SoftTheme.successButton("🔍 Search");
        searchBtn.addActionListener(e -> runSearch());
        filterCard.add(searchBtn);

        JButton clearBtn = SoftTheme.primaryButton("✕ Clear");
        clearBtn.addActionListener(e -> {
            locationField.setText("");
            minPriceField.setText("");
            maxPriceField.setText("");
            runSearch();
        });
        filterCard.add(clearBtn);

        // Trigger search on Enter from any field
        locationField.addActionListener(e -> runSearch());
        minPriceField.addActionListener(e -> runSearch());
        maxPriceField.addActionListener(e -> runSearch());

        add(filterCard, BorderLayout.NORTH); // will be wrapped
        JPanel northWrap = new JPanel(new BorderLayout(0, 12));
        northWrap.setOpaque(false);
        northWrap.add(header, BorderLayout.NORTH);
        northWrap.add(filterCard, BorderLayout.CENTER);
        // Re-add properly
        removeAll();
        add(northWrap, BorderLayout.NORTH);

        // ── Result count ─────────────────────────────────────────────────
        resultCountLabel = new JLabel("  ");
        resultCountLabel.setFont(SoftTheme.FONT_SMALL);
        resultCountLabel.setForeground(SoftTheme.TEXT_SECONDARY);
        add(resultCountLabel, BorderLayout.BEFORE_FIRST_LINE);

        // ── Results table ─────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(SoftTheme.BORDER_SOFT, 1, true));
        scroll.getViewport().setBackground(SoftTheme.BG_CARD);
        add(scroll, BorderLayout.CENTER);

        // ── Quick analyse button for selected row ─────────────────────────
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomBar.setOpaque(false);

        JButton viewAnalysisBtn = SoftTheme.primaryButton("📊 View Full Analysis");
        viewAnalysisBtn.addActionListener(e -> dashboard.navigateTo("ANALYSIS"));
        bottomBar.add(viewAnalysisBtn);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void runSearch() {
        String location = locationField.getText().trim();
        Double minPrice = null, maxPrice = null;

        try {
            if (!minPriceField.getText().isBlank())
                minPrice = Double.parseDouble(minPriceField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Min Price must be a number.");
            return;
        }
        try {
            if (!maxPriceField.getText().isBlank())
                maxPrice = Double.parseDouble(maxPriceField.getText().trim());
        } catch (NumberFormatException ex) {
            showError("Max Price must be a number.");
            return;
        }

        final Double fMin = minPrice;
        final Double fMax = maxPrice;
        final String fLoc = location.isEmpty() ? null : location;

        SwingWorker<List<Property>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Property> doInBackground() throws SQLException {
                return propertyService.search(fLoc, fMin, fMax);
            }

            @Override
            protected void done() {
                try {
                    List<Property> props = get();
                    tableModel.setRowCount(0);
                    int i = 1;
                    for (Property p : props) {
                        tableModel.addRow(new Object[]{
                            i++,
                            p.getPropertyName(),
                            p.getLocation(),
                            currency.format(p.getPurchasePrice()),
                            currency.format(p.getRentalIncome()),
                            currency.format(p.getExpenses()),
                            p.getLocationRating() + "/10",
                            p.getRiskLevel().name()
                        });
                    }
                    resultCountLabel.setText("  " + props.size()
                        + " propert" + (props.size() == 1 ? "y" : "ies") + " found");
                } catch (Exception ex) {
                    showError("Search failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
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

        JTableHeader header = table.getTableHeader();
        header.setFont(SoftTheme.FONT_SUBHEAD);
        header.setBackground(SoftTheme.BG_SIDEBAR);
        header.setForeground(SoftTheme.TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, SoftTheme.BORDER_SOFT));
        header.setReorderingAllowed(false);

        int[] widths = { 40, 180, 130, 130, 110, 110, 80, 80 };
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Risk renderer
        table.getColumnModel().getColumn(7).setCellRenderer(
            (tbl, value, sel, focus, row, col) -> {
                JLabel lbl = new JLabel(String.valueOf(value), SwingConstants.CENTER);
                lbl.setOpaque(true);
                lbl.setFont(SoftTheme.FONT_SMALL);
                lbl.setBackground(sel ? tbl.getSelectionBackground()
                    : switch (String.valueOf(value)) {
                        case "LOW"    -> SoftTheme.HIGHLIGHT_GREEN;
                        case "MEDIUM" -> SoftTheme.HIGHLIGHT_AMBER;
                        case "HIGH"   -> SoftTheme.HIGHLIGHT_RED;
                        default       -> SoftTheme.BG_CARD;
                    });
                lbl.setForeground(SoftTheme.TEXT_PRIMARY);
                return lbl;
            }
        );

        // Alternating rows
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                if (!sel) setBackground(row % 2 == 0 ? SoftTheme.BG_CARD : new Color(0xF1F5F9));
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setForeground(SoftTheme.TEXT_PRIMARY);
                return this;
            }
        });
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Search Error", JOptionPane.WARNING_MESSAGE);
    }
}