package com.lighthouse.presentation.screens;

import com.lighthouse.model.User;
import com.lighthouse.presentation.theme.SoftTheme;
import com.lighthouse.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

/**
 * TIER 1 — PRESENTATION
 * Dashboard — main application shell.
 * Houses the sidebar navigation and a content panel that swaps screens.
 * Uses CardLayout for smooth panel transitions.
 */
public class DashboardScreen extends JFrame {

    private final User          currentUser;
    private final CardLayout    cardLayout;
    private final JPanel        contentArea;

    // Screen references (lazy-initialised when first navigated to)
    private PropertyListScreen  propertyListScreen;
    private PropertyFormScreen  propertyFormScreen;
    private AnalysisScreen      analysisScreen;
    private GraphScreen         graphScreen;
    private SearchScreen        searchScreen;

    // Navigation buttons (kept as fields for active-state styling)
    private JButton btnProperties, btnAdd, btnAnalysis, btnGraphs, btnSearch;

    public DashboardScreen(User user) {
        this.currentUser = user;
        this.cardLayout  = new CardLayout();
        this.contentArea = new JPanel(cardLayout);
        initUI();
    }

    private void initUI() {
        setTitle("Lighthouse — Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        getContentPane().setBackground(SoftTheme.BG_PRIMARY);

        add(buildSidebar(),    BorderLayout.WEST);
        add(buildTopBar(),     BorderLayout.NORTH);
        add(buildContentArea(),BorderLayout.CENTER);

        // Show properties by default
        navigateTo("PROPERTIES");
        setVisible(true);
    }

    // ── Sidebar ──────────────────────────────────────────────────────────────

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SoftTheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        // App logo + name
        JLabel logoIcon = new JLabel("🏠");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("Lighthouse");
        appName.setFont(SoftTheme.FONT_HEADING);
        appName.setForeground(SoftTheme.TEXT_ACCENT);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        sidebar.add(logoIcon);
        sidebar.add(Box.createVerticalStrut(4));
        sidebar.add(appName);
        sidebar.add(Box.createVerticalStrut(4));

        // Role badge
        JLabel roleBadge = new JLabel(currentUser.getRole().name());
        roleBadge.setFont(SoftTheme.FONT_SMALL);
        roleBadge.setForeground(SoftTheme.TEXT_SECONDARY);
        roleBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(roleBadge);
        sidebar.add(Box.createVerticalStrut(28));

        // Divider
        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(16));

        // Nav section label
        sidebar.add(sectionLabel("NAVIGATION"));
        sidebar.add(Box.createVerticalStrut(8));

        // Nav buttons
        btnProperties = navButton("📋  Properties", "PROPERTIES");
        sidebar.add(btnProperties);
        sidebar.add(Box.createVerticalStrut(6));

        // Only admins can add/modify properties
        if (currentUser.isAdmin()) {
            btnAdd = navButton("➕  Add Property", "ADD_PROPERTY");
            sidebar.add(btnAdd);
            sidebar.add(Box.createVerticalStrut(6));
        }

        btnAnalysis = navButton("📊  Analysis", "ANALYSIS");
        sidebar.add(btnAnalysis);
        sidebar.add(Box.createVerticalStrut(6));

        btnGraphs = navButton("📈  Graphs", "GRAPHS");
        sidebar.add(btnGraphs);
        sidebar.add(Box.createVerticalStrut(6));

        btnSearch = navButton("🔍  Search", "SEARCH");
        sidebar.add(btnSearch);

        // Spacer pushes logout to the bottom
        sidebar.add(Box.createVerticalGlue());

        // User info + logout
        sidebar.add(makeDivider());
        sidebar.add(Box.createVerticalStrut(12));
        JLabel userLabel = new JLabel("👤 " + currentUser.getUsername());
        userLabel.setFont(SoftTheme.FONT_SMALL);
        userLabel.setForeground(SoftTheme.TEXT_SECONDARY);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidebar.add(userLabel);
        sidebar.add(Box.createVerticalStrut(8));

        JButton logoutBtn = SoftTheme.dangerButton("Logout");
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(180, 36));
        logoutBtn.addActionListener(e -> {
            new AuthService().logout();
            dispose();
            new LoginScreen();
        });
        sidebar.add(logoutBtn);

        return sidebar;
    }

    // ── Top Bar ───────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(SoftTheme.BG_PRIMARY);
        topBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, SoftTheme.BORDER_SOFT),
            new EmptyBorder(14, 24, 14, 24)
        ));
        topBar.setPreferredSize(new Dimension(0, 60));

        JLabel pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(SoftTheme.FONT_TITLE);
        pageTitle.setForeground(SoftTheme.TEXT_PRIMARY);
        pageTitle.setName("pageTitle");

        JLabel dateLabel = new JLabel(java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLabel.setFont(SoftTheme.FONT_SMALL);
        dateLabel.setForeground(SoftTheme.TEXT_SECONDARY);

        topBar.add(pageTitle, BorderLayout.WEST);
        topBar.add(dateLabel, BorderLayout.EAST);

        return topBar;
    }

    // ── Content Area ─────────────────────────────────────────────────────────

    private JPanel buildContentArea() {
        contentArea.setBackground(SoftTheme.BG_PRIMARY);
        contentArea.setBorder(new EmptyBorder(24, 24, 24, 24));
        return contentArea;
    }

    // ── Navigation Logic ─────────────────────────────────────────────────────

    public void navigateTo(String screen) {
        if (!contentArea.isAncestorOf(getCard(screen))) {
            contentArea.add(getCard(screen), screen);
        }
        cardLayout.show(contentArea, screen);
        updateNavActiveState(screen);
    }

    private JComponent getCard(String screen) {
        return switch (screen) {
            case "PROPERTIES"  -> {
                if (propertyListScreen == null)
                    propertyListScreen = new PropertyListScreen(this, currentUser);
                else propertyListScreen.refresh();
                yield propertyListScreen;
            }
            case "ADD_PROPERTY" -> {
                propertyFormScreen = new PropertyFormScreen(this, currentUser, null);
                yield propertyFormScreen;
            }
            case "ANALYSIS" -> {
                if (analysisScreen == null) {
                    analysisScreen = new AnalysisScreen(this, currentUser);
                } else {
                    analysisScreen.refresh();
                }
                yield analysisScreen;
            }
            case "GRAPHS" -> {
                if (graphScreen == null) {
                    graphScreen = new GraphScreen(this);
                } else {
                    graphScreen.refresh();
                }
                yield graphScreen;
            }
            case "SEARCH" -> {
                if (searchScreen == null)
                    searchScreen = new SearchScreen(this, currentUser);
                yield searchScreen;
            }
            default -> new JLabel("Screen not found: " + screen);
        };
    }

    private void updateNavActiveState(String active) {
        JButton[] btns = { btnProperties, btnAdd, btnAnalysis, btnGraphs, btnSearch };
        String[]  keys = { "PROPERTIES", "ADD_PROPERTY", "ANALYSIS", "GRAPHS", "SEARCH" };
        for (int i = 0; i < btns.length; i++) {
            if (btns[i] == null) continue;
            boolean isActive = keys[i].equals(active);
            btns[i].setBackground(isActive ? SoftTheme.BTN_PRIMARY : SoftTheme.BG_SIDEBAR);
            btns[i].setForeground(isActive ? SoftTheme.TEXT_PRIMARY : SoftTheme.TEXT_SECONDARY);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private JButton navButton(String text, String screenKey) {
        JButton btn = new JButton(text);
        btn.setFont(SoftTheme.FONT_BODY);
        btn.setForeground(SoftTheme.TEXT_SECONDARY);
        btn.setBackground(SoftTheme.BG_SIDEBAR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> navigateTo(screenKey));
        return btn;
    }

    private JSeparator makeDivider() {
        JSeparator sep = new JSeparator();
        sep.setForeground(SoftTheme.BORDER_SOFT);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(SoftTheme.TEXT_SECONDARY);
        lbl.setBorder(new EmptyBorder(0, 14, 0, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    public User getCurrentUser() { return currentUser; }
}