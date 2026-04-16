package com.propertyiq.presentation.screens;

import com.propertyiq.presentation.theme.SoftTheme;
import com.propertyiq.service.AuthService;
import com.propertyiq.service.AuthService.AuthException;
import com.propertyiq.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.SQLException;

/**
 * TIER 1 — PRESENTATION
 * Login Screen — entry point of the application.
 * Validates credentials via AuthService (BCrypt-backed).
 */
public class LoginScreen extends JFrame {

    private JTextField     usernameField;
    private JPasswordField passwordField;
    private JLabel         errorLabel;
    private JButton        loginButton;

    public LoginScreen() {
        initUI();
    }

    private void initUI() {
        setTitle("PropertyIQ — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true); // frameless for clean look
        setShape(new RoundRectangle2D.Double(0, 0, 460, 560, 30, 30));

        // ── Root panel ──────────────────────────────────────────────────────
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SoftTheme.BG_PRIMARY);
        root.setBorder(BorderFactory.createLineBorder(SoftTheme.BORDER_SOFT, 1, true));
        setContentPane(root);

        // ── Drag-to-move (since frameless) ───────────────────────────────
        final Point[] dragStart = {null};
        root.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { dragStart[0] = e.getPoint(); }
        });
        root.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (dragStart[0] != null) {
                    Point loc = getLocation();
                    setLocation(loc.x + e.getX() - dragStart[0].x,
                                loc.y + e.getY() - dragStart[0].y);
                }
            }
        });

        // ── Top bar (close button) ───────────────────────────────────────
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        topBar.setOpaque(false);
        JButton closeBtn = new JButton("✕");
        closeBtn.setFont(SoftTheme.FONT_SMALL);
        closeBtn.setForeground(SoftTheme.TEXT_SECONDARY);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));
        topBar.add(closeBtn);
        root.add(topBar, BorderLayout.NORTH);

        // ── Center card ──────────────────────────────────────────────────
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        root.add(centerWrapper, BorderLayout.CENTER);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(SoftTheme.BG_CARD);
        card.setBorder(new EmptyBorder(40, 44, 40, 44));

        // Logo / Icon
        JLabel logoLabel = new JLabel("🏠");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(logoLabel);
        card.add(Box.createVerticalStrut(12));

        // Title
        JLabel titleLabel = new JLabel("PropertyIQ");
        titleLabel.setFont(SoftTheme.FONT_TITLE);
        titleLabel.setForeground(SoftTheme.TEXT_ACCENT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Investment Analysis System");
        subtitleLabel.setFont(SoftTheme.FONT_SMALL);
        subtitleLabel.setForeground(SoftTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(subtitleLabel);
        card.add(Box.createVerticalStrut(32));

        // Username
        JLabel userLbl = SoftTheme.bodyLabel("Username");
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(userLbl);
        card.add(Box.createVerticalStrut(6));
        usernameField = SoftTheme.styledField(22);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(usernameField);
        card.add(Box.createVerticalStrut(16));

        // Password
        JLabel passLbl = SoftTheme.bodyLabel("Password");
        passLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passLbl);
        card.add(Box.createVerticalStrut(6));
        passwordField = SoftTheme.styledPasswordField(22);
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(passwordField);
        card.add(Box.createVerticalStrut(10));

        // Error label (hidden until needed)
        errorLabel = new JLabel(" ");
        errorLabel.setFont(SoftTheme.FONT_SMALL);
        errorLabel.setForeground(new Color(0xEF4444));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(20));

        // Login button
        loginButton = SoftTheme.primaryButton("Sign In");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginButton.addActionListener(e -> handleLogin());
        card.add(loginButton);
        card.add(Box.createVerticalStrut(16));

        // Hint text
        JLabel hintLabel = new JLabel("Default admin: admin / admin123");
        hintLabel.setFont(SoftTheme.FONT_SMALL);
        hintLabel.setForeground(SoftTheme.TEXT_SECONDARY);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(hintLabel);

        // Enter key triggers login
        passwordField.addActionListener(e -> handleLogin());
        usernameField.addActionListener(e -> passwordField.requestFocus());

        // Shadow wrapper
        JPanel shadowWrap = SoftTheme.cardPanel();
        shadowWrap.setLayout(new BorderLayout());
        shadowWrap.add(card, BorderLayout.CENTER);

        centerWrapper.add(shadowWrap, new GridBagConstraints());
        setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Signing in...");

        // Run in background to avoid EDT freeze
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() throws Exception {
                return new AuthService().login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    dispose();
                    new DashboardScreen(user);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof AuthException) {
                        showError(cause.getMessage());
                    } else if (cause instanceof SQLException) {
                        showError("Database error. Please check connection.");
                    } else {
                        showError("An unexpected error occurred.");
                    }
                } finally {
                    loginButton.setEnabled(true);
                    loginButton.setText("Sign In");
                    passwordField.setText("");
                }
            }
        };
        worker.execute();
    }

    private void showError(String message) {
        errorLabel.setText("⚠ " + message);
    }
}