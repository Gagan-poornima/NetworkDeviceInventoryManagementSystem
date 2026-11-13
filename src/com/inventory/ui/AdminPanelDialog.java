package com.inventory.ui;

import com.inventory.dao.DeviceDAO;
import com.inventory.dao.UserDAO;
import com.inventory.db.DBConnection;
import com.inventory.db.DatabaseInitializer;
import com.inventory.model.Session;
import com.inventory.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class AdminPanelDialog extends JDialog {
    private final UserDAO userDAO = new UserDAO();
    private final DeviceDAO deviceDAO = new DeviceDAO();

    private JTable tblUsers;
    private DefaultTableModel userTableModel;

    private JLabel lblTotalDevices;
    private JLabel lblActiveDevices;
    private JLabel lblInactiveDevices;
    private JLabel lblMaintDevices;
    private JLabel lblTotalUsers;
    private JLabel lblDbStatus;

    public AdminPanelDialog(Frame owner) {
        super(owner, "System Admin Panel", true);
        initUI();
        refreshUserData();
        refreshStatsData();
    }

    private void initUI() {
        setSize(750, 560);
        setLocationRelativeTo(getOwner());
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblTitle = new JLabel("ADMINISTRATIVE MANAGEMENT CONSOLE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(new Color(93, 173, 226));

        JLabel lblSub = new JLabel("User accounts, database status, and inventory maintenance");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(189, 195, 199));

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabbedPane.addTab("User Accounts", createUserManagementTab());
        tabbedPane.addTab("Database & Maintenance", createMaintenanceTab());
        add(tabbedPane, BorderLayout.CENTER);

        // Bottom close button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createUserManagementTab() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Users table
        String[] columns = {"ID", "Username", "Full Name", "Email", "Role"};
        userTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblUsers = new JTable(userTableModel);
        tblUsers.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblUsers.setRowHeight(24);
        tblUsers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollTable = new JScrollPane(tblUsers);
        panel.add(scrollTable, BorderLayout.CENTER);

        // Buttons toolbar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

        JButton btnAddUser = new JButton("Add User");
        btnAddUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAddUser.addActionListener(e -> showAddUserDialog());

        JButton btnChangeRole = new JButton("Change Role");
        btnChangeRole.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnChangeRole.addActionListener(e -> changeSelectedUserRole());

        JButton btnDeleteUser = new JButton("Delete User");
        btnDeleteUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());

        JButton btnRefreshUsers = new JButton("Refresh");
        btnRefreshUsers.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRefreshUsers.addActionListener(e -> refreshUserData());

        btnBar.add(btnAddUser);
        btnBar.add(btnChangeRole);
        btnBar.add(btnDeleteUser);
        btnBar.add(btnRefreshUsers);
        panel.add(btnBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createMaintenanceTab() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(18, 20, 18, 20));

        // Stats card
        JPanel statsPanel = new JPanel(new GridLayout(2, 3, 15, 12));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Live Database Metrics"));

        lblTotalDevices = new JLabel("Total Devices: —");
        lblActiveDevices = new JLabel("Active: —");
        lblInactiveDevices = new JLabel("Inactive: —");
        lblMaintDevices = new JLabel("Maintenance: —");
        lblTotalUsers = new JLabel("Total Users: —");
        lblDbStatus = new JLabel("Connection: Checking...");

        lblTotalDevices.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblActiveDevices.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInactiveDevices.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMaintDevices.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotalUsers.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));

        statsPanel.add(lblTotalDevices);
        statsPanel.add(lblActiveDevices);
        statsPanel.add(lblInactiveDevices);
        statsPanel.add(lblMaintDevices);
        statsPanel.add(lblTotalUsers);
        statsPanel.add(lblDbStatus);

        panel.add(statsPanel);
        panel.add(Box.createVerticalStrut(20));

        // Maintenance operations
        JPanel opsPanel = new JPanel();
        opsPanel.setLayout(new BoxLayout(opsPanel, BoxLayout.Y_AXIS));
        opsPanel.setBorder(BorderFactory.createTitledBorder("Administrative Inventory Actions"));

        JButton btnReseed = new JButton("Re-Seed Sample Network Devices");
        btnReseed.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReseed.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnReseed.addActionListener(e -> reseedSampleDevices());

        JLabel lblReseedHint = new JLabel("Restores standard sample Routers, Switches, Firewalls, and Servers to database.");
        lblReseedHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblReseedHint.setForeground(Color.GRAY);
        lblReseedHint.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnTestConn = new JButton("Test MySQL Database Ping");
        btnTestConn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnTestConn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTestConn.addActionListener(e -> testDatabasePing());

        opsPanel.add(btnReseed);
        opsPanel.add(lblReseedHint);
        opsPanel.add(Box.createVerticalStrut(12));
        opsPanel.add(btnTestConn);

        panel.add(opsPanel);
        return panel;
    }

    private void refreshUserData() {
        userTableModel.setRowCount(0);
        try {
            List<User> users = userDAO.getAllUsers();
            for (User u : users) {
                userTableModel.addRow(new Object[]{
                        u.getId(),
                        u.getUsername(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getRole()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load user accounts: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshStatsData() {
        try {
            Map<String, Integer> counts = deviceDAO.getStatusCounts();
            int totalUsers = userDAO.getUserCount();

            lblTotalDevices.setText("Total Devices: " + counts.getOrDefault("Total", 0));
            lblActiveDevices.setText("Active Devices: " + counts.getOrDefault("Active", 0));
            lblInactiveDevices.setText("Inactive Devices: " + counts.getOrDefault("Inactive", 0));
            lblMaintDevices.setText("Maintenance: " + counts.getOrDefault("Under Maintenance", 0));
            lblTotalUsers.setText("Registered Users: " + totalUsers);
            lblDbStatus.setText("Database: Connected (" + DBConnection.getDatabase() + ")");
            lblDbStatus.setForeground(new Color(39, 174, 96));
        } catch (SQLException ex) {
            lblDbStatus.setText("Database: Offline (" + ex.getMessage() + ")");
            lblDbStatus.setForeground(Color.RED);
        }
    }

    private void showAddUserDialog() {
        JTextField txtU = new JTextField();
        JTextField txtF = new JTextField();
        JTextField txtE = new JTextField();
        JPasswordField txtP = new JPasswordField();
        JComboBox<String> cmbRole = new JComboBox<>(new String[]{"STAFF", "ADMIN", "TECHNICIAN"});

        Object[] msg = {
                "Username:", txtU,
                "Password:", txtP,
                "Full Name:", txtF,
                "Email:", txtE,
                "Role:", cmbRole
        };

        int opt = JOptionPane.showConfirmDialog(this, msg, "Create User Account", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            String u = txtU.getText().trim();
            String p = new String(txtP.getPassword()).trim();
            String f = txtF.getText().trim();
            String em = txtE.getText().trim();
            String role = (String) cmbRole.getSelectedItem();

            if (u.isEmpty() || p.isEmpty() || f.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username, Password, and Full Name are required.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                if (userDAO.isUsernameTaken(u)) {
                    JOptionPane.showMessageDialog(this, "Username already exists.", "Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                userDAO.registerUser(new User(0, u, p, f, em, role));
                refreshUserData();
                refreshStatsData();
                JOptionPane.showMessageDialog(this, "User created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changeSelectedUserRole() {
        int row = tblUsers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) userTableModel.getValueAt(row, 0);
        String username = (String) userTableModel.getValueAt(row, 1);
        String currentRole = (String) userTableModel.getValueAt(row, 4);

        String[] roles = new String[]{"STAFF", "ADMIN", "TECHNICIAN"};
        String newRole = (String) JOptionPane.showInputDialog(this,
                "Select new role for user '" + username + "':",
                "Change User Role",
                JOptionPane.QUESTION_MESSAGE,
                null,
                roles,
                currentRole);

        if (newRole != null && !newRole.equals(currentRole)) {
            try {
                userDAO.updateUserRole(userId, newRole);
                refreshUserData();
                JOptionPane.showMessageDialog(this, "User role updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to update role: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedUser() {
        int row = tblUsers.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) userTableModel.getValueAt(row, 0);
        String username = (String) userTableModel.getValueAt(row, 1);

        if (Session.getCurrentUser() != null && Session.getCurrentUser().getId() == userId) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own currently logged-in account!", "Action Forbidden", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete user account '" + username + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                userDAO.deleteUser(userId);
                refreshUserData();
                refreshStatsData();
                JOptionPane.showMessageDialog(this, "User account deleted.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Failed to delete user: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void reseedSampleDevices() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "This will add missing standard sample devices to the database. Continue?",
                "Re-seed Sample Data",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                DatabaseInitializer.initialize();
                refreshStatsData();
                JOptionPane.showMessageDialog(this, "Database checked and sample devices verified!", "Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void testDatabasePing() {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            boolean valid = conn.isValid(3);
            if (valid) {
                JOptionPane.showMessageDialog(this,
                        "MySQL connection is active and healthy!\nDatabase: " + DBConnection.getDatabase() +
                        "\nServer: " + DBConnection.getHost() + ":" + DBConnection.getPort(),
                        "Ping Successful", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Connection is closed or invalid.", "Ping Failed", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Ping error: " + ex.getMessage(), "Connection Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}

