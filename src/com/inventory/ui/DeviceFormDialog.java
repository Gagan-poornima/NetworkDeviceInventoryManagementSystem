package com.inventory.ui;

import com.inventory.dao.DeviceDAO;
import com.inventory.model.Device;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;
import java.util.regex.Pattern;

public class DeviceFormDialog extends JDialog {
    private final DeviceDAO deviceDAO = new DeviceDAO();
    private final Device existingDevice;
    private boolean saved = false;

    private JTextField txtDeviceCode;
    private JTextField txtDeviceName;
    private JComboBox<String> cmbDeviceType;
    private JTextField txtIpAddress;
    private JTextField txtMacAddress;
    private JComboBox<String> cmbManufacturer;
    private JTextField txtModel;
    private JTextField txtLocation;
    private JTextField txtAssignedUser;
    private JTextField txtPurchaseDate;
    private JTextField txtWarrantyExpiry;
    private JComboBox<String> cmbStatus;
    private JTextArea txtNotes;

    private static final Pattern IP_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");

    private static final Pattern MAC_PATTERN = Pattern.compile(
            "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$");

    public DeviceFormDialog(Frame owner, Device deviceToEdit) {
        super(owner, deviceToEdit == null ? "Add New Network Device" : "Update Device Information", true);
        this.existingDevice = deviceToEdit;
        initUI();
    }

    private void initUI() {
        setSize(650, 680);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel lblTitle = new JLabel(existingDevice == null ? "REGISTER NEW NETWORK DEVICE" : "EDIT DEVICE DETAILS");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(93, 173, 226));

        JLabel lblSub = new JLabel("Fill in complete network specifications and asset metadata");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(189, 195, 199));

        headerPanel.add(lblTitle, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form fields panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(15, 25, 15, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 6, 5, 6);

        txtDeviceCode = new JTextField(15);
        JButton btnAutoGenCode = new JButton("Auto-ID");
        btnAutoGenCode.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnAutoGenCode.setMargin(new Insets(2, 6, 2, 6));
        btnAutoGenCode.addActionListener(e -> generateDeviceCode());

        JPanel codePanel = new JPanel(new BorderLayout(5, 0));
        codePanel.add(txtDeviceCode, BorderLayout.CENTER);
        codePanel.add(btnAutoGenCode, BorderLayout.EAST);

        txtDeviceName = new JTextField(15);

        String[] types = new String[]{"Router", "Switch", "Server", "Firewall", "Access Point"};
        cmbDeviceType = new JComboBox<>(types);

        txtIpAddress = new JTextField(15);
        txtMacAddress = new JTextField(15);

        String[] makers = new String[]{"Cisco", "Juniper", "Dell", "HP", "Fortinet", "Aruba", "Ubiquiti", "Palo Alto", "MikroTik", "Other"};
        cmbManufacturer = new JComboBox<>(makers);
        cmbManufacturer.setEditable(true);

        txtModel = new JTextField(15);
        txtLocation = new JTextField(15);
        txtAssignedUser = new JTextField(15);

        txtPurchaseDate = new JTextField(15);
        txtPurchaseDate.setToolTipText("Format: YYYY-MM-DD (e.g. 2024-05-20)");
        txtWarrantyExpiry = new JTextField(15);
        txtWarrantyExpiry.setToolTipText("Format: YYYY-MM-DD (e.g. 2027-05-20)");

        String[] statuses = new String[]{"Active", "Inactive", "Under Maintenance"};
        cmbStatus = new JComboBox<>(statuses);

        txtNotes = new JTextArea(3, 20);
        txtNotes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        JScrollPane scrollNotes = new JScrollPane(txtNotes);

        int r = 0;
        addFormRow(formPanel, gbc, r++, "Device Code / ID *:", codePanel);
        addFormRow(formPanel, gbc, r++, "Device Name *:", txtDeviceName);
        addFormRow(formPanel, gbc, r++, "Device Type *:", cmbDeviceType);
        addFormRow(formPanel, gbc, r++, "IP Address *:", txtIpAddress);
        addFormRow(formPanel, gbc, r++, "MAC Address *:", txtMacAddress);
        addFormRow(formPanel, gbc, r++, "Manufacturer *:", cmbManufacturer);
        addFormRow(formPanel, gbc, r++, "Model *:", txtModel);
        addFormRow(formPanel, gbc, r++, "Location *:", txtLocation);
        addFormRow(formPanel, gbc, r++, "Assigned User/Team *:", txtAssignedUser);
        addFormRow(formPanel, gbc, r++, "Purchase Date (YYYY-MM-DD):", txtPurchaseDate);
        addFormRow(formPanel, gbc, r++, "Warranty Expiry (YYYY-MM-DD):", txtWarrantyExpiry);
        addFormRow(formPanel, gbc, r++, "Status *:", cmbStatus);
        addFormRow(formPanel, gbc, r++, "Notes / Comments:", scrollNotes);

        JScrollPane centerScroll = new JScrollPane(formPanel);
        centerScroll.setBorder(null);
        centerScroll.getVerticalScrollBar().setUnitIncrement(16);
        add(centerScroll, BorderLayout.CENTER);

        // Prepopulate if editing
        if (existingDevice != null) {
            txtDeviceCode.setText(existingDevice.getDeviceCode());
            txtDeviceName.setText(existingDevice.getDeviceName());
            cmbDeviceType.setSelectedItem(existingDevice.getDeviceType());
            txtIpAddress.setText(existingDevice.getIpAddress());
            txtMacAddress.setText(existingDevice.getMacAddress());
            cmbManufacturer.setSelectedItem(existingDevice.getManufacturer());
            txtModel.setText(existingDevice.getModel());
            txtLocation.setText(existingDevice.getLocation());
            txtAssignedUser.setText(existingDevice.getAssignedUser());
            txtPurchaseDate.setText(existingDevice.getPurchaseDate());
            txtWarrantyExpiry.setText(existingDevice.getWarrantyExpiry());
            cmbStatus.setSelectedItem(existingDevice.getStatus());
            txtNotes.setText(existingDevice.getNotes());
        } else {
            generateDeviceCode();
            txtPurchaseDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            txtWarrantyExpiry.setText(LocalDate.now().plusYears(3).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        // Action Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(new Color(245, 247, 248));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(existingDevice == null ? "Add Device" : "Save Changes");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setBackground(new Color(39, 174, 96));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.addActionListener(e -> saveDevice());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.32;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(lbl, gbc);

        gbc.gridx = 1; gbc.weightx = 0.68;
        panel.add(comp, gbc);
    }

    private void generateDeviceCode() {
        String type = (String) cmbDeviceType.getSelectedItem();
        String prefix = "DEV";
        if ("Router".equalsIgnoreCase(type)) prefix = "RTR";
        else if ("Switch".equalsIgnoreCase(type)) prefix = "SW";
        else if ("Server".equalsIgnoreCase(type)) prefix = "SRV";
        else if ("Firewall".equalsIgnoreCase(type)) prefix = "FW";
        else if ("Access Point".equalsIgnoreCase(type)) prefix = "AP";

        int randNum = 100 + new Random().nextInt(900);
        txtDeviceCode.setText(prefix + "-" + randNum);
    }

    private void saveDevice() {
        String code = txtDeviceCode.getText().trim();
        String name = txtDeviceName.getText().trim();
        String type = (String) cmbDeviceType.getSelectedItem();
        String ip = txtIpAddress.getText().trim();
        String mac = txtMacAddress.getText().trim().toUpperCase();
        String manufacturer = cmbManufacturer.getSelectedItem() != null ? cmbManufacturer.getSelectedItem().toString().trim() : "";
        String model = txtModel.getText().trim();
        String location = txtLocation.getText().trim();
        String assignedUser = txtAssignedUser.getText().trim();
        String pDate = txtPurchaseDate.getText().trim();
        String wDate = txtWarrantyExpiry.getText().trim();
        String status = (String) cmbStatus.getSelectedItem();
        String notes = txtNotes.getText().trim();

        // 1. Basic validation
        if (code.isEmpty() || name.isEmpty() || ip.isEmpty() || mac.isEmpty() || model.isEmpty() || location.isEmpty() || assignedUser.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all required fields (Device Code, Name, IP, MAC, Model, Location, Assigned User).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 2. IP Address format validation
        if (!IP_PATTERN.matcher(ip).matches()) {
            JOptionPane.showMessageDialog(this,
                    "Invalid IP Address format. Example: 192.168.1.10",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtIpAddress.requestFocus();
            return;
        }

        // 3. MAC Address format validation
        if (!MAC_PATTERN.matcher(mac).matches()) {
            JOptionPane.showMessageDialog(this,
                    "Invalid MAC Address format. Example: 00:1A:2B:3C:4D:5E or 00-1A-2B-3C-4D-5E",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            txtMacAddress.requestFocus();
            return;
        }

        // 4. Date validation
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (!pDate.isEmpty()) {
            try {
                LocalDate.parse(pDate, dtf);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Purchase Date must be in YYYY-MM-DD format (e.g. 2024-01-15).",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtPurchaseDate.requestFocus();
                return;
            }
        }

        if (!wDate.isEmpty()) {
            try {
                LocalDate.parse(wDate, dtf);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,
                        "Warranty Expiry must be in YYYY-MM-DD format (e.g. 2027-01-15).",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                txtWarrantyExpiry.requestFocus();
                return;
            }
        }

        int currentId = existingDevice != null ? existingDevice.getId() : 0;

        // 5. Database uniqueness checks
        try {
            if (deviceDAO.isDeviceCodeExists(code, currentId)) {
                JOptionPane.showMessageDialog(this,
                        "Device Code '" + code + "' already exists! Please use a unique Device Code.",
                        "Duplicate Error", JOptionPane.WARNING_MESSAGE);
                txtDeviceCode.requestFocus();
                return;
            }

            if (deviceDAO.isIpAddressExists(ip, currentId)) {
                JOptionPane.showMessageDialog(this,
                        "IP Address '" + ip + "' is already assigned to another device!",
                        "Duplicate IP Warning", JOptionPane.WARNING_MESSAGE);
                txtIpAddress.requestFocus();
                return;
            }

            Device device = existingDevice != null ? existingDevice : new Device();
            device.setDeviceCode(code);
            device.setDeviceName(name);
            device.setDeviceType(type);
            device.setIpAddress(ip);
            device.setMacAddress(mac);
            device.setManufacturer(manufacturer);
            device.setModel(model);
            device.setLocation(location);
            device.setAssignedUser(assignedUser);
            device.setPurchaseDate(pDate);
            device.setWarrantyExpiry(wDate);
            device.setStatus(status);
            device.setNotes(notes);

            boolean success;
            if (existingDevice == null) {
                success = deviceDAO.addDevice(device);
            } else {
                success = deviceDAO.updateDevice(device);
            }

            if (success) {
                saved = true;
                JOptionPane.showMessageDialog(this,
                        existingDevice == null ? "Device added successfully!" : "Device updated successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Operation could not be saved to database.",
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Database Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}

