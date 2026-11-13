# Network Device Inventory Management System

A functional desktop application built with **Java Swing**, **JDBC (PreparedStatement)**, and **MySQL** for managing enterprise network hardware assets, tracking lifecycle statuses, and inventory auditing.

---

## 🚀 Key Features

- **Authentication & Roles**:
  - Secure login and registration with role separation (`ADMIN`, `STAFF`, `TECHNICIAN`).
  - Pre-filled credentials for rapid evaluation:
    - Admin: `admin` / `admin123`
    - Staff / Tech: `tech1` / `tech123`
- **Dashboard & Inventory Metrics**:
  - Top summary cards: Total Devices, Active Devices, Inactive Devices, Under Maintenance.
  - Device Inventory Table displaying ID, Code, Name, Type, IP Address, MAC Address, Manufacturer, Model, Location, Assigned User, and Status.
  - Color-coded badges for device operational statuses (`Active` = Green, `Inactive` = Gray/Red, `Under Maintenance` = Orange).
- **Search & Filters**:
  - Multi-attribute text search (matches Name, Code, IP, MAC, Model, Location, or Assigned User).
  - Dropdown filters by **Device Type** (`Router`, `Switch`, `Server`, `Firewall`, `Access Point`).
  - Dropdown filters by **Status** (`Active`, `Inactive`, `Under Maintenance`).
  - One-click filter reset.
- **Device Management Lifecycle**:
  - **Add Device**: Modal dialog with input validation (IPv4 regex, MAC address format, date checks, duplicate IP/Code prevention). Auto-ID code generation helper.
  - **View Details**: Full device inspection including hardware specs, location, procurement dates, warranty validity badge, and notes.
  - **Update Device**: Modify specifications, location, or assigned team with live validation.
  - **Delete Device**: Confirmation dialog and clean database deletion.
  - **Quick Status Tracking**: One-click status toggling (`Active`, `Maintenance`, `Inactive`) from the sidebar.
- **Admin Management Console**:
  - Manage user accounts (view registered users, change roles, add users, delete users).
  - Database status metrics and sample data re-seed tool.
  - Live MySQL ping and connection diagnostic.
- **Automatic Database Setup**:
  - The application automatically creates the `network_device_inventory` database and required tables upon initial startup, seeding initial sample devices.
  - Includes a standalone `schema.sql` script.

---

## 🛠️ Technology Stack & Architecture

- **GUI**: Java Swing (`JFrame`, `JDialog`, `JTable`, `GridBagLayout`, `BorderLayout`, `Nimbus Look & Feel`)
- **Database**: MySQL 8.0 (`network_device_inventory`)
- **Persistence**: Java JDBC with `PreparedStatement`
- **Driver**: MySQL Connector/J 8.3.0 (`lib/mysql-connector-j-8.3.0.jar`)
- **Target Java**: Compatible with Java 8, 11, 17, 21, and 25+

---

## 📁 Project Structure

```
NetworkDeviceInventoryManagementSystem/
│── bin/                         # Compiled bytecode class files
│── lib/
│   └── mysql-connector-j-8.3.0.jar  # MySQL JDBC Driver
│── src/
│   └── com/inventory/
│       ├── Main.java            # Application bootstrap & Look and Feel
│       ├── VerificationTest.java# Automated end-to-end test suite
│       ├── dao/
│       │   ├── DeviceDAO.java   # PreparedStatement CRUD & search/filter queries
│       │   └── UserDAO.java     # User authentication & management queries
│       ├── db/
│       │   ├── DBConnection.java       # Connection provider & config loader
│       │   └── DatabaseInitializer.java# Auto-DDL database & table creation
│       ├── model/
│       │   ├── Device.java      # Network device data model
│       │   ├── Session.java     # Active user session holder
│       │   └── User.java        # User data model
│       └── ui/
│           ├── AdminPanelDialog.java   # Admin management console
│           ├── DBConfigDialog.java     # Live MySQL connection settings dialog
│           ├── DeviceDetailDialog.java # Full device view dialog
│           ├── DeviceFormDialog.java   # Add/Edit form with validation
│           ├── LoginFrame.java         # Sign In & Registration frame
│           └── MainDashboardFrame.java # Dashboard, table, search & sidebar
│── compile.bat                  # Compilation script
│── run.bat                      # Application launcher script
│── run.ps1                      # PowerShell launcher script
│── run_tests.bat                # Automated verification runner
│── db.properties                # MySQL connection configuration
│── schema.sql                   # Standalone database DDL & seed script
└── README.md
```

---

## ⚡ How to Run

### Option 1: Double-Click or Run Script (Windows)
Double-click `run.bat` or execute in PowerShell:
```powershell
.\run.bat
```
*(or `powershell -File .\run.ps1`)*

### Option 2: Run Automated Verification Tests
```powershell
.\run_tests.bat
```

### Option 3: Manual Command Line
```powershell
java -cp "bin;lib\mysql-connector-j-8.3.0.jar;src" com.inventory.Main
```

---

## 🔐 Default Credentials

| Username | Password | Role | Notes |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | **ADMIN** | Full administrative privileges, access to Admin Panel |
| `tech1` | `tech123` | **STAFF** | Standard inventory manager |

