package com.inventory.ui;

import com.inventory.dao.UserDAO;
import com.inventory.model.Session;
import com.inventory.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;

public class LoginFrame extends JFrame {
    private final UserDAO userDAO = new UserDAO();

    private JTextField txtLoginUser;
    private JPasswordField txtLoginPass;

    private JTextField txtRegFullName;
    private JTextField txtRegUsername;
    private JTextField txtRegEmail;
    private JPasswordField txtRegPassword;
    private JComboBox<String> cmbRegRole;

    public LoginFrame() {
        super("Network Device Inventory Management System - Login");
        initUI();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(22, 20, 22, 20));

        JLabel lblTitle = new JLabel("NETWORK DEVICE INVENTORY", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(93, 173, 226));

        JLabel lblSub = new JLabel("Infrastructure Asset Management & Monitoring", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(189, 195, 199));

        headerPanel.add(lblTitle, BorderLayout.CENTER);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.addTab("Sign In", createLoginTab());
        tabbedPane.addTab("Register New Account", createRegisterTab());
        add(tabbedPane, BorderLayout.CENTER);

        // Footer Panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(245, 247, 248));
        footerPanel.setBorder(new EmptyBorder(8, 15, 8, 15));

        JLabel lblVer = new JLabel("v1.0 • MySQL Connected");
        lblVer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVer.setForeground(Color.GRAY);

        JButton btnDbSettings = new JButton("Database Settings");
        btnDbSettings.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDbSettings.setFocusPainted(false);
        btnDbSettings.addActionListener(e -> {
            DBConfigDialog dlg = new DBConfigDialog(this);
            dlg.setVisible(true);
        });

        footerPanel.add(lblVer, BorderLayout.WEST);
        footerPanel.add(btnDbSettings, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JPanel createLoginTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(25, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 6, 8, 6);

        txtLoginUser = new JTextField(15);
        txtLoginUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtLoginPass = new JPasswordField(15);
        txtLoginPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Default admin credentials for instant testing
        txtLoginUser.setText("admin");
        txtLoginPass.setText("admin123");

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lblUser, gbc);

        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.7;
        panel.add(txtLoginUser, gbc);

        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(lblPass, gbc);

        gbc.gridx = 1; gbc.gridy = row++; gbc.weightx = 0.7;
        panel.add(txtLoginPass, gbc);

        // Hint label
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel lblHint = new JLabel("Default Admin: admin / admin123 | Tech: tech1 / tech123");
        lblHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblHint.setForeground(new Color(127, 140, 141));
        panel.add(lblHint, gbc);

        // Sign In Button
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 6, 8, 6);
        JButton btnLogin = new JButton("Sign In");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(41, 128, 185));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setPreferredSize(new Dimension(100, 38));
        btnLogin.addActionListener(e -> performLogin());
        panel.add(btnLogin, gbc);

        // Enter key submits form
        txtLoginPass.addActionListener(e -> performLogin());
        txtLoginUser.addActionListener(e -> performLogin());

        return panel;
    }

    private JPanel createRegisterTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 30, 15, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 6, 5, 6);

        txtRegFullName = new JTextField(15);
        txtRegFullName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtRegUsername = new JTextField(15);
        txtRegUsername.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtRegEmail = new JTextField(15);
        txtRegEmail.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtRegPassword = new JPasswordField(15);
        txtRegPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        cmbRegRole = new JComboBox<>(new String[]{"STAFF", "ADMIN", "TECHNICIAN"});
        cmbRegRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        int row = 0;
        addRegField(panel, gbc, row++, "Full Name:", txtRegFullName);
        addRegField(panel, gbc, row++, "Username:", txtRegUsername);
        addRegField(panel, gbc, row++, "Email:", txtRegEmail);
        addRegField(panel, gbc, row++, "Password:", txtRegPassword);
        addRegField(panel, gbc, row++, "Account Role:", cmbRegRole);

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 6, 6);
        JButton btnRegister = new JButton("Create Account");
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegister.setBackground(new Color(39, 174, 96));
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setFocusPainted(false);
        btnRegister.setPreferredSize(new Dimension(100, 36));
        btnRegister.addActionListener(e -> performRegister());
        panel.add(btnRegister, gbc);

        return panel;
    }

    private void addRegField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    private void performLogin() {
        String username = txtLoginUser.getText().trim();
        String password = new String(txtLoginPass.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = userDAO.authenticate(username, password);
            if (user != null) {
                Session.setCurrentUser(user);
                MainDashboardFrame dashboard = new MainDashboardFrame();
                dashboard.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password. Please verify credentials.",
                        "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database Error during login: " + ex.getMessage() +
                    "\nPlease check 'Database Settings' at the bottom of the window.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performRegister() {
        String fullName = txtRegFullName.getText().trim();
        String username = txtRegUsername.getText().trim();
        String email = txtRegEmail.getText().trim();
        String password = new String(txtRegPassword.getPassword()).trim();
        String role = (String) cmbRegRole.getSelectedItem();

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Full Name, Username, and Password are required.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (username.length() < 3) {
            JOptionPane.showMessageDialog(this,
                    "Username must be at least 3 characters long.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (userDAO.isUsernameTaken(username)) {
                JOptionPane.showMessageDialog(this,
                        "Username '" + username + "' is already in use. Please choose another.",
                        "Registration Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            User newUser = new User(0, username, password, fullName, email, role);
            boolean created = userDAO.registerUser(newUser);
            if (created) {
                JOptionPane.showMessageDialog(this,
                        "Registration successful! You may now sign in with your credentials.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                txtLoginUser.setText(username);
                txtLoginPass.setText("");
                txtLoginPass.requestFocus();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to register account: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

