package com.inventory.ui;

import com.inventory.db.DBConnection;
import com.inventory.db.DatabaseInitializer;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class DBConfigDialog extends JDialog {
    private JTextField txtHost;
    private JTextField txtPort;
    private JTextField txtDatabase;
    private JTextField txtUser;
    private JPasswordField txtPassword;
    private JLabel lblStatus;
    private boolean connectedSuccessfully = false;

    public DBConfigDialog(Frame owner) {
        super(owner, "Database Connection Settings", true);
        initUI();
    }

    private void initUI() {
        setSize(440, 390);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel lblTitle = new JLabel("MySQL Database Configuration");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        JLabel lblSub = new JLabel("Configure network_device_inventory connection");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(200, 215, 235));
        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(16, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtHost = new JTextField(DBConnection.getHost(), 15);
        txtPort = new JTextField(String.valueOf(DBConnection.getPort()), 15);
        txtDatabase = new JTextField(DBConnection.getDatabase(), 15);
        txtUser = new JTextField(DBConnection.getUser(), 15);
        txtPassword = new JPasswordField(DBConnection.getPassword(), 15);

        int row = 0;
        addFormField(formPanel, gbc, row++, "Host:", txtHost);
        addFormField(formPanel, gbc, row++, "Port:", txtPort);
        addFormField(formPanel, gbc, row++, "Database Name:", txtDatabase);
        addFormField(formPanel, gbc, row++, "Username:", txtUser);
        addFormField(formPanel, gbc, row++, "Password:", txtPassword);

        lblStatus = new JLabel("Ready to connect", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblStatus.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(lblStatus, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        JButton btnTest = new JButton("Test Connection");
        btnTest.addActionListener(e -> testConnection());

        JButton btnSave = new JButton("Save & Initialize");
        btnSave.setBackground(new Color(41, 128, 185));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveAndApply());

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnTest);
        btnPanel.add(btnSave);
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(comp, gbc);
    }

    private void testConnection() {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();
        String db = txtDatabase.getText().trim();
        String user = txtUser.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        if (host.isEmpty() || portStr.isEmpty() || user.isEmpty()) {
            lblStatus.setText("Host, Port, and Username cannot be empty.");
            lblStatus.setForeground(Color.RED);
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            lblStatus.setText("Port must be a valid integer.");
            lblStatus.setForeground(Color.RED);
            return;
        }

        lblStatus.setText("Testing connection...");
        lblStatus.setForeground(Color.BLUE);

        SwingUtilities.invokeLater(() -> {
            try {
                boolean ok = DBConnection.testConnection(host, port, db, user, pass);
                if (ok) {
                    lblStatus.setText("✓ Connection successful!");
                    lblStatus.setForeground(new Color(39, 174, 96));
                } else {
                    lblStatus.setText("✗ Could not connect.");
                    lblStatus.setForeground(Color.RED);
                }
            } catch (SQLException ex) {
                lblStatus.setText("✗ Error: " + ex.getMessage());
                lblStatus.setForeground(Color.RED);
            }
        });
    }

    private void saveAndApply() {
        String host = txtHost.getText().trim();
        String portStr = txtPort.getText().trim();
        String db = txtDatabase.getText().trim();
        String user = txtUser.getText().trim();
        String pass = new String(txtPassword.getPassword()).trim();

        int port = 3306;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ignored) {}

        DBConnection.saveConfig(host, port, db, user, pass);

        try {
            DatabaseInitializer.initialize();
            connectedSuccessfully = true;
            JOptionPane.showMessageDialog(this,
                    "Database settings saved and tables initialized successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to initialize database: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConnectedSuccessfully() {
        return connectedSuccessfully;
    }
}

