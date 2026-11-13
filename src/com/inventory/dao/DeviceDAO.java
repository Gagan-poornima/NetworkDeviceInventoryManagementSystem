package com.inventory.dao;

import com.inventory.db.DBConnection;
import com.inventory.model.Device;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceDAO {

    public List<Device> getAllDevices() throws SQLException {
        return searchAndFilter(null, null, null);
    }

    public Device getDeviceById(int id) throws SQLException {
        String sql = "SELECT * FROM `devices` WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDevice(rs);
                }
            }
        }
        return null;
    }

    public List<Device> searchAndFilter(String searchQuery, String typeFilter, String statusFilter) throws SQLException {
        List<Device> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM `devices` WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            String term = "%" + searchQuery.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(device_code) LIKE ? OR LOWER(device_name) LIKE ? OR LOWER(ip_address) LIKE ? ")
               .append("OR LOWER(mac_address) LIKE ? OR LOWER(manufacturer) LIKE ? OR LOWER(model) LIKE ? ")
               .append("OR LOWER(location) LIKE ? OR LOWER(assigned_user) LIKE ?)");
            for (int i = 0; i < 8; i++) {
                params.add(term);
            }
        }

        if (typeFilter != null && !typeFilter.trim().isEmpty() && !typeFilter.equalsIgnoreCase("All Types")) {
            sql.append(" AND device_type = ?");
            params.add(typeFilter.trim());
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty() && !statusFilter.equalsIgnoreCase("All Statuses")) {
            sql.append(" AND status = ?");
            params.add(statusFilter.trim());
        }

        sql.append(" ORDER BY id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToDevice(rs));
                }
            }
        }
        return list;
    }

    public boolean addDevice(Device d) throws SQLException {
        String sql = "INSERT INTO `devices` (device_code, device_name, device_type, ip_address, mac_address, " +
                "manufacturer, model, location, assigned_user, purchase_date, warranty_expiry, status, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getDeviceCode());
            ps.setString(2, d.getDeviceName());
            ps.setString(3, d.getDeviceType());
            ps.setString(4, d.getIpAddress());
            ps.setString(5, d.getMacAddress());
            ps.setString(6, d.getManufacturer());
            ps.setString(7, d.getModel());
            ps.setString(8, d.getLocation());
            ps.setString(9, d.getAssignedUser());

            if (d.getPurchaseDate() != null && !d.getPurchaseDate().trim().isEmpty()) {
                ps.setDate(10, Date.valueOf(d.getPurchaseDate().trim()));
            } else {
                ps.setNull(10, Types.DATE);
            }

            if (d.getWarrantyExpiry() != null && !d.getWarrantyExpiry().trim().isEmpty()) {
                ps.setDate(11, Date.valueOf(d.getWarrantyExpiry().trim()));
            } else {
                ps.setNull(11, Types.DATE);
            }

            ps.setString(12, d.getStatus() != null ? d.getStatus() : "Active");
            ps.setString(13, d.getNotes());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        d.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateDevice(Device d) throws SQLException {
        String sql = "UPDATE `devices` SET device_code = ?, device_name = ?, device_type = ?, ip_address = ?, " +
                "mac_address = ?, manufacturer = ?, model = ?, location = ?, assigned_user = ?, purchase_date = ?, " +
                "warranty_expiry = ?, status = ?, notes = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, d.getDeviceCode());
            ps.setString(2, d.getDeviceName());
            ps.setString(3, d.getDeviceType());
            ps.setString(4, d.getIpAddress());
            ps.setString(5, d.getMacAddress());
            ps.setString(6, d.getManufacturer());
            ps.setString(7, d.getModel());
            ps.setString(8, d.getLocation());
            ps.setString(9, d.getAssignedUser());

            if (d.getPurchaseDate() != null && !d.getPurchaseDate().trim().isEmpty()) {
                ps.setDate(10, Date.valueOf(d.getPurchaseDate().trim()));
            } else {
                ps.setNull(10, Types.DATE);
            }

            if (d.getWarrantyExpiry() != null && !d.getWarrantyExpiry().trim().isEmpty()) {
                ps.setDate(11, Date.valueOf(d.getWarrantyExpiry().trim()));
            } else {
                ps.setNull(11, Types.DATE);
            }

            ps.setString(12, d.getStatus());
            ps.setString(13, d.getNotes());
            ps.setInt(14, d.getId());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteDevice(int id) throws SQLException {
        String sql = "DELETE FROM `devices` WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String newStatus) throws SQLException {
        String sql = "UPDATE `devices` SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean isDeviceCodeExists(String code, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `devices` WHERE device_code = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean isIpAddressExists(String ip, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM `devices` WHERE ip_address = ? AND id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ip);
            ps.setInt(2, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public Map<String, Integer> getStatusCounts() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("Total", 0);
        counts.put("Active", 0);
        counts.put("Inactive", 0);
        counts.put("Under Maintenance", 0);

        String sql = "SELECT status, COUNT(*) as cnt FROM `devices` GROUP BY status";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int total = 0;
            while (rs.next()) {
                String st = rs.getString("status");
                int cnt = rs.getInt("cnt");
                total += cnt;
                if (st != null) {
                    if (st.equalsIgnoreCase("Active")) counts.put("Active", cnt);
                    else if (st.equalsIgnoreCase("Inactive")) counts.put("Inactive", cnt);
                    else if (st.equalsIgnoreCase("Under Maintenance")) counts.put("Under Maintenance", cnt);
                }
            }
            counts.put("Total", total);
        }
        return counts;
    }

    public Map<String, Integer> getTypeCounts() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT device_type, COUNT(*) as cnt FROM `devices` GROUP BY device_type ORDER BY cnt DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                counts.put(rs.getString("device_type"), rs.getInt("cnt"));
            }
        }
        return counts;
    }

    private Device mapRowToDevice(ResultSet rs) throws SQLException {
        Device d = new Device();
        d.setId(rs.getInt("id"));
        d.setDeviceCode(rs.getString("device_code"));
        d.setDeviceName(rs.getString("device_name"));
        d.setDeviceType(rs.getString("device_type"));
        d.setIpAddress(rs.getString("ip_address"));
        d.setMacAddress(rs.getString("mac_address"));
        d.setManufacturer(rs.getString("manufacturer"));
        d.setModel(rs.getString("model"));
        d.setLocation(rs.getString("location"));
        d.setAssignedUser(rs.getString("assigned_user"));

        Date pDate = rs.getDate("purchase_date");
        d.setPurchaseDate(pDate != null ? pDate.toString() : "");

        Date wDate = rs.getDate("warranty_expiry");
        d.setWarrantyExpiry(wDate != null ? wDate.toString() : "");

        d.setStatus(rs.getString("status"));
        d.setNotes(rs.getString("notes"));
        return d;
    }
}

