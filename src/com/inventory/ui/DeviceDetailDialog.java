package com.inventory.ui;

import com.inventory.model.Device;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DeviceDetailDialog extends JDialog {
    private final Device device;
    private boolean edited = false;

    public DeviceDetailDialog(Frame owner, Device device) {
        super(owner, "Device Details - " + device.getDeviceName(), true);
        this.device = device;
        initUI();
    }

    private void initUI() {
        setSize(560, 620);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel lblName = new JLabel(device.getDeviceName());
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblName.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(device.getDeviceType() + " | Code: " + device.getDeviceCode() + " | IP: " + device.getIpAddress());
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(189, 195, 199));

        headerPanel.add(lblName, BorderLayout.NORTH);
        headerPanel.add(lblSub, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Body Content
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Status Card
        JPanel statusCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        statusCard.setBackground(new Color(245, 247, 250));
        statusCard.setBorder(new CompoundBorder(new LineBorder(new Color(220, 225, 230)), new EmptyBorder(4, 8, 4, 8)));

        JLabel lblStatusTag = new JLabel("Status: ");
        lblStatusTag.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel lblStatusVal = new JLabel(device.getStatus());
        lblStatusVal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        if ("Active".equalsIgnoreCase(device.getStatus())) {
            lblStatusVal.setForeground(new Color(39, 174, 96));
        } else if ("Inactive".equalsIgnoreCase(device.getStatus())) {
            lblStatusVal.setForeground(new Color(192, 57, 43));
        } else {
            lblStatusVal.setForeground(new Color(211, 84, 0));
        }

        statusCard.add(lblStatusTag);
        statusCard.add(lblStatusVal);

        // Warranty evaluation
        String wDate = device.getWarrantyExpiry();
        if (wDate != null && !wDate.trim().isEmpty()) {
            try {
                LocalDate expiry = LocalDate.parse(wDate.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                boolean isExpired = expiry.isBefore(LocalDate.now());
                JLabel lblWarranty = new JLabel(" | Warranty: " + (isExpired ? "EXPIRED (" + wDate + ")" : "VALID (until " + wDate + ")"));
                lblWarranty.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                lblWarranty.setForeground(isExpired ? new Color(192, 57, 43) : new Color(39, 174, 96));
                statusCard.add(lblWarranty);
            } catch (Exception ignored) {}
        }
        bodyPanel.add(statusCard);
        bodyPanel.add(Box.createVerticalStrut(15));

        // Details grid
        JPanel gridPanel = new JPanel(new GridLayout(0, 2, 10, 8));
        gridPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        addDetailItem(gridPanel, "Database Record ID", String.valueOf(device.getId()));
        addDetailItem(gridPanel, "Device Code", device.getDeviceCode());
        addDetailItem(gridPanel, "Device Name", device.getDeviceName());
        addDetailItem(gridPanel, "Device Type", device.getDeviceType());
        addDetailItem(gridPanel, "IPv4 Address", device.getIpAddress());
        addDetailItem(gridPanel, "MAC Physical Address", device.getMacAddress());
        addDetailItem(gridPanel, "Manufacturer", device.getManufacturer());
        addDetailItem(gridPanel, "Model Specification", device.getModel());
        addDetailItem(gridPanel, "Physical Location", device.getLocation());
        addDetailItem(gridPanel, "Assigned User / Team", device.getAssignedUser());
        addDetailItem(gridPanel, "Purchase Date", device.getPurchaseDate() != null && !device.getPurchaseDate().isEmpty() ? device.getPurchaseDate() : "N/A");
        addDetailItem(gridPanel, "Warranty Expiry", device.getWarrantyExpiry() != null && !device.getWarrantyExpiry().isEmpty() ? device.getWarrantyExpiry() : "N/A");

        bodyPanel.add(gridPanel);
        bodyPanel.add(Box.createVerticalStrut(15));

        // Notes section
        JLabel lblNotesTitle = new JLabel("Notes / Operational History:");
        lblNotesTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNotesTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(lblNotesTitle);
        bodyPanel.add(Box.createVerticalStrut(5));

        JTextArea txtNotes = new JTextArea(device.getNotes() != null && !device.getNotes().isEmpty() ? device.getNotes() : "No notes recorded.");
        txtNotes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtNotes.setEditable(false);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setBackground(new Color(248, 249, 250));
        txtNotes.setBorder(new CompoundBorder(new LineBorder(new Color(220, 225, 230)), new EmptyBorder(6, 6, 6, 6)));
        JScrollPane scrollNotes = new JScrollPane(txtNotes);
        scrollNotes.setPreferredSize(new Dimension(500, 70));
        scrollNotes.setAlignmentX(Component.LEFT_ALIGNMENT);
        bodyPanel.add(scrollNotes);

        JScrollPane mainScroll = new JScrollPane(bodyPanel);
        mainScroll.setBorder(null);
        add(mainScroll, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        btnPanel.setBackground(new Color(245, 247, 248));

        JButton btnEdit = new JButton("Edit This Device");
        btnEdit.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnEdit.setBackground(new Color(243, 156, 18));
        btnEdit.setForeground(Color.BLACK);
        btnEdit.setFocusPainted(false);
        btnEdit.addActionListener(e -> {
            DeviceFormDialog form = new DeviceFormDialog((Frame) getOwner(), device);
            form.setVisible(true);
            if (form.isSaved()) {
                edited = true;
                dispose();
            }
        });

        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClose.addActionListener(e -> dispose());

        btnPanel.add(btnEdit);
        btnPanel.add(btnClose);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void addDetailItem(JPanel panel, String title, String value) {
        JPanel item = new JPanel(new BorderLayout());
        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblT.setForeground(Color.GRAY);

        JLabel lblV = new JLabel(value != null && !value.isEmpty() ? value : "—");
        lblV.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblV.setForeground(new Color(44, 62, 80));

        item.add(lblT, BorderLayout.NORTH);
        item.add(lblV, BorderLayout.CENTER);
        panel.add(item);
    }

    public boolean isEdited() {
        return edited;
    }
}

