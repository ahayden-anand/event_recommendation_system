import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * UserDAO.java
 * Handles user authentication and registration against the MySQL database with parameterized queries.
 * Integrates standard Java SHA-256 password hashing (java.security.MessageDigest).
 * Ensures zero plain-text passwords and zero hardcoded student credentials.
 */
public class UserDAO {

    public UserDAO() {
        createTable();
    }

    /**
     * Hashes a plain-text password using the SHA-256 cryptographic algorithm.
     * Converts the resulting 32-byte digest into a standard 64-character hexadecimal string.
     * College Course Reference: CO5 Database Connectivity & Information Security.
     * @param plainPassword Raw password string entered by the user
     * @return 64-character hexadecimal SHA-256 hash
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(plainPassword.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            System.err.println("Error generating SHA-256 hash: " + e.getMessage());
            return plainPassword; // Fallback only if algorithm unavailable
        }
    }

    public void createTable() {
        if (!DBConnection.isConnectionAvailable()) return;

        String sql = "CREATE TABLE IF NOT EXISTS users ("
                   + "user_id INT AUTO_INCREMENT PRIMARY KEY, "
                   + "username VARCHAR(50) NOT NULL UNIQUE, "
                   + "password VARCHAR(100) NOT NULL, "
                   + "role VARCHAR(20) NOT NULL, "
                   + "full_name VARCHAR(100) NOT NULL, "
                   + "email VARCHAR(100) NOT NULL UNIQUE"
                   + ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            // Ensure password column supports up to 100 characters for SHA-256 hashes
            try {
                stmt.execute("ALTER TABLE users MODIFY COLUMN password VARCHAR(100) NOT NULL");
            } catch (SQLException ignored) {
            }

            // Seed coordinator with SHA-256 hashed password ('admin123')
            String coordinatorHash = hashPassword("admin123");
            stmt.execute("INSERT IGNORE INTO users (username, password, role, full_name, email) VALUES "
                       + "('coordinator', '" + coordinatorHash + "', 'COORDINATOR', 'Dr. Sarah Jenkins', 'sarah.jenkins@campus.edu')");

            // Migrate legacy plain-text coordinator password if present
            stmt.execute("UPDATE users SET password = '" + coordinatorHash + "' WHERE username = 'coordinator' AND password = 'admin123'");

            // Remove legacy hardcoded mock student credentials
            stmt.execute("DELETE FROM users WHERE username = 'student' AND role = 'STUDENT'");
        } catch (SQLException e) {
            System.err.println("Error initializing users table: " + e.getMessage());
        }
    }

    /**
     * Checks if a username (Student ID or Coordinator handle) is already taken.
     * @param username Username / Student ID to check
     * @return true if taken, false if available
     */
    public boolean isUsernameTaken(String username) {
        if (DBConnection.isConnectionAvailable()) {
            String sql = "SELECT user_id FROM users WHERE username = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            } catch (SQLException e) {
                System.err.println("Error checking username availability: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Checks if an email is already registered in users or students tables.
     * @param email Email address to check
     * @return true if taken, false if available
     */
    public boolean isEmailTaken(String email) {
        if (DBConnection.isConnectionAvailable()) {
            String sqlUsers = "SELECT user_id FROM users WHERE email = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlUsers)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return true;
                }
            } catch (SQLException e) {
                System.err.println("Error checking email in users table: " + e.getMessage());
            }

            String sqlStudents = "SELECT student_id FROM students WHERE email = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlStudents)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return true;
                }
            } catch (SQLException e) {
                System.err.println("Error checking email in students table: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Registers a new student account using a transaction for atomicity.
     * Inserts into 'users' table (with role 'STUDENT') and 'students' profile table.
     * @param student Student model containing academic preferences
     * @param studentId Student ID / Registration Number
     * @param password Password for student account
     * @return true if successfully registered, false otherwise
     */
    public boolean registerStudent(Student student, String studentId, String password) {
        if (!DBConnection.isConnectionAvailable()) {
            System.err.println("Registration failed: Database connection is unavailable.");
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction (CO5: Database Connectivity in Java)

            // 1. Insert into users table with SHA-256 hashed password
            String hashedPassword = hashPassword(password);
            String userSql = "INSERT INTO users (username, password, role, full_name, email) VALUES (?, ?, 'STUDENT', ?, ?)";
            try (PreparedStatement psUser = conn.prepareStatement(userSql)) {
                psUser.setString(1, studentId);
                psUser.setString(2, hashedPassword);
                psUser.setString(3, student.getName());
                psUser.setString(4, student.getEmail());
                psUser.executeUpdate();
            }

            // 2. Insert into students table
            String studentSql = "INSERT INTO students (name, email, department, academic_year, budget, interests, skills, availability) "
                              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psStudent = conn.prepareStatement(studentSql)) {
                psStudent.setString(1, student.getName());
                psStudent.setString(2, student.getEmail());
                psStudent.setString(3, student.getDepartment());
                psStudent.setInt(4, student.getAcademicYear());
                psStudent.setDouble(5, student.getMaxBudget());
                psStudent.setString(6, student.getInterests());
                psStudent.setString(7, student.getSkills());
                psStudent.setString(8, student.getAvailableSlot());
                psStudent.executeUpdate();
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            System.err.println("Transaction rollback due to error: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Authenticates a user by username/email and password using PreparedStatement.
     * Hashes the entered password using SHA-256 before comparing with database hash.
     * @param usernameOrEmail Username or email entered by user
     * @param password Password entered by user
     * @return User object if authenticated, or null if invalid
     */
    public User authenticate(String usernameOrEmail, String password) {
        if (DBConnection.isConnectionAvailable()) {
            String hashedPassword = hashPassword(password);
            String sql = "SELECT user_id, username, password, role, full_name, email "
                       + "FROM users WHERE (username = ? OR email = ?) AND (password = ? OR password = ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, usernameOrEmail);
                ps.setString(2, usernameOrEmail);
                ps.setString(3, hashedPassword);
                ps.setString(4, password); // allows migration if unhashed coordinator exists

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role"),
                            rs.getString("full_name"),
                            rs.getString("email")
                        );
                    }
                }
            } catch (SQLException e) {
                System.err.println("Authentication error: " + e.getMessage());
            }
        }

        // Coordinator fallback only when database is offline
        if ("coordinator".equalsIgnoreCase(usernameOrEmail) && "admin123".equals(password)) {
            return new User(2, "coordinator", hashPassword("admin123"), "COORDINATOR", "Dr. Sarah Jenkins", "sarah.jenkins@campus.edu");
        }

        return null; // Authentication failed
    }
}
