package com.propertyiq.presentation.theme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * TIER 1 — PRESENTATION
 * design system: color palette, fonts, border radii, shadows.
 * All UI classes pull constants from here — change once, update everywhere.
 */
public class SoftTheme {

    // ── Color Palette ─────────────────────────────────────────────────────────
    public static final Color BG_PRIMARY      = new Color(0xF8FAFC); // Ghost White
    public static final Color BG_SIDEBAR      = new Color(0xE0F2FE); // Soft Sky Blue
    public static final Color BG_CARD         = new Color(0xFFFFFF); // Pure White
    public static final Color BTN_PRIMARY     = new Color(0x93C5FD); // Baby Blue
    public static final Color BTN_SUCCESS     = new Color(0x86EFAC); // Pale Mint
    public static final Color BTN_DANGER      = new Color(0xFCA5A5); // Soft Coral
    public static final Color BTN_WARNING     = new Color(0xFCD34D); // Soft Amber
    public static final Color BTN_HOVER       = new Color(0x60A5FA); // Deeper Blue (hover state)
    public static final Color TEXT_PRIMARY    = new Color(0x334155); // Slate Gray
    public static final Color TEXT_SECONDARY  = new Color(0x64748B); // Muted Slate
    public static final Color TEXT_ACCENT     = new Color(0x0EA5E9); // Sky Blue (headers)
    public static final Color BORDER_SOFT     = new Color(0xE2E8F0); // Light border
    public static final Color HIGHLIGHT_GREEN = new Color(0xD1FAE5); // Mint bg for HIGH_PROFIT
    public static final Color HIGHLIGHT_AMBER = new Color(0xFEF3C7); // Amber bg for MODERATE
    public static final Color HIGHLIGHT_RED   = new Color(0xFEE2E2); // Rose bg for RISKY
    public static final Color CHART_BLUE      = new Color(0xBAE6FD); // Pastel chart bar
    public static final Color CHART_MINT      = new Color(0xA7F3D0); // Pastel chart line
    public static final Color CHART_LAVENDER  = new Color(0xDDD6FE); // Pastel chart accent
    public static final Color CHART_PEACH     = new Color(0xFED7AA); // Pastel chart accent 2

    // ── Typography ────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_SUBHEAD = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas", Font.PLAIN, 12);

    // ── Geometry ──────────────────────────────────────────────────────────────
    public static final int RADIUS_BUTTON = 20;
    public static final int RADIUS_CARD   = 16;
    public static final int RADIUS_FIELD  = 12;
    public static final int PADDING_CARD  = 20;
    public static final int PADDING_BTN_H = 24; // horizontal button padding
    public static final int PADDING_BTN_V =  8; // vertical button padding

    // ── Shadows ───────────────────────────────────────────────────────────────
    /** A soft drop-shadow compound border for card panels. */
    public static Border cardShadowBorder() {
        return BorderFactory.createCompoundBorder(
            new ShadowBorder(6, new Color(0, 0, 0, 18)),
            BorderFactory.createEmptyBorder(PADDING_CARD, PADDING_CARD, PADDING_CARD, PADDING_CARD)
        );
    }

    /** Simple rounded empty border for lightweight panels. */
    public static Border softBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
    }

    // ── Component Factories ───────────────────────────────────────────────────

    /** Creates a styled primary button (Baby Blue). */
    public static JButton primaryButton(String text) {
        return styledButton(text, BTN_PRIMARY, TEXT_PRIMARY);
    }

    /** Creates a styled success/calculate button (Pale Mint). */
    public static JButton successButton(String text) {
        return styledButton(text, BTN_SUCCESS, TEXT_PRIMARY);
    }

    /** Creates a styled danger button (Soft Coral). */
    public static JButton dangerButton(String text) {
        return styledButton(text, BTN_DANGER, TEXT_PRIMARY);
    }

    /** Creates a styled warning button (Soft Amber). */
    public static JButton warningButton(String text) {
        return styledButton(text, BTN_WARNING, TEXT_PRIMARY);
    }

    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                    ? bg.darker()
                    : getModel().isRollover() ? bg.brighter() : bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS_BUTTON, RADIUS_BUTTON));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_SUBHEAD);
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(
            PADDING_BTN_V, PADDING_BTN_H, PADDING_BTN_V, PADDING_BTN_H));
        return btn;
    }

    /** Creates a styled rounded text field. */
    public static JTextField styledField(int columns) {
        JTextField field = new JTextField(columns);
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_CARD);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    /** Creates a styled rounded password field. */
    public static JPasswordField styledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setBackground(BG_CARD);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_SOFT, 1, true),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return field;
    }

    /** Creates a card panel with white background, rounded corners, and a shadow. */
    public static JPanel cardPanel() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(cardShadowBorder());
        return card;
    }

    /** Creates a label styled as a section heading. */
    public static JLabel headingLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(TEXT_ACCENT);
        return lbl;
    }

    /** Creates a normal body label. */
    public static JLabel bodyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    // ── Inner Shadow Border ───────────────────────────────────────────────────

    /** Lightweight drop shadow painted outside the component boundary. */
    public static class ShadowBorder extends AbstractBorder {
        private final int   blurRadius;
        private final Color shadowColor;

        public ShadowBorder(int blurRadius, Color shadowColor) {
            this.blurRadius  = blurRadius;
            this.shadowColor = shadowColor;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(blurRadius, blurRadius, blurRadius + 2, blurRadius + 2);
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = blurRadius; i >= 1; i--) {
                int alpha = (int)(18.0 * (1.0 - (double) i / blurRadius));
                g2.setColor(new Color(
                    shadowColor.getRed(), shadowColor.getGreen(),
                    shadowColor.getBlue(), Math.max(0, alpha)));
                g2.drawRoundRect(x + i, y + i, w - i * 2, h - i * 2,
                    RADIUS_CARD, RADIUS_CARD);
            }
            g2.dispose();
        }
    }
}