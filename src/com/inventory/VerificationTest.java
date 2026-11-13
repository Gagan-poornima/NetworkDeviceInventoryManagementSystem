package com.inventory;

import com.inventory.dao.DeviceDAO;
import com.inventory.dao.UserDAO;
import com.inventory.db.DatabaseInitializer;
import com.inventory.model.Device;
import com.inventory.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class VerificationTest {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("NETWORK DEVICE INVENTORY MANAGEMENT SYSTEM - VERIFICATION SUITE");
        System.out.println("===============================================================");

        int testsPassed = 0;
        int totalTests = 8;

        try {
            // STEP 1: Database Initialization
            System.out.println("\n[Test 1/8] Initializing Database & Verifying Schema...");
            DatabaseInitializer.initialize();
            System.out.println("✓ Database initialized successfully!");
            testsPassed++;

            // STEP 2: Authentication Workflow
            System.out.println("\n[Test 2/8] Testing Login & Authentication Workflow...");
            UserDAO userDAO = new UserDAO();
            User admin = userDAO.authenticate("admin", "admin123");
            if (admin == null || !"ADMIN".equalsIgnoreCase(admin.getRole())) {
                throw new AssertionError("Admin authentication failed!");
            }
            User invalid = userDAO.authenticate("admin", "wrong_pass");
            if (invalid != null) {
                throw new AssertionError("Invalid password check failed!");
            }
            System.out.println("✓ Authentication verified: admin logged in with role: " + admin.getRole());
            testsPassed++;

            // STEP 3: Dashboard Metrics & Initial Inventory
            System.out.println("\n[Test 3/8] Testing Dashboard Metrics & Inventory Table Data...");
            DeviceDAO deviceDAO = new DeviceDAO();
            List<Device> initialDevices = deviceDAO.getAllDevices();
            System.out.println("✓ Found " + initialDevices.size() + " total devices in inventory.");
            if (initialDevices.isEmpty()) {
                throw new AssertionError("Device inventory should not be empty!");
            }

            Map<String, Integer> counts = deviceDAO.getStatusCounts();
            System.out.println("✓ Status Counts: Total=" + counts.get("Total") +
                    ", Active=" + counts.get("Active") +
                    ", Inactive=" + counts.get("Inactive") +
                    ", Under Maintenance=" + counts.get("Under Maintenance"));
            testsPassed++;

            // STEP 4: Add Network Device
            System.out.println("\n[Test 4/8] Testing Add Network Device...");
            Device newDevice = new Device();
            newDevice.setDeviceCode("RTR-VERIF-99");
            newDevice.setDeviceName("Verification Edge Router");
            newDevice.setDeviceType("Router");
            newDevice.setIpAddress("192.168.99.1");
            newDevice.setMacAddress("AA:BB:CC:DD:EE:99");
            newDevice.setManufacturer("Cisco");
            newDevice.setModel("Catalyst 8200-1N-4T");
            newDevice.setLocation("Test Lab Rack 9");
            newDevice.setAssignedUser("QA Validation Team");
            newDevice.setPurchaseDate("2024-01-10");
            newDevice.setWarrantyExpiry("2027-01-10");
            newDevice.setStatus("Active");
            newDevice.setNotes("Automated end-to-end verification device.");

            boolean added = deviceDAO.addDevice(newDevice);
            if (!added || newDevice.getId() <= 0) {
                throw new AssertionError("Failed to add test device!");
            }
            System.out.println("✓ Device added successfully! ID: " + newDevice.getId() + " | Code: " + newDevice.getDeviceCode());
            testsPassed++;

            // STEP 5: View Device Details
            System.out.println("\n[Test 5/8] Testing View Device Details...");
            Device fetched = deviceDAO.getDeviceById(newDevice.getId());
            if (fetched == null || !fetched.getDeviceName().equals("Verification Edge Router")) {
                throw new AssertionError("Device details do not match expected values!");
            }
            System.out.println("✓ Retrieved Device Details: " + fetched.getDeviceName() + " (" + fetched.getModel() +
                    ") at " + fetched.getLocation() + " [Status: " + fetched.getStatus() + "]");
            testsPassed++;

            // STEP 6: Search and Filter Devices
            System.out.println("\n[Test 6/8] Testing Search & Filter Operations...");
            List<Device> searchResult = deviceDAO.searchAndFilter("Verification", null, null);
            if (searchResult.isEmpty() || searchResult.get(0).getId() != newDevice.getId()) {
                throw new AssertionError("Search by name failed!");
            }
            System.out.println("✓ Search by keyword 'Verification' returned: " + searchResult.size() + " match(es).");

            List<Device> ipResult = deviceDAO.searchAndFilter("192.168.99.1", null, null);
            if (ipResult.isEmpty()) {
                throw new AssertionError("Search by IP failed!");
            }
            System.out.println("✓ Search by IP '192.168.99.1' returned match.");

            List<Device> typeFilter = deviceDAO.searchAndFilter(null, "Router", "Active");
            if (typeFilter.isEmpty()) {
                throw new AssertionError("Filter by Type and Status failed!");
            }
            System.out.println("✓ Filter by Type='Router' and Status='Active' returned " + typeFilter.size() + " matches.");
            testsPassed++;

            // STEP 7: Update Device Information & Status Tracking
            System.out.println("\n[Test 7/8] Testing Update Device Information & Status Tracking...");
            fetched.setDeviceName("Verification Edge Router (Upgraded)");
            fetched.setModel("Catalyst 8300-2N-8T");
            fetched.setStatus("Under Maintenance");
            fetched.setNotes("Upgraded to 8300 series; undergoing maintenance checks.");

            boolean updated = deviceDAO.updateDevice(fetched);
            if (!updated) {
                throw new AssertionError("Device update failed!");
            }

            Device reFetched = deviceDAO.getDeviceById(newDevice.getId());
            if (!"Under Maintenance".equals(reFetched.getStatus()) || !"Catalyst 8300-2N-8T".equals(reFetched.getModel())) {
                throw new AssertionError("Updated values did not persist!");
            }
            System.out.println("✓ Updated device info persisted: Name='" + reFetched.getDeviceName() +
                    "', Model='" + reFetched.getModel() + "', Status='" + reFetched.getStatus() + "'");

            boolean statusQuickUpdated = deviceDAO.updateStatus(newDevice.getId(), "Inactive");
            if (!statusQuickUpdated || !"Inactive".equals(deviceDAO.getDeviceById(newDevice.getId()).getStatus())) {
                throw new AssertionError("Quick status update failed!");
            }
            System.out.println("✓ Quick status tracking verified: changed status to 'Inactive'.");
            testsPassed++;

            // STEP 8: Delete Device
            System.out.println("\n[Test 8/8] Testing Delete Device...");
            boolean deleted = deviceDAO.deleteDevice(newDevice.getId());
            if (!deleted) {
                throw new AssertionError("Failed to delete test device!");
            }
            Device deletedCheck = deviceDAO.getDeviceById(newDevice.getId());
            if (deletedCheck != null) {
                throw new AssertionError("Device still exists after deletion!");
            }
            System.out.println("✓ Device successfully deleted from database.");
            testsPassed++;

            System.out.println("\n===============================================================");
            System.out.println("ALL " + testsPassed + "/" + totalTests + " WORKFLOW TESTS PASSED SUCCESSFULLY!");
            System.out.println("Full Workflow Verified: Login → Dashboard → Add → View → Search → Update → Delete");
            System.out.println("===============================================================");

        } catch (SQLException e) {
            System.err.println("SQL Exception during verification: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (AssertionError e) {
            System.err.println("Assertion Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}

