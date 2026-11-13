package com.inventory.ui;

import com.inventory.dao.DeviceDAO;
import com.inventory.db.DBConnection;
import com.inventory.model.Device;
import com.inventory.model.Session;
import com.inventory.model.User;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainDashboardFrame extends JFrame {
    private final DeviceDAO deviceDAO = new DeviceDAO();

    // Table and Data
    private JTable tblDevices;
    private DefaultTableModel tableModel;
    private List<Device> currentDeviceList = new ArrayList<>();
    private Device selectedDevice = null;

    // KPI Summary Labels
    private JLabel lblTotalCount;
    private JLabel lblActiveCount;
    private JLabel lblInactiveCount;
    private JLabel lblMaintCount;

    // Search and Filter Controls
    private JTextField txtSearch;
    private JComboBox<String> cmbTypeFilter;
    private JComboBox<String> cmbStatusFilter;

    // Right Sidebar Detail Components
    private JLabel lblPreviewName;
    private JLabel lblPreviewCode;
    private JLabel lblPreviewType;
    private JLabel lblPreviewIp;
    private JLabel lblPreviewMac;
    private JLabel lblPreviewModel;
    private JLabel lblPreviewLocation;
    private JLabel lblPreviewAssigned;
    private JLabel lblPreviewStatus;
    private JLabel lblPreviewWarranty;

    // Action Buttons
    private JButton btnViewDetails;
    private JButton btnEditDevice;
    private JButton btnDeleteDevice;
    private JButton btnSetActive;
    private JButton btnSetMaint;
    private JButton btnSetInactive;

    // Status bar
    private JLabel lblStatusText;

    public MainDashboardFrame() {
        super("Network Device Inventory Management System - Dashboard");
        initUI();
        refreshAllData();
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Header with branding and session info
        add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Content (KPIs + Search/Filter Bar + Split Table/Sidebar)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBorder(new EmptyBorder(12, 16, 8, 16));

        // Sub-top: KPI Cards + Search Bar
        JPanel topControlsPanel = new JPanel(new BorderLayout(0, 10));
        topControlsPanel.add(createKpiPanel(), BorderLayout.NORTH);
        topControlsPanel.add(createSearchFilterBar(), BorderLayout.SOUTH);
        centerPanel.add(topControlsPanel, BorderLayout.NORTH);

        // Center Split: Left = Device Table, Right = Detail/Action Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createTablePanel(), createRightActionPanel());
        splitPane.setDividerLocation(840);
        splitPane.setResizeWeight(0.72);
        splitPane.setContinuousLayout(true);
        centerPanel.add(splitPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Status Bar
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 43, 73));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Brand Title
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel lblLogo = new JLabel("⚡");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblLogo.setForeground(new Color(93, 173, 226));

        JPanel titleTextPanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titleTextPanel.setOpaque(false);
        JLabel lblTitle = new JLabel("NETWORK DEVICE INVENTORY MANAGEMENT SYSTEM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel("Hardware Assets, Network Topology & Device Lifecycle Tracker");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(189, 195, 199));

        titleTextPanel.add(lblTitle);
        titleTextPanel.add(lblSub);
        brandPanel.add(lblLogo);
        brandPanel.add(titleTextPanel);

        // Right side user & action bar
        JPanel rightBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightBar.setOpaque(false);

        User curUser = Session.getCurrentUser();
        String userDisplay = curUser != null ? curUser.getFullName() + " (" + curUser.getRole() + ")" : "Administrator";
        JLabel lblUser = new JLabel("👤 " + userDisplay);
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUser.setForeground(new Color(230, 235, 245));

        JButton btnAdmin = new JButton("Admin Panel");
        btnAdmin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAdmin.setFocusPainted(false);
        btnAdmin.addActionListener(e -> {
            AdminPanelDialog adminDlg = new AdminPanelDialog(this);
            adminDlg.setVisible(true);
            refreshAllData();
        });

        JButton btnDb = new JButton("DB Config");
        btnDb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDb.setFocusPainted(false);
        btnDb.addActionListener(e -> {
            DBConfigDialog dlg = new DBConfigDialog(this);
            dlg.setVisible(true);
            refreshAllData();
        });

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> performLogout());

        rightBar.add(lblUser);
        rightBar.add(btnAdmin);
        rightBar.add(btnDb);
        rightBar.add(btnLogout);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(rightBar, BorderLayout.EAST);
        return header;
    }

    private JPanel createKpiPanel() {
        JPanel kpiPanel = new JPanel(new GridLayout(1, 4, 12, 0));

        lblTotalCount = new JLabel("0", SwingConstants.CENTER);
        lblActiveCount = new JLabel("0", SwingConstants.CENTER);
        lblInactiveCount = new JLabel("0", SwingConstants.CENTER);
        lblMaintCount = new JLabel("0", SwingConstants.CENTER);

        kpiPanel.add(createCard("TOTAL DEVICES", lblTotalCount, new Color(41, 128, 185), new Color(235, 245, 251)));
        kpiPanel.add(createCard("ACTIVE DEVICES", lblActiveCount, new Color(39, 174, 96), new Color(234, 250, 234)));
        kpiPanel.add(createCard("INACTIVE DEVICES", lblInactiveCount, new Color(127, 140, 141), new Color(242, 244, 244)));
        kpiPanel.add(createCard("UNDER MAINTENANCE", lblMaintCount, new Color(230, 126, 34), new Color(254, 249, 231)));

        return kpiPanel;
    }

    private JPanel createCard(String title, JLabel countLabel, Color accentColor, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(bgColor);
        card.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 1, true),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel lblTitle = new JLabel(title, SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(accentColor.darker());

        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        countLabel.setForeground(accentColor.darker());

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSearchFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setBackground(new Color(248, 249, 250));
        bar.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 225, 230)),
                new EmptyBorder(6, 10, 6, 10)
        ));

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));

        txtSearch = new JTextField(16);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtSearch.setToolTipText("Search name, IP, MAC, model, location, or code...");
        txtSearch.addActionListener(e -> applyFilter());

        JButton btnSearch = new JButton("Search");
        btnSearch.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSearch.setFocusPainted(false);
        btnSearch.addActionListener(e -> applyFilter());

        JLabel lblType = new JLabel("Type:");
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbTypeFilter = new JComboBox<>(new String[]{"All Types", "Router", "Switch", "Server", "Firewall", "Access Point"});
        cmbTypeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbTypeFilter.addActionListener(e -> applyFilter());

        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbStatusFilter = new JComboBox<>(new String[]{"All Statuses", "Active", "Inactive", "Under Maintenance"});
        cmbStatusFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbStatusFilter.addActionListener(e -> applyFilter());

        JButton btnReset = new JButton("Reset");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReset.setFocusPainted(false);
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbTypeFilter.setSelectedIndex(0);
            cmbStatusFilter.setSelectedIndex(0);
            applyFilter();
        });

        JButton btnAddTop = new JButton("+ Add Device");
        btnAddTop.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddTop.setBackground(new Color(39, 174, 96));
        btnAddTop.setForeground(Color.WHITE);
        btnAddTop.setFocusPainted(false);
        btnAddTop.addActionListener(e -> showAddDeviceDialog());

        bar.add(lblSearch);
        bar.add(txtSearch);
        bar.add(btnSearch);
        bar.add(Box.createHorizontalStrut(10));
        bar.add(lblType);
        bar.add(cmbTypeFilter);
        bar.add(lblStatus);
        bar.add(cmbStatusFilter);
        bar.add(btnReset);
        bar.add(Box.createHorizontalStrut(15));
        bar.add(btnAddTop);

        return bar;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] headers = {
                "ID", "Device Code", "Device Name", "Type", "IP Address",
                "MAC Address", "Manufacturer", "Model", "Location", "Assigned User", "Status"
        };

        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblDevices = new JTable(tableModel);
        tblDevices.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblDevices.setRowHeight(26);
        tblDevices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblDevices.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tblDevices.getTableHeader().setBackground(new Color(235, 240, 245));

        // Column width hints
        tblDevices.getColumnModel().getColumn(0).setPreferredWidth(35); // ID
        tblDevices.getColumnModel().getColumn(1).setPreferredWidth(95); // Code
        tblDevices.getColumnModel().getColumn(2).setPreferredWidth(140); // Name
        tblDevices.getColumnModel().getColumn(3).setPreferredWidth(85); // Type
        tblDevices.getColumnModel().getColumn(4).setPreferredWidth(100); // IP
        tblDevices.getColumnModel().getColumn(5).setPreferredWidth(120); // MAC
        tblDevices.getColumnModel().getColumn(6).setPreferredWidth(85); // Maker
        tblDevices.getColumnModel().getColumn(7).setPreferredWidth(100); // Model
        tblDevices.getColumnModel().getColumn(8).setPreferredWidth(120); // Location
        tblDevices.getColumnModel().getColumn(9).setPreferredWidth(100); // User
        tblDevices.getColumnModel().getColumn(10).setPreferredWidth(90); // Status

        // Status column badge renderer
        tblDevices.getColumnModel().getColumn(10).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null && !isSelected) {
                    String st = value.toString();
                    if ("Active".equalsIgnoreCase(st)) {
                        c.setForeground(new Color(25, 135, 84));
                        c.setBackground(new Color(235, 248, 240));
                    } else if ("Inactive".equalsIgnoreCase(st)) {
                        c.setForeground(new Color(108, 117, 125));
                        c.setBackground(new Color(245, 245, 245));
                    } else {
                        c.setForeground(new Color(211, 84, 0));
                        c.setBackground(new Color(254, 249, 231));
                    }
                } else if (isSelected) {
                    c.setForeground(table.getSelectionForeground());
                    c.setBackground(table.getSelectionBackground());
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });

        // Row selection listener
        tblDevices.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tblDevices.getSelectedRow();
                if (row >= 0 && row < currentDeviceList.size()) {
                    selectedDevice = currentDeviceList.get(row);
                    updateRightPreview();
                } else {
                    selectedDevice = null;
                    clearRightPreview();
                }
            }
        });

        // Double click to view details
        tblDevices.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tblDevices.getSelectedRow() >= 0) {
                    showViewDetailsDialog();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tblDevices);
        scrollPane.setBorder(new LineBorder(new Color(220, 225, 230)));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightActionPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(220, 225, 230)),
                new EmptyBorder(12, 14, 12, 14)
        ));

        // Preview Header
        JLabel lblHeader = new JLabel("DEVICE DETAILS & ACTIONS");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setForeground(new Color(24, 43, 73));
        panel.add(lblHeader, BorderLayout.NORTH);

        // Center: Details Card
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        lblPreviewName = new JLabel("Select a device from table");
        lblPreviewName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblPreviewName.setForeground(new Color(41, 128, 185));

        lblPreviewCode = new JLabel("—");
        lblPreviewType = new JLabel("—");
        lblPreviewIp = new JLabel("—");
        lblPreviewMac = new JLabel("—");
        lblPreviewModel = new JLabel("—");
        lblPreviewLocation = new JLabel("—");
        lblPreviewAssigned = new JLabel("—");
        lblPreviewStatus = new JLabel("—");
        lblPreviewWarranty = new JLabel("—");

        infoPanel.add(lblPreviewName);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(createPreviewRow("Code / ID:", lblPreviewCode));
        infoPanel.add(createPreviewRow("Device Type:", lblPreviewType));
        infoPanel.add(createPreviewRow("IP Address:", lblPreviewIp));
        infoPanel.add(createPreviewRow("MAC Address:", lblPreviewMac));
        infoPanel.add(createPreviewRow("Model:", lblPreviewModel));
        infoPanel.add(createPreviewRow("Location:", lblPreviewLocation));
        infoPanel.add(createPreviewRow("Assigned:", lblPreviewAssigned));
        infoPanel.add(createPreviewRow("Status:", lblPreviewStatus));
        infoPanel.add(createPreviewRow("Warranty:", lblPreviewWarranty));

        JScrollPane scrollInfo = new JScrollPane(infoPanel);
        scrollInfo.setBorder(null);
        panel.add(scrollInfo, BorderLayout.CENTER);

        // Bottom: Action Buttons
        JPanel actionsContainer = new JPanel();
        actionsContainer.setLayout(new BoxLayout(actionsContainer, BoxLayout.Y_AXIS));
        actionsContainer.setBackground(Color.WHITE);

        // Primary actions
        JPanel primaryBtns = new JPanel(new GridLayout(2, 2, 6, 6));
        primaryBtns.setOpaque(false);

        btnViewDetails = new JButton("View Full Details");
        btnViewDetails.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnViewDetails.setEnabled(false);
        btnViewDetails.addActionListener(e -> showViewDetailsDialog());

        btnEditDevice = new JButton("Edit Device");
        btnEditDevice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEditDevice.setEnabled(false);
        btnEditDevice.addActionListener(e -> showEditDeviceDialog());

        btnDeleteDevice = new JButton("Delete Device");
        btnDeleteDevice.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnDeleteDevice.setBackground(new Color(231, 76, 60));
        btnDeleteDevice.setForeground(Color.WHITE);
        btnDeleteDevice.setEnabled(false);
        btnDeleteDevice.addActionListener(e -> performDeleteDevice());

        JButton btnAddDevice = new JButton("+ Add New");
        btnAddDevice.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAddDevice.setBackground(new Color(39, 174, 96));
        btnAddDevice.setForeground(Color.WHITE);
        btnAddDevice.addActionListener(e -> showAddDeviceDialog());

        primaryBtns.add(btnViewDetails);
        primaryBtns.add(btnEditDevice);
        primaryBtns.add(btnAddDevice);
        primaryBtns.add(btnDeleteDevice);

        // Quick Status Toggles
        JPanel statusTogglePanel = new JPanel(new GridLayout(1, 3, 4, 0));
        statusTogglePanel.setOpaque(false);
        statusTogglePanel.setBorder(BorderFactory.createTitledBorder("Quick Status Change"));

        btnSetActive = new JButton("Active");
        btnSetActive.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSetActive.setEnabled(false);
        btnSetActive.addActionListener(e -> changeDeviceStatus("Active"));

        btnSetMaint = new JButton("Maint.");
        btnSetMaint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSetMaint.setEnabled(false);
        btnSetMaint.addActionListener(e -> changeDeviceStatus("Under Maintenance"));

        btnSetInactive = new JButton("Inactive");
        btnSetInactive.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnSetInactive.setEnabled(false);
        btnSetInactive.addActionListener(e -> changeDeviceStatus("Inactive"));

        statusTogglePanel.add(btnSetActive);
        statusTogglePanel.add(btnSetMaint);
        statusTogglePanel.add(btnSetInactive);

        actionsContainer.add(primaryBtns);
        actionsContainer.add(Box.createVerticalStrut(8));
        actionsContainer.add(statusTogglePanel);

        panel.add(actionsContainer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPreviewRow(String label, JLabel valComp) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(new EmptyBorder(3, 2, 3, 2));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(Color.GRAY);
        lbl.setPreferredSize(new Dimension(85, 18));

        valComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        valComp.setForeground(new Color(44, 62, 80));

        row.add(lbl, BorderLayout.WEST);
        row.add(valComp, BorderLayout.CENTER);
        return row;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(240, 243, 245));
        bar.setBorder(new EmptyBorder(5, 16, 5, 16));

        lblStatusText = new JLabel("Database connected: " + DBConnection.getDatabase() + " | Ready");
        lblStatusText.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatusText.setForeground(new Color(44, 62, 80));

        JLabel lblLegend = new JLabel("System: MySQL 8.0 • Double-click row for full details");
        lblLegend.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblLegend.setForeground(Color.GRAY);

        bar.add(lblStatusText, BorderLayout.WEST);
        bar.add(lblLegend, BorderLayout.EAST);
        return bar;
    }

    public void refreshAllData() {
        applyFilter();
        refreshKpis();
    }

    private void refreshKpis() {
        try {
            Map<String, Integer> counts = deviceDAO.getStatusCounts();
            lblTotalCount.setText(String.valueOf(counts.getOrDefault("Total", 0)));
            lblActiveCount.setText(String.valueOf(counts.getOrDefault("Active", 0)));
            lblInactiveCount.setText(String.valueOf(counts.getOrDefault("Inactive", 0)));
            lblMaintCount.setText(String.valueOf(counts.getOrDefault("Under Maintenance", 0)));
        } catch (SQLException ex) {
            lblStatusText.setText("Status counts error: " + ex.getMessage());
        }
    }

    public void applyFilter() {
        String search = txtSearch.getText().trim();
        String type = (String) cmbTypeFilter.getSelectedItem();
        String status = (String) cmbStatusFilter.getSelectedItem();

        try {
            currentDeviceList = deviceDAO.searchAndFilter(search, type, status);
            tableModel.setRowCount(0);

            for (Device d : currentDeviceList) {
                tableModel.addRow(new Object[]{
                        d.getId(),
                        d.getDeviceCode(),
                        d.getDeviceName(),
                        d.getDeviceType(),
                        d.getIpAddress(),
                        d.getMacAddress(),
                        d.getManufacturer(),
                        d.getModel(),
                        d.getLocation(),
                        d.getAssignedUser(),
                        d.getStatus()
                });
            }

            lblStatusText.setText("Loaded " + currentDeviceList.size() + " network devices. Filter active: " +
                    (search.isEmpty() ? "None" : "'" + search + "'") + " | " + type + " | " + status);

            clearRightPreview();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to query inventory: " + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRightPreview() {
        if (selectedDevice != null) {
            lblPreviewName.setText(selectedDevice.getDeviceName());
            lblPreviewCode.setText(selectedDevice.getDeviceCode());
            lblPreviewType.setText(selectedDevice.getDeviceType());
            lblPreviewIp.setText(selectedDevice.getIpAddress());
            lblPreviewMac.setText(selectedDevice.getMacAddress());
            lblPreviewModel.setText(selectedDevice.getManufacturer() + " " + selectedDevice.getModel());
            lblPreviewLocation.setText(selectedDevice.getLocation());
            lblPreviewAssigned.setText(selectedDevice.getAssignedUser());
            lblPreviewStatus.setText(selectedDevice.getStatus());

            if ("Active".equalsIgnoreCase(selectedDevice.getStatus())) {
                lblPreviewStatus.setForeground(new Color(39, 174, 96));
            } else if ("Inactive".equalsIgnoreCase(selectedDevice.getStatus())) {
                lblPreviewStatus.setForeground(new Color(192, 57, 43));
            } else {
                lblPreviewStatus.setForeground(new Color(230, 126, 34));
            }

            String w = selectedDevice.getWarrantyExpiry();
            lblPreviewWarranty.setText(w != null && !w.isEmpty() ? w : "N/A");

            btnViewDetails.setEnabled(true);
            btnEditDevice.setEnabled(true);
            btnDeleteDevice.setEnabled(true);
            btnSetActive.setEnabled(true);
            btnSetMaint.setEnabled(true);
            btnSetInactive.setEnabled(true);
        }
    }

    private void clearRightPreview() {
        selectedDevice = null;
        lblPreviewName.setText("Select a device from table");
        lblPreviewName.setForeground(Color.GRAY);
        lblPreviewCode.setText("—");
        lblPreviewType.setText("—");
        lblPreviewIp.setText("—");
        lblPreviewMac.setText("—");
        lblPreviewModel.setText("—");
        lblPreviewLocation.setText("—");
        lblPreviewAssigned.setText("—");
        lblPreviewStatus.setText("—");
        lblPreviewStatus.setForeground(Color.DARK_GRAY);
        lblPreviewWarranty.setText("—");

        btnViewDetails.setEnabled(false);
        btnEditDevice.setEnabled(false);
        btnDeleteDevice.setEnabled(false);
        btnSetActive.setEnabled(false);
        btnSetMaint.setEnabled(false);
        btnSetInactive.setEnabled(false);
    }

    private void showAddDeviceDialog() {
        DeviceFormDialog form = new DeviceFormDialog(this, null);
        form.setVisible(true);
        if (form.isSaved()) {
            refreshAllData();
        }
    }

    private void showEditDeviceDialog() {
        if (selectedDevice == null) return;
        DeviceFormDialog form = new DeviceFormDialog(this, selectedDevice);
        form.setVisible(true);
        if (form.isSaved()) {
            refreshAllData();
        }
    }

    private void showViewDetailsDialog() {
        if (selectedDevice == null) return;
        DeviceDetailDialog dlg = new DeviceDetailDialog(this, selectedDevice);
        dlg.setVisible(true);
        if (dlg.isEdited()) {
            refreshAllData();
        }
    }

    private void performDeleteDevice() {
        if (selectedDevice == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to permanently delete device '" + selectedDevice.getDeviceName() +
                "' (" + selectedDevice.getDeviceCode() + " - " + selectedDevice.getIpAddress() + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean deleted = deviceDAO.deleteDevice(selectedDevice.getId());
                if (deleted) {
                    JOptionPane.showMessageDialog(this, "Device removed successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    refreshAllData();
                } else {
                    JOptionPane.showMessageDialog(this, "Could not delete device from database.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changeDeviceStatus(String newStatus) {
        if (selectedDevice == null) return;
        try {
            boolean updated = deviceDAO.updateStatus(selectedDevice.getId(), newStatus);
            if (updated) {
                selectedDevice.setStatus(newStatus);
                refreshAllData();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Status update error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to sign out?", "Confirm Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            Session.clear();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}

