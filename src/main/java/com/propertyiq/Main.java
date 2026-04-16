package com.propertyiq;

import com.formdev.flatlaf.FlatLightLaf;
import com.propertyiq.database.DatabaseConnection;
import com.propertyiq.presentation.screens.LoginScreen;
import com.propertyiq.presentation.theme.SoftTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Application entry point.
 * Bootstraps FlatLaf, applies the Soft theme overrides,
 * verifies the DB connection, then launches the Login screen.
 */
public class Main {

    public static void main(String[] args) {
        // Install FlatLaf before any Swing component is created
        FlatLightLaf.setup();
 
        UIManager.put("Button.arc",               SoftTheme.RADIUS_BUTTON);
        UIManager.put("TextComponent.arc",        SoftTheme.RADIUS_FIELD);
        UIManager.put("Component.arc",            SoftTheme.RADIUS_CARD);
        UIManager.put("ScrollBar.thumbArc",       999); // fully rounded scrollbar thumb
        UIManager.put("ScrollBar.thumbInsets",    new Insets(2, 2, 2, 2));

        UIManager.put("Panel.background",         SoftTheme.BG_PRIMARY);
        UIManager.put("Label.foreground",         SoftTheme.TEXT_PRIMARY);
        UIManager.put("TextField.background",     SoftTheme.BG_CARD);
        UIManager.put("TextField.foreground",     SoftTheme.TEXT_PRIMARY);
        UIManager.put("ComboBox.background",      SoftTheme.BG_CARD);
        UIManager.put("Table.background",         SoftTheme.BG_CARD);
        UIManager.put("Table.alternateRowColor",  new Color(0xF1F5F9));
        UIManager.put("ScrollPane.background",    SoftTheme.BG_PRIMARY);
        UIManager.put("Separator.foreground",     SoftTheme.BORDER_SOFT);
        UIManager.put("ProgressBar.arc",          8);

        // Font overrides
        UIManager.put("defaultFont",    SoftTheme.FONT_BODY);
        UIManager.put("Button.font",    SoftTheme.FONT_SUBHEAD);
        UIManager.put("Label.font",     SoftTheme.FONT_BODY);
        UIManager.put("TextField.font", SoftTheme.FONT_BODY);
        UIManager.put("Table.font",     SoftTheme.FONT_BODY);
        UIManager.put("TableHeader.font", SoftTheme.FONT_SUBHEAD);

        // ── Verify DB connection before launching UI ─────────────────────
        SwingUtilities.invokeLater(() -> {
            try {
                // This will throw RuntimeException if DB is unreachable
                DatabaseConnection.getInstance().getConnection();
                new LoginScreen();
            } catch (RuntimeException e) {
                JOptionPane.showMessageDialog(null,
                    "<html><b>Cannot connect to the database.</b><br><br>"
                    + "Please ensure:<br>"
                    + "• MySQL is running on localhost:3306<br>"
                    + "• Database 'propertyiq_db' exists (run database_schema.sql)<br>"
                    + "• Credentials in DatabaseConnection.java are correct<br><br>"
                    + "<i>Error: " + e.getCause().getMessage() + "</i></html>",
                    "Database Connection Failed",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}