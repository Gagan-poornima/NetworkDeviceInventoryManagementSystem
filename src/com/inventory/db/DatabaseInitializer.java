package com.inventory.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static synchronized void initialize() throws SQLException {
        // Step 1: Create database if it does not exist
        try (Connection serverConn = DBConnection.getServerConnection();
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + DBConnection.getDatabase() + "`");
        }

        // Step 2: Create tables and seed data in the network device inventory database
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS `users` (" +
                    "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "`username` VARCHAR(50) NOT NULL UNIQUE," +
                    "`password` VARCHAR(255) NOT NULL," +
                    "`full_name` VARCHAR(100) NOT NULL," +
                    "`email` VARCHAR(100)," +
                    "`role` VARCHAR(20) NOT NULL DEFAULT 'STAFF'," +
                    "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
            stmt.executeUpdate(createUsersTable);

            // 2. devices table
            String createDevicesTable = "CREATE TABLE IF NOT EXISTS `devices` (" +
                    "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "`device_code` VARCHAR(50) NOT NULL UNIQUE," +
                    "`device_name` VARCHAR(100) NOT NULL," +
                    "`device_type` VARCHAR(50) NOT NULL," +
                    "`ip_address` VARCHAR(50) NOT NULL," +
                    "`mac_address` VARCHAR(50) NOT NULL," +
                    "`manufacturer` VARCHAR(100) NOT NULL," +
                    "`model` VARCHAR(100) NOT NULL," +
                    "`location` VARCHAR(100) NOT NULL," +
                    "`assigned_user` VARCHAR(100) NOT NULL," +
                    "`purchase_date` DATE," +
                    "`warranty_expiry` DATE," +
                    "`status` VARCHAR(30) NOT NULL DEFAULT 'Active'," +
                    "`notes` TEXT," +
                    "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")";
            stmt.executeUpdate(createDevicesTable);

            // Step 3: Seed initial users if table is empty
            boolean hasUsers = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM `users`")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasUsers = true;
                }
            }

            if (!hasUsers) {
                stmt.executeUpdate("INSERT INTO `users` (`id`, `username`, `password`, `full_name`, `email`, `role`) VALUES " +
                        "(1, 'admin', 'admin123', 'System Administrator', 'admin@network.local', 'ADMIN')," +
                        "(2, 'tech1', 'tech123', 'Senior Network Engineer', 'tech@network.local', 'STAFF')");
            }

            // Step 4: Seed initial devices if table is empty
            boolean hasDevices = false;
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM `devices`")) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasDevices = true;
                }
            }

            if (!hasDevices) {
                stmt.executeUpdate("INSERT INTO `devices` (`id`, `device_code`, `device_name`, `device_type`, `ip_address`, `mac_address`, `manufacturer`, `model`, `location`, `assigned_user`, `purchase_date`, `warranty_expiry`, `status`, `notes`) VALUES " +
                        "(1, 'RTR-CORE-01', 'Core Gateway Router', 'Router', '192.168.1.1', '00:1A:2B:3C:4D:01', 'Cisco', 'Catalyst 8300', 'Data Center A - Rack 01', 'Network Ops', '2023-01-15', '2027-01-15', 'Active', 'Primary enterprise border gateway router.')," +
                        "(2, 'SW-DIST-01', 'Distribution Switch Fl 2', 'Switch', '192.168.1.10', '00:1A:2B:3C:4D:02', 'Cisco', 'Catalyst 9300 48-Port', 'Building B - Floor 2 IDF', 'IT Infrastructure', '2023-03-20', '2026-03-20', 'Active', 'Connects floor 2 workstations and APs.')," +
                        "(3, 'SRV-DB-01', 'Primary MySQL Database Host', 'Server', '192.168.1.50', '00:1A:2B:3C:4D:03', 'Dell', 'PowerEdge R750', 'Data Center A - Rack 04', 'Database Admin', '2022-11-10', '2025-11-10', 'Active', 'High-availability database host.')," +
                        "(4, 'FW-EDGE-01', 'Perimeter Firewall', 'Firewall', '192.168.1.254', '00:1A:2B:3C:4D:04', 'Fortinet', 'FortiGate 100F', 'Data Center A - Rack 01', 'Security Ops', '2023-06-01', '2026-06-01', 'Active', 'Hardware firewall with IPS/IDS active.')," +
                        "(5, 'AP-LOBBY-01', 'HQ Main Lobby Wi-Fi AP', 'Access Point', '192.168.10.15', '00:1A:2B:3C:4D:05', 'Aruba', 'AP-515 Wi-Fi 6', 'HQ Building - Main Lobby', 'Network Ops', '2024-02-14', '2027-02-14', 'Active', 'Dual-band Wi-Fi 6 indoor access point.')," +
                        "(6, 'SW-ACCESS-03', 'Warehouse Access Switch', 'Switch', '192.168.20.12', '00:1A:2B:3C:4D:06', 'Juniper', 'EX2300-24P', 'Warehouse Rack 1', 'Warehouse IT', '2021-08-05', '2024-08-05', 'Under Maintenance', 'Scheduled for port transceiver replacement.')," +
                        "(7, 'SRV-BACKUP-02', 'Secondary Cold Storage Server', 'Server', '192.168.1.55', '00:1A:2B:3C:4D:07', 'HP', 'ProLiant DL380 Gen10', 'Disaster Recovery Site', 'SysAdmin Team', '2020-05-18', '2023-05-18', 'Inactive', 'Decommissioned offline backup server.')");
            }
        }
    }
}

