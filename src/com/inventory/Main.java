package com.inventory;

import com.inventory.db.DatabaseInitializer;
import com.inventory.ui.LoginFrame;

import javax.swing.*;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        // Set modern Look and Feel
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {}
        }

        // Initialize database and tables automatically
        try {
            DatabaseInitializer.initialize();
            System.out.println("Network Device Inventory database and tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Warning: Could not connect to MySQL at startup: " + e.getMessage());
            System.err.println("Users can configure connection parameters via the GUI 'Database Settings' option.");
        }

        // Launch UI on Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}

