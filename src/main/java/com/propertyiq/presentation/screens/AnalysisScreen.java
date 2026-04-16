package com.propertyiq.presentation.screens;

import com.propertyiq.model.AnalysisResult;
import com.propertyiq.model.Property;
import com.propertyiq.model.User;
import com.propertyiq.presentation.theme.NordicTheme;
import com.propertyiq.service.InvestmentAnalysisService;
import com.propertyiq.service.PropertyService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * TIER 1 — PRESENTATION
 * Analysis Screen — shows computed ROI, monthly profit, annual yield,
 * investment score, and recommendation for all properties.
 * Also highlights the best investment pick.
 */
public class AnalysisScreen extends JPanel {

    private final DashboardScreen          dashboard;
    private final User                     currentUser;
    private final InvestmentAnalysisService analysisService;
    private final PropertyService          propertyService;
    private final NumberFormat             currency;
    private final NumberFormat             percent;

    private JPanel resultsPanel;
    private JLabel bestInvestmentLabel;

    public AnalysisScreen(DashboardScreen dashboard, User currentUser) {
        this.dashboard       = dashboard;
        this.currentUser     = currentUser;
        this.analysisService = new InvestmentAnalysisService();
        this.propertyService = new PropertyService();
        this.currency        = NumberFormat.getCurrencyInstance(new Locale("en", "MY"));
        this.percent         = NumberFormat.getNumberInstance();
        percent.setMinimumFractionDigits(2);
        percent.setMaximumFractionDigits(2);

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ── Header ────────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        header.add(NordicTheme.headingLabel("Investment Analysis"), BorderLayout.WEST);

        JButton refreshBtn = NordicTheme.primaryButton("🔄 Recalculate All");
        refreshBtn.addActionListener(e -> recalculateAll());
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Best investment banner ────────────────────────────────────────
        JPanel bestPanel = NordicTheme.cardPanel();
        bestPanel.setLayout(new BorderLayout());
        bestPanel.setBackground(NordicTheme.HIGHLIGHT_GREEN);
        bestInvestmentLabel = new JLabel("Calculating best investment...");
        bestInvestmentLabel.setFont(NordicTheme.FONT_SUBHEAD);
        bestInvestmentLabel.setForeground(new Color(0x065F46));
        bestInvestmentLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        bestPanel.add(bestInvestmentLabel, BorderLayout.CENTER);
        add(bestPanel, BorderLayout.NORTH); // will be in a wrapper below

        // Wrap header + banner together in NORTH
        JPanel northSection = new JPanel(new BorderLayout(0, 10));
        northSection.setOpaque(false);
        northSection.add(header, BorderLayout.NORTH);
        northSection.add(bestPanel, BorderLayout.CENTER);
        add(northSection, BorderLayout.NORTH);

        // ── Scrollable results cards ──────────────────────────────────────
        resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(resultsPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        SwingWorker<List<AnalysisResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AnalysisResult> doInBackground() throws SQLException {
                return analysisService.getAllResults();
            }

            @Override
            protected void done() {
                try {
                    List<AnalysisResult> results = get();
                    renderResults(results);
                    Optional<AnalysisResult> best = analysisService.findBestInvestment(results);
                    best.ifPresentOrElse(
                        b -> bestInvestmentLabel.setText("🏆 Best Investment: "
                            + b.getPropertyName()
                            + "  |  Score: " + percent.format(b.getInvestmentScore())
                            + "  |  ROI: " + percent.format(b.getRoi()) + "%"),
                        () -> bestInvestmentLabel.setText("No analysis data yet.")
                    );
                } catch (Exception ex) {
                    bestInvestmentLabel.setText("⚠ Failed to load analysis: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void recalculateAll() {
        bestInvestmentLabel.setText("Recalculating...");
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws SQLException {
                List<Property> props = propertyService.getAllProperties();
                for (Property p : props) analysisService.analyseAndSave(p);
                return null;
            }
            @Override
            protected void done() { loadData(); }
        };
        worker.execute();
    }

    private void renderResults(List<AnalysisResult> results) {
        resultsPanel.removeAll();

        if (results.isEmpty()) {
            JLabel empty = NordicTheme.bodyLabel("No analysis results found. Add properties first.");
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            resultsPanel.add(empty);
            resultsPanel.revalidate();
            return;
        }

        for (AnalysisResult r : results) {
            resultsPanel.add(buildResultCard(r));
            resultsPanel.add(Box.createVerticalStrut(12));
        }
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JPanel buildResultCard(AnalysisResult r) {
        JPanel card = NordicTheme.cardPanel();
        card.setLayout(new BorderLayout(16, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Left: property name + recommendation badge
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(200, 0));

        JLabel nameLbl = new JLabel(r.getPropertyName());
        nameLbl.setFont(NordicTheme.FONT_SUBHEAD);
        nameLbl.setForeground(NordicTheme.TEXT_PRIMARY);

        JLabel recBadge = new JLabel(r.getRecommendationLabel());
        recBadge.setFont(NordicTheme.FONT_SMALL);
        recBadge.setOpaque(true);
        recBadge.setBorder(new EmptyBorder(3, 10, 3, 10));
        recBadge.setBackground(switch (r.getRecommendation()) {
            case HIGH_PROFIT -> NordicTheme.HIGHLIGHT_GREEN;
            case MODERATE    -> NordicTheme.HIGHLIGHT_AMBER;
            case RISKY       -> NordicTheme.HIGHLIGHT_RED;
        });
        recBadge.setForeground(NordicTheme.TEXT_PRIMARY);

        left.add(nameLbl);
        left.add(Box.createVerticalStrut(8));
        left.add(recBadge);

        // Center: metrics grid
        JPanel metrics = new JPanel(new GridLayout(2, 3, 12, 8));
        metrics.setOpaque(false);

        metrics.add(metricBox("ROI",           percent.format(r.getRoi()) + "%"));
        metrics.add(metricBox("Monthly Profit", currency.format(r.getMonthlyProfit())));
        metrics.add(metricBox("Annual Yield",  percent.format(r.getAnnualYield()) + "%"));
        metrics.add(metricBox("Score",         String.format("%.1f / 100", r.getInvestmentScore())));
        metrics.add(metricBox("Annual Income", currency.format(r.getMonthlyProfit() * 12)));
        metrics.add(metricBox("Status",        r.getRecommendationLabel()));

        card.add(left,    BorderLayout.WEST);
        card.add(metrics, BorderLayout.CENTER);

        // Score progress bar on the right
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(100, 0));

        JProgressBar scoreBar = new JProgressBar(0, 100);
        scoreBar.setValue((int) r.getInvestmentScore());
        scoreBar.setStringPainted(true);
        scoreBar.setString(String.format("%.0f%%", r.getInvestmentScore()));
        scoreBar.setFont(NordicTheme.FONT_SMALL);
        scoreBar.setForeground(r.getInvestmentScore() >= 65
            ? new Color(0x34D399)
            : r.getInvestmentScore() >= 40
                ? new Color(0xFBBF24)
                : new Color(0xF87171));
        scoreBar.setBackground(NordicTheme.BORDER_SOFT);
        scoreBar.setBorder(new EmptyBorder(0, 8, 0, 8));
        scoreBar.setOrientation(JProgressBar.VERTICAL);

        JLabel scoreLbl = new JLabel("Score", SwingConstants.CENTER);
        scoreLbl.setFont(NordicTheme.FONT_SMALL);
        scoreLbl.setForeground(NordicTheme.TEXT_SECONDARY);

        rightPanel.add(scoreLbl,  BorderLayout.NORTH);
        rightPanel.add(scoreBar,  BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);

        return card;
    }

    private JPanel metricBox(String label, String value) {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setBackground(NordicTheme.BG_PRIMARY);
        box.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(NordicTheme.BORDER_SOFT, 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
        JLabel lbl = new JLabel(label);
        lbl.setFont(NordicTheme.FONT_SMALL);
        lbl.setForeground(NordicTheme.TEXT_SECONDARY);
        JLabel val = new JLabel(value);
        val.setFont(NordicTheme.FONT_SUBHEAD);
        val.setForeground(NordicTheme.TEXT_PRIMARY);
        box.add(lbl, BorderLayout.NORTH);
        box.add(val, BorderLayout.CENTER);
        return box;
    }
}