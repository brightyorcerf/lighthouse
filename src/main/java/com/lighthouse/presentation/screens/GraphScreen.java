package com.lighthouse.presentation.screens;

import com.lighthouse.model.AnalysisResult;
import com.lighthouse.presentation.theme.SoftTheme;
import com.lighthouse.service.InvestmentAnalysisService;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * TIER 1 — PRESENTATION
 * Graph Screen — JFreeChart visualisations with the pastel palette.
 * Charts:
 *   1. Bar chart  — Monthly Profit per property
 *   2. Bar chart  — ROI (%) per property
 *   3. Line chart — Investment Score comparison
 */
public class GraphScreen extends JPanel {

    private final DashboardScreen          dashboard;
    private final InvestmentAnalysisService analysisService;

    private JPanel chartsWrapper;

    public GraphScreen(DashboardScreen dashboard) {
        this.dashboard       = dashboard;
        this.analysisService = new InvestmentAnalysisService();

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        buildUI();
        loadCharts();
    }

    private void buildUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 8, 0));
        header.add(SoftTheme.headingLabel("Investment Graphs"), BorderLayout.WEST);

        JButton refreshBtn = SoftTheme.primaryButton("🔄 Refresh");
        refreshBtn.addActionListener(e -> loadCharts());
        header.add(refreshBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Charts wrapper (2x2 grid, top row has 2 charts, bottom has 1 centred)
        chartsWrapper = new JPanel(new GridLayout(2, 2, 16, 16));
        chartsWrapper.setOpaque(false);

        JScrollPane scroll = new JScrollPane(chartsWrapper);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadCharts() {
        chartsWrapper.removeAll();
        JLabel loading = SoftTheme.bodyLabel("Loading charts...");
        loading.setHorizontalAlignment(SwingConstants.CENTER);
        chartsWrapper.add(loading);
        chartsWrapper.revalidate();

        SwingWorker<List<AnalysisResult>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<AnalysisResult> doInBackground() throws SQLException {
                return analysisService.getAllResults();
            }

            @Override
            protected void done() {
                try {
                    List<AnalysisResult> results = get();
                    chartsWrapper.removeAll();
                    if (results.isEmpty()) {
                        chartsWrapper.add(SoftTheme.bodyLabel("No data available. Add properties first."));
                    } else {
                        chartsWrapper.setLayout(new GridLayout(2, 2, 16, 16));
                        chartsWrapper.add(wrapChart(buildMonthlyProfitChart(results), "Monthly Profit (RM)"));
                        chartsWrapper.add(wrapChart(buildROIChart(results), "ROI Comparison (%)"));
                        chartsWrapper.add(wrapChart(buildScoreChart(results), "Investment Score (0–100)"));
                        chartsWrapper.add(wrapChart(buildAnnualIncomeChart(results), "Annual Net Income (RM)"));
                    }
                    chartsWrapper.revalidate();
                    chartsWrapper.repaint();
                } catch (Exception ex) {
                    chartsWrapper.removeAll();
                    chartsWrapper.add(SoftTheme.bodyLabel("Chart error: " + ex.getMessage()));
                    chartsWrapper.revalidate();
                }
            }
        };
        worker.execute();
    }

    // ── Chart Builders ────────────────────────────────────────────────────────

    private JFreeChart buildMonthlyProfitChart(List<AnalysisResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (AnalysisResult r : results)
            dataset.addValue(r.getMonthlyProfit(), "Monthly Profit", shortName(r.getPropertyName()));

        JFreeChart chart = ChartFactory.createBarChart(
            null, null, "RM", dataset, PlotOrientation.VERTICAL, false, true, false);

        applyPastelBarStyle(chart, SoftTheme.CHART_BLUE);
        return chart;
    }

    private JFreeChart buildROIChart(List<AnalysisResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (AnalysisResult r : results)
            dataset.addValue(r.getRoi(), "ROI %", shortName(r.getPropertyName()));

        JFreeChart chart = ChartFactory.createBarChart(
            null, null, "%", dataset, PlotOrientation.VERTICAL, false, true, false);

        applyPastelBarStyle(chart, SoftTheme.CHART_MINT);
        return chart;
    }

    private JFreeChart buildScoreChart(List<AnalysisResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (AnalysisResult r : results)
            dataset.addValue(r.getInvestmentScore(), "Score", shortName(r.getPropertyName()));

        JFreeChart chart = ChartFactory.createLineChart(
            null, null, "Score", dataset, PlotOrientation.VERTICAL, false, true, false);

        styleLineChart(chart);
        return chart;
    }

    private JFreeChart buildAnnualIncomeChart(List<AnalysisResult> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (AnalysisResult r : results)
            dataset.addValue(r.getMonthlyProfit() * 12, "Annual Income", shortName(r.getPropertyName()));

        JFreeChart chart = ChartFactory.createBarChart(
            null, null, "RM", dataset, PlotOrientation.VERTICAL, false, true, false);

        applyPastelBarStyle(chart, SoftTheme.CHART_LAVENDER);
        return chart;
    }

    // ── Styling Helpers ───────────────────────────────────────────────────────

    private void applyPastelBarStyle(JFreeChart chart, Color barColor) {
        chart.setBackgroundPaint(SoftTheme.BG_CARD);
        chart.setBorderVisible(false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(SoftTheme.BG_CARD);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(SoftTheme.BORDER_SOFT);
        plot.setRangeGridlineStroke(new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND, 1f, new float[]{4f}, 0f));
        plot.setDomainGridlinesVisible(false);
        plot.setInsets(new org.jfree.chart.ui.RectangleInsets(10, 10, 10, 10));

        // Axis styling
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(SoftTheme.FONT_SMALL);
        domainAxis.setTickLabelPaint(SoftTheme.TEXT_SECONDARY);
        domainAxis.setAxisLineVisible(false);
        domainAxis.setTickMarksVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(SoftTheme.FONT_SMALL);
        rangeAxis.setTickLabelPaint(SoftTheme.TEXT_SECONDARY);
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setTickMarksVisible(false);

        // Bar renderer
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, barColor);
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setItemMargin(0.1);
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.12);
    }

    private void styleLineChart(JFreeChart chart) {
        chart.setBackgroundPaint(SoftTheme.BG_CARD);
        chart.setBorderVisible(false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(SoftTheme.BG_CARD);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(SoftTheme.BORDER_SOFT);
        plot.setDomainGridlinesVisible(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(SoftTheme.FONT_SMALL);
        domainAxis.setTickLabelPaint(SoftTheme.TEXT_SECONDARY);
        domainAxis.setAxisLineVisible(false);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(SoftTheme.FONT_SMALL);
        rangeAxis.setTickLabelPaint(SoftTheme.TEXT_SECONDARY);
        rangeAxis.setAxisLineVisible(false);
        rangeAxis.setRange(0, 100);

        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, SoftTheme.CHART_PEACH.darker());
        renderer.setSeriesStroke(0, new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        renderer.setDefaultShapesVisible(true);
        renderer.setDefaultFillPaint(SoftTheme.CHART_PEACH);
        renderer.setUseFillPaint(true);
    }

    private JPanel wrapChart(JFreeChart chart, String title) {
        JPanel wrapper = SoftTheme.cardPanel();
        wrapper.setLayout(new BorderLayout(0, 8));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(SoftTheme.FONT_SUBHEAD);
        titleLbl.setForeground(SoftTheme.TEXT_SECONDARY);
        wrapper.add(titleLbl, BorderLayout.NORTH);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setOpaque(false);
        chartPanel.setBackground(SoftTheme.BG_CARD);
        chartPanel.setMouseWheelEnabled(false);
        chartPanel.setPreferredSize(new Dimension(0, 280));
        wrapper.add(chartPanel, BorderLayout.CENTER);

        return wrapper;
    }

    /** Truncates long property names for axis labels. */
    private String shortName(String name) {
        return name.length() > 14 ? name.substring(0, 13) + "…" : name;
    }
}