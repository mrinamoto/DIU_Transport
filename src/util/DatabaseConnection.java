package util;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class handles database connection using SQLite
 * SQLite is perfect for beginners - no server setup needed
 */
public class DatabaseConnection {
    private static Connection connection = null;
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());
   
    // Private constructor to prevent instantiation
    private DatabaseConnection() {}
   
    /**
     * Get database connection with auto-reconnect feature
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                establishConnection();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Connection check failed, reconnecting...", e);
            establishConnection();
        }
        return connection;
    }
   
    /**
     * Establish new database connection
     */
    private static synchronized void establishConnection() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
           
            // Check if database file exists, create directory if needed
            File dbFile = new File(Constants.DB_NAME);
            File parentDir = dbFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (created) {
                    logger.log(Level.FINE, "Created database directory: {0}", parentDir.getAbsolutePath());
                }
            }
           
            // Create connection to database file
            connection = DriverManager.getConnection(Constants.DB_URL);
           
            // Enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
           
            logger.info("Database connected successfully!");
            createTables(); // Create tables if they don't exist
           
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, "SQLite JDBC driver not found!", e);
            throw new RuntimeException("SQLite JDBC driver not found. Please add sqlite-jdbc jar to classpath.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to connect to database: {0}", Constants.DB_URL);
            logger.log(Level.SEVERE, "SQLException details:", e);
            connection = null;
        }
    }
   
    /**
     * Create all necessary tables if they don't exist
     */
    private static void createTables() {
        String[] tables = {
            // Users table
            "CREATE TABLE IF NOT EXISTS users (" +
            "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "    username TEXT UNIQUE NOT NULL," +
            "    password TEXT NOT NULL," +
            "    role TEXT NOT NULL CHECK(role IN ('Admin', 'Student', 'Teacher', 'Staff', 'Driver'))," +
            "    name TEXT NOT NULL," +
            "    email TEXT UNIQUE," +
            "    phone TEXT," +
            "    department TEXT," +
            "    semester INTEGER," +
            "    position TEXT," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
           
            // Buses table
            "CREATE TABLE IF NOT EXISTS buses (" +
            "    bus_id TEXT PRIMARY KEY," +
            "    bus_number TEXT UNIQUE NOT NULL," +
            "    registration_number TEXT UNIQUE," +
            "    capacity INTEGER CHECK(capacity > 0 AND capacity <= 100)," +
            "    status TEXT DEFAULT 'Active' CHECK(status IN ('Active', 'Maintenance', 'Out of Service', 'Reserved'))," +
            "    route_id TEXT," +
            "    model TEXT," +
            "    year INTEGER," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
           
            // Routes table
            "CREATE TABLE IF NOT EXISTS routes (" +
            "    route_id TEXT PRIMARY KEY," +
            "    route_name TEXT NOT NULL," +
            "    stops TEXT," +
            "    timing TEXT," +
            "    distance REAL," +
            "    estimated_time TEXT," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
           
            // Schedules table
            "CREATE TABLE IF NOT EXISTS schedules (" +
            "    schedule_id TEXT PRIMARY KEY," +
            "    bus_id TEXT," +
            "    route_id TEXT," +
            "    departure_time TEXT NOT NULL," +
            "    arrival_time TEXT NOT NULL," +
            "    days_of_week TEXT," +
            "    is_active INTEGER DEFAULT 1," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (bus_id) REFERENCES buses(bus_id) ON DELETE SET NULL," +
            "    FOREIGN KEY (route_id) REFERENCES routes(route_id) ON DELETE SET NULL" +
            ")",
           
            // Transport Cards table
            "CREATE TABLE IF NOT EXISTS transport_cards (" +
            "    card_id TEXT PRIMARY KEY," +
            "    user_id INTEGER NOT NULL," +
            "    issue_date TEXT NOT NULL," +
            "    expiry_date TEXT NOT NULL," +
            "    status TEXT DEFAULT 'Pending' CHECK(status IN ('Active', 'Expired', 'Pending', 'Suspended'))," +
            "    payment_status TEXT DEFAULT 'Due' CHECK(payment_status IN ('Paid', 'Due', 'Partial', 'Overdue'))," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
            ")",
           
            // Billing table
            "CREATE TABLE IF NOT EXISTS billing (" +
            "    billing_id TEXT PRIMARY KEY," +
            "    user_id INTEGER NOT NULL," +
            "    amount REAL NOT NULL CHECK(amount > 0)," +
            "    semester TEXT NOT NULL," +
            "    payment_status TEXT DEFAULT 'Due' CHECK(payment_status IN ('Paid', 'Due', 'Partial', 'Overdue', 'Refunded'))," +
            "    due_date TEXT NOT NULL," +
            "    payment_date TEXT," +
            "    payment_method TEXT," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
            ")",
           
            // Fines table
            "CREATE TABLE IF NOT EXISTS fines (" +
            "    fine_id TEXT PRIMARY KEY," +
            "    user_id INTEGER NOT NULL," +
            "    description TEXT NOT NULL," +
            "    amount REAL NOT NULL CHECK(amount > 0)," +
            "    fine_type TEXT," +
            "    issue_date TEXT NOT NULL," +
            "    due_date TEXT NOT NULL," +
            "    status TEXT DEFAULT 'Pending' CHECK(status IN ('Pending', 'Paid', 'Overdue', 'Waived', 'Appealed'))," +
            "    issued_by TEXT," +
            "    violation_details TEXT," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
            ")",
           
            // Drivers table
            "CREATE TABLE IF NOT EXISTS drivers (" +
            "    driver_id TEXT PRIMARY KEY," +
            "    name TEXT NOT NULL," +
            "    phone TEXT," +
            "    license_number TEXT UNIQUE," +
            "    assigned_bus TEXT," +
            "    shift TEXT CHECK(shift IN ('Morning', 'Evening', 'Night', 'Flexible'))," +
            "    license_expiry TEXT," +
            "    experience_years INTEGER DEFAULT 0," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (assigned_bus) REFERENCES buses(bus_id) ON DELETE SET NULL" +
            ")",
           
            // Employees table (non-driver staff)
            "CREATE TABLE IF NOT EXISTS employees (" +
            "    employee_id TEXT PRIMARY KEY," +
            "    name TEXT NOT NULL," +
            "    role TEXT CHECK(role IN ('Maintenance', 'Transport Officer', 'Helper', 'Supervisor', 'Administrator'))," +
            "    phone TEXT," +
            "    email TEXT," +
            "    address TEXT," +
            "    join_date TEXT," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
           
            // Notifications table
            "CREATE TABLE IF NOT EXISTS notifications (" +
            "    notification_id TEXT PRIMARY KEY," +
            "    title TEXT NOT NULL," +
            "    message TEXT NOT NULL," +
            "    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    type TEXT CHECK(type IN ('Schedule Update', 'Cancellation', 'New Route', 'Special Trip', 'System Alert'))," +
            "    is_read INTEGER DEFAULT 0," +
            "    target_role TEXT" +
            ")",
           
            // Lost and Found table
            "CREATE TABLE IF NOT EXISTS lost_found (" +
            "    item_id TEXT PRIMARY KEY," +
            "    item_name TEXT NOT NULL," +
            "    description TEXT," +
            "    found_location TEXT NOT NULL," +
            "    found_date TEXT NOT NULL," +
            "    status TEXT DEFAULT 'Unclaimed' CHECK(status IN ('Claimed', 'Unclaimed', 'Processing'))," +
            "    claimed_by TEXT," +
            "    contact_info TEXT," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")",
           
            // Contacts table
            "CREATE TABLE IF NOT EXISTS contacts (" +
            "    contact_id TEXT PRIMARY KEY," +
            "    name TEXT NOT NULL," +
            "    role TEXT CHECK(role IN ('Transport Manager', 'Transport Officer', 'Emergency Hotline', 'Administrator', 'Support'))," +
            "    phone TEXT NOT NULL," +
            "    email TEXT," +
            "    department TEXT," +
            "    office_hours TEXT DEFAULT '9:00 AM - 5:00 PM'," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        };
       
        try (Statement stmt = connection.createStatement()) {
            // Execute all table creation statements
            for (String tableSQL : tables) {
                stmt.execute(tableSQL);
            }
           
            // Insert default contacts if table is empty
            insertDefaultContacts();
           
            logger.info("Database tables created/verified successfully!");
           
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating tables", e);
        }
    }
   
    /**
     * Insert default emergency contacts
     */
    private static void insertDefaultContacts() {
        String checkSql = "SELECT COUNT(*) FROM contacts";
        String insertSql = "INSERT OR IGNORE INTO contacts (contact_id, name, role, phone, email, department) VALUES (?, ?, ?, ?, ?, ?)";
       
        try (Statement checkStmt = connection.createStatement();
             ResultSet rs = checkStmt.executeQuery(checkSql)) {
           
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement pstmt = connection.prepareStatement(insertSql)) {
                    // Transport Manager
                    pstmt.setString(1, "CONTACT-001");
                    pstmt.setString(2, "Mr. Rahman");
                    pstmt.setString(3, "Administrator");
                    pstmt.setString(4, "01912345678");
                    pstmt.setString(5, "transport@diu.edu.bd");
                    pstmt.setString(6, "Transport Department");
                    pstmt.executeUpdate();
                   
                    // Transport Officer
                    pstmt.setString(1, "CONTACT-002");
                    pstmt.setString(2, "Ms. Fatima");
                    pstmt.setString(3, "Transport Officer");
                    pstmt.setString(4, "01787654321");
                    pstmt.setString(5, "transport.officer@diu.edu.bd");
                    pstmt.setString(6, "Transport Department");
                    pstmt.executeUpdate();
                   
                    // Emergency Hotline
                    pstmt.setString(1, "CONTACT-003");
                    pstmt.setString(2, "Emergency Hotline");
                    pstmt.setString(3, "Emergency Hotline");
                    pstmt.setString(4, "01900112233");
                    pstmt.setString(5, "emergency@diu.edu.bd");
                    pstmt.setString(6, "Security Department");
                    pstmt.executeUpdate();
                   
                    logger.info("Default contacts inserted successfully!");
                }
            }
           
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Could not insert default contacts", e);
        }
    }
   
    /**
     * Execute a SQL query
     */
    public static ResultSet executeQuery(String sql) throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(sql);
    }
   
    /**
     * Execute a SQL update (INSERT, UPDATE, DELETE)
     */
    public static int executeUpdate(String sql) throws SQLException {
        try (Statement stmt = getConnection().createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }
   
    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed.");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error closing connection", e);
        } finally {
            connection = null;
        }
    }
   
    /**
     * Backup database
     */
    public static boolean backupDatabase(String backupPath) {
        try {
            File backupDir = new File(Constants.DB_BACKUP_DIR);
            if (!backupDir.exists()) {
                boolean dirsCreated = backupDir.mkdirs();
                if (!dirsCreated) {
                    logger.log(Level.WARNING, "Failed to create backup directory: {0}", Constants.DB_BACKUP_DIR);
                    return false;
                }
            }
           
            String backupFile = Constants.DB_BACKUP_DIR + backupPath;
            try (Statement stmt = getConnection().createStatement()) {
                stmt.executeUpdate("BACKUP TO '" + backupFile + "'");
                logger.log(Level.INFO, "Database backed up to: {0}", backupFile);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database backup failed", e);
            return false;
        }
    }
    
    /**
     * Restore database from backup
     */
    public static boolean restoreDatabase(String backupFile) {
        File backup = new File(backupFile);
        if (!backup.exists()) {
            logger.log(Level.SEVERE, "Backup file not found: {0}", backupFile);
            return false;
        }
        
        try {
            closeConnection();
            
            // Copy backup file to database location
            File dbFile = new File(Constants.DB_NAME);
            if (dbFile.exists()) {
                boolean deleted = dbFile.delete();
                if (!deleted) {
                    logger.warning("Failed to delete existing database file");
                }
            }
            
            // Re-establish connection and restore
            getConnection();
            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("RESTORE FROM '" + backupFile + "'");
                logger.log(Level.INFO, "Database restored from: {0}", backupFile);
                return true;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database restore failed", e);
            return false;
        }
    }
    
    /**
     * Compact database (VACUUM)
     */
    public static void vacuumDatabase() {
        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute("VACUUM");
            logger.fine("Database vacuum completed.");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Database vacuum failed", e);
        }
    }
    
    /**
     * Get database size in MB
     */
    public static double getDatabaseSize() {
        File dbFile = new File(Constants.DB_NAME);
        if (dbFile.exists()) {
            double sizeInBytes = dbFile.length();
            double sizeInMB = sizeInBytes / (1024 * 1024);
            return Math.round(sizeInMB * 100.0) / 100.0;
        }
        return 0.0;
    }
    
    /**
     * Get total table count
     */
    public static int getTableCount() {
        String sql = "SELECT COUNT(*) FROM sqlite_master WHERE type='table'";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to get table count", e);
        }
        return 0;
    }
    
    /**
     * Get total record count across all tables
     */
    public static int getTotalRecordCount() {
        int totalCount = 0;
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'";
        
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String tableName = rs.getString("name");
                try (Statement countStmt = connection.createStatement();
                     ResultSet countRs = countStmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                    if (countRs.next()) {
                        totalCount += countRs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to get total record count", e);
        }
        return totalCount;
    }
   
    /**
     * Check if database is connected
     */
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Execute batch of SQL statements
     */
    public static boolean executeBatch(String[] sqlStatements) {
        try (Statement stmt = getConnection().createStatement()) {
            connection.setAutoCommit(false);
            
            for (String sql : sqlStatements) {
                stmt.addBatch(sql);
            }
            
            stmt.executeBatch();
            connection.commit();
            logger.fine("Batch execution completed successfully.");
            return true;
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Rollback failed", ex);
            }
            logger.log(Level.SEVERE, "Batch execution failed", e);
            return false;
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                }
            } catch (SQLException e) {
                logger.log(Level.WARNING, "Failed to set auto-commit back to true", e);
            }
        }
    }
    
    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try {
            // Just attempt to get a connection
            DriverManager.getConnection(Constants.DB_URL).close();
            return true;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }
    
    /**
     * Get database metadata
     */
    public static String getDatabaseInfo() {
        StringBuilder info = new StringBuilder();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            info.append("Database Product: ").append(metaData.getDatabaseProductName()).append("\n");
            info.append("Database Version: ").append(metaData.getDatabaseProductVersion()).append("\n");
            info.append("Driver Name: ").append(metaData.getDriverName()).append("\n");
            info.append("Driver Version: ").append(metaData.getDriverVersion()).append("\n");
            info.append("JDBC URL: ").append(metaData.getURL()).append("\n");
            info.append("Username: ").append(metaData.getUserName()).append("\n");
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Failed to get database info", e);
            return "Database information unavailable";
        }
        return info.toString();
    }
}