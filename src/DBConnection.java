import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DBConnection.java
 * Centralized JDBC Connection Manager.
 * Stores MySQL connection details in one place and provides automatic connection on startup.
 * Automatically creates tables and seeds default data without repeated credential prompts.
 */
public class DBConnection {

    // Database connection parameters - Stored in one place
    private static final String URL = "jdbc:mysql://localhost:3306/campus_events_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String USER = "root";
    private static String PASSWORD = "hayden15"; // Confirmed MySQL root password

    // Static block to load the MySQL Connector/J driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Warning: MySQL JDBC Driver not found in classpath.");
        }
    }

    /**
     * Returns a live JDBC Connection using the configured credentials.
     * Automatically attempts known credentials if root password varies.
     * @return Connection object
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            // Auto-fallback in case password varies
            String[] alternatives = {"hayden15", "Vedha@0523", "root", ""};
            for (String alt : alternatives) {
                if (!alt.equals(PASSWORD)) {
                    try {
                        Connection conn = DriverManager.getConnection(URL, USER, alt);
                        PASSWORD = alt; // update for subsequent calls
                        return conn;
                    } catch (SQLException ignored) {
                    }
                }
            }
            throw e;
        }
    }

    /**
     * Optional helper to override credentials if needed.
     */
    public static void setCredentials(String username, String password) {
        USER = username;
        PASSWORD = password;
    }

    /**
     * Checks if the MySQL database is reachable without throwing unhandled exceptions.
     * @return true if connected, false otherwise
     */
    public static boolean isConnectionAvailable() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Ensures all required tables and starter records exist in MySQL on startup.
     */
    public static void initializeDatabase() {
        if (!isConnectionAvailable()) {
            System.err.println("Database is currently offline. Standalone in-memory mode will be used.");
            return;
        }

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users ("
                       + "user_id INT AUTO_INCREMENT PRIMARY KEY, "
                       + "username VARCHAR(50) NOT NULL UNIQUE, "
                       + "password VARCHAR(100) NOT NULL, "
                       + "role VARCHAR(20) NOT NULL, "
                       + "full_name VARCHAR(100) NOT NULL, "
                       + "email VARCHAR(100) NOT NULL UNIQUE)");

            try {
                stmt.execute("ALTER TABLE users MODIFY COLUMN password VARCHAR(100) NOT NULL");
            } catch (SQLException ignored) {
            }

            // 2. Events Table
            stmt.execute("CREATE TABLE IF NOT EXISTS events ("
                       + "event_id INT AUTO_INCREMENT PRIMARY KEY, "
                       + "title VARCHAR(120) NOT NULL, "
                       + "category VARCHAR(50) NOT NULL, "
                       + "event_date VARCHAR(20) NOT NULL, "
                       + "time_slot VARCHAR(20) NOT NULL, "
                       + "fee DOUBLE NOT NULL DEFAULT 0.0, "
                       + "venue VARCHAR(120) NOT NULL, "
                       + "required_skills VARCHAR(255), "
                       + "eligible_dept VARCHAR(100) DEFAULT 'All', "
                       + "eligible_year INT DEFAULT 0, "
                       + "event_format VARCHAR(20) DEFAULT 'Offline', "
                       + "target_skill_level VARCHAR(20) DEFAULT 'All', "
                       + "start_time VARCHAR(20) DEFAULT '09:00 AM', "
                       + "end_time VARCHAR(20) DEFAULT '05:00 PM', "
                       + "max_capacity INT DEFAULT 100)");

            try { stmt.execute("ALTER TABLE events ADD COLUMN event_format VARCHAR(20) DEFAULT 'Offline'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE events ADD COLUMN target_skill_level VARCHAR(20) DEFAULT 'All'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE events ADD COLUMN start_time VARCHAR(20) DEFAULT '09:00 AM'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE events ADD COLUMN end_time VARCHAR(20) DEFAULT '05:00 PM'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE events ADD COLUMN max_capacity INT DEFAULT 100"); } catch (SQLException ignored) {}

            // 3. Students Table
            stmt.execute("CREATE TABLE IF NOT EXISTS students ("
                       + "student_id INT AUTO_INCREMENT PRIMARY KEY, "
                       + "name VARCHAR(100) NOT NULL, "
                       + "email VARCHAR(100) NOT NULL UNIQUE, "
                       + "department VARCHAR(50) NOT NULL, "
                       + "academic_year INT NOT NULL, "
                       + "budget DOUBLE NOT NULL DEFAULT 0.0, "
                       + "interests VARCHAR(255), "
                       + "skills VARCHAR(255), "
                       + "availability VARCHAR(50), "
                       + "skill_level VARCHAR(20) DEFAULT 'Beginner', "
                       + "preferred_days VARCHAR(50) DEFAULT 'Both', "
                       + "career_goals VARCHAR(255) DEFAULT 'Improve programming skills', "
                       + "preferred_format VARCHAR(20) DEFAULT 'Either', "
                       + "free_only TINYINT(1) DEFAULT 0)");

            try { stmt.execute("ALTER TABLE students ADD COLUMN skill_level VARCHAR(20) DEFAULT 'Beginner'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN preferred_days VARCHAR(50) DEFAULT 'Both'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN career_goals VARCHAR(255) DEFAULT 'Improve programming skills'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN preferred_format VARCHAR(20) DEFAULT 'Either'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE students ADD COLUMN free_only TINYINT(1) DEFAULT 0"); } catch (SQLException ignored) {}

            // 4. Registrations Table
            stmt.execute("CREATE TABLE IF NOT EXISTS registrations ("
                       + "reg_id INT AUTO_INCREMENT PRIMARY KEY, "
                       + "student_id INT DEFAULT 0, "
                       + "student_name VARCHAR(100) NOT NULL, "
                       + "student_email VARCHAR(100) NOT NULL, "
                       + "event_id INT NOT NULL, "
                       + "event_title VARCHAR(120) NOT NULL, "
                       + "registered_at VARCHAR(50) NOT NULL, "
                       + "FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE)");

            // 5. Seed coordinator account if missing (students register their own accounts)
            String coordinatorHash = UserDAO.hashPassword("admin123");
            stmt.execute("INSERT IGNORE INTO users (username, password, role, full_name, email) VALUES "
                       + "('coordinator', '" + coordinatorHash + "', 'COORDINATOR', 'Dr. Sarah Jenkins', 'sarah.jenkins@campus.edu')");
            stmt.execute("UPDATE users SET password = '" + coordinatorHash + "' WHERE username = 'coordinator' AND password = 'admin123'");
            stmt.execute("DELETE FROM users WHERE username = 'student' AND role = 'STUDENT'");

            // 6. Seed events if events table is empty
            EventDAO eventDAO = new EventDAO();
            if (eventDAO.getAllEvents().isEmpty()) {
                EventManager seeder = new EventManager();
                seeder.loadSampleEvents();
                for (Event ev : seeder.getAllEvents()) {
                    eventDAO.addEvent(ev);
                }
            }

            System.out.println("MySQL database 'campus_events_db' successfully verified and initialized.");

        } catch (SQLException e) {
            System.err.println("Error initializing database schema: " + e.getMessage());
        }
    }

    public static String getURL() {
        return URL;
    }

    public static String getUser() {
        return USER;
    }
}
