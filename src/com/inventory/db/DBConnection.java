package com.inventory.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static final String CONFIG_FILE = "db.properties";
    private static String host = "localhost";
    private static int port = 3306;
    private static String database = "network_device_inventory";
    private static String user = "root";
    private static String password = "DivgunSingh@21$";

    static {
        loadConfig();
    }

    public static synchronized void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                host = props.getProperty("db.host", host).trim();
                try {
                    port = Integer.parseInt(props.getProperty("db.port", String.valueOf(port)).trim());
                } catch (NumberFormatException ignored) {}
                database = props.getProperty("db.database", database).trim();
                user = props.getProperty("db.user", user).trim();
                password = props.getProperty("db.password", password).trim();
            } catch (IOException e) {
                System.err.println("Warning: could not read " + CONFIG_FILE + ": " + e.getMessage());
            }
        } else {
            saveConfig(host, port, database, user, password);
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                System.err.println("MySQL JDBC Driver not found in classpath!");
            }
        }
    }

    public static synchronized void saveConfig(String newHost, int newPort, String newDb, String newUser, String newPass) {
        host = newHost;
        port = newPort;
        database = newDb;
        user = newUser;
        password = newPass;

        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", String.valueOf(port));
        props.setProperty("db.database", database);
        props.setProperty("db.user", user);
        props.setProperty("db.password", password);

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "MySQL Database Configuration for Network Device Inventory Management System");
        } catch (IOException e) {
            System.err.println("Could not save " + CONFIG_FILE + ": " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
        return DriverManager.getConnection(url, user, password);
    }

    public static Connection getServerConnection() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port +
                "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
        return DriverManager.getConnection(url, user, password);
    }

    public static boolean testConnection(String testHost, int testPort, String testDb, String testUser, String testPass) throws SQLException {
        String url = "jdbc:mysql://" + testHost + ":" + testPort +
                "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8&connectTimeout=5000";
        try (Connection conn = DriverManager.getConnection(url, testUser, testPass)) {
            return conn != null && !conn.isClosed();
        }
    }

    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getDatabase() { return database; }
    public static String getUser() { return user; }
    public static String getPassword() { return password; }
}

