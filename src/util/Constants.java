package util;

/**
 * This class stores all constant values used throughout the application
 * Using constants makes it easy to change values in one place
 */
public class Constants {
    
    // ============ APPLICATION INFORMATION ============
    public static final String APP_NAME = "DIU Transport";
    public static final String APP_VERSION = "1.0.0";
    public static final String DEVELOPER = "DIU CSE Department";
    public static final String COPYRIGHT = "© 2024 Daffodil International University";
    
    // ============ DATABASE CONFIGURATION ============
    public static final String DB_NAME = "diu_transport.db";
    public static final String DB_URL = "jdbc:sqlite:" + DB_NAME;
    public static final String DB_BACKUP_DIR = "backups/";
    
    // ============ FILE PATHS ============
    public static final String LOG_FILE = "logs/transport_system.log";
    public static final String CONFIG_FILE = "config.properties";
    public static final String IMAGES_DIR = "images/";
    
    // ============ USER ROLES ============
    public static final String ROLE_ADMIN = "Admin";
    public static final String ROLE_STUDENT = "Student";
    public static final String ROLE_TEACHER = "Teacher";
    public static final String ROLE_STAFF = "Staff";
    public static final String ROLE_DRIVER = "Driver";
    
    // ============ TRANSPORT CARD STATUS ============
    public static final String CARD_ACTIVE = "Active";
    public static final String CARD_EXPIRED = "Expired";
    public static final String CARD_PENDING = "Pending";
    public static final String CARD_SUSPENDED = "Suspended";
    
    // ============ PAYMENT STATUS ============
    public static final String PAYMENT_PAID = "Paid";
    public static final String PAYMENT_DUE = "Due";
    public static final String PAYMENT_PARTIAL = "Partial";
    public static final String PAYMENT_OVERDUE = "Overdue";
    public static final String PAYMENT_REFUNDED = "Refunded";
    
    // ============ BUS STATUS ============
    public static final String BUS_ACTIVE = "Active";
    public static final String BUS_MAINTENANCE = "Maintenance";
    public static final String BUS_OUT_OF_SERVICE = "Out of Service";
    public static final String BUS_RESERVED = "Reserved";
    
    // ============ DRIVER SHIFTS ============
    public static final String SHIFT_MORNING = "Morning";
    public static final String SHIFT_EVENING = "Evening";
    public static final String SHIFT_NIGHT = "Night";
    public static final String SHIFT_FLEXIBLE = "Flexible";
    
    // ============ LOST & FOUND STATUS ============
    public static final String LF_CLAIMED = "Claimed";
    public static final String LF_UNCLAIMED = "Unclaimed";
    public static final String LF_PROCESSING = "Processing";
    
    // ============ NOTIFICATION TYPES ============
    public static final String NOTIF_SCHEDULE_UPDATE = "Schedule Update";
    public static final String NOTIF_CANCELLATION = "Cancellation";
    public static final String NOTIF_NEW_ROUTE = "New Route";
    public static final String NOTIF_SPECIAL_TRIP = "Special Trip";
    public static final String NOTIF_SYSTEM_ALERT = "System Alert";
    
    // ============ FINE STATUS ============
    public static final String FINE_PENDING = "Pending";
    public static final String FINE_PAID = "Paid";
    public static final String FINE_OVERDUE = "Overdue";
    public static final String FINE_WAIVED = "Waived";
    public static final String FINE_APPEALED = "Appealed";
    
    // ============ DATE FORMATS ============
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    // ============ VALIDATION CONSTRAINTS ============
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_PASSWORD_LENGTH = 20;
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 20;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MAX_PHONE_LENGTH = 15;
    
    // ============ PRICING CONSTANTS ============
    public static final double STUDENT_TRANSPORT_FEE = 5000.00;
    public static final double TEACHER_TRANSPORT_FEE = 3000.00;
    public static final double STAFF_TRANSPORT_FEE = 2000.00;
    
    // ============ LIMIT CONSTANTS ============
    public static final int MAX_BUS_CAPACITY = 60;
    public static final int MAX_SCHEDULE_PER_DAY = 10;
    public static final int MAX_NOTIFICATIONS = 50;
    
    // ============ GUI CONSTANTS ============
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 700;
    public static final String APP_ICON_PATH = "images/icon.png";
    
    // ============ ERROR MESSAGES ============
    public static final String ERROR_DB_CONNECTION = "Database connection failed!";
    public static final String ERROR_LOGIN_FAILED = "Invalid username or password!";
    public static final String ERROR_REQUIRED_FIELD = "This field is required!";
    public static final String ERROR_INVALID_EMAIL = "Invalid email format!";
    public static final String ERROR_INVALID_PHONE = "Invalid phone number!";
    public static final String ERROR_ACCESS_DENIED = "Access denied! You don't have permission.";
    
    // ============ SUCCESS MESSAGES ============
    public static final String SUCCESS_LOGIN = "Login successful!";
    public static final String SUCCESS_REGISTRATION = "Registration successful!";
    public static final String SUCCESS_UPDATE = "Update successful!";
    public static final String SUCCESS_DELETE = "Delete successful!";
    public static final String SUCCESS_PAYMENT = "Payment successful!";
    
    // ============ REGEX PATTERNS ============
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PHONE_REGEX = "^01[3-9]\\d{8}$"; // Bangladesh format
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_]{3,20}$";
    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{6,20}$";
    
    // ============ COLORS (for GUI) ============
    public static final String COLOR_PRIMARY = "#2C3E50";    // Dark blue
    public static final String COLOR_SECONDARY = "#3498DB";  // Blue
    public static final String COLOR_SUCCESS = "#27AE60";    // Green
    public static final String COLOR_WARNING = "#F39C12";    // Orange
    public static final String COLOR_DANGER = "#E74C3C";     // Red
    public static final String COLOR_LIGHT = "#ECF0F1";      // Light gray
    public static final String COLOR_DARK = "#2C3E50";       // Dark
}