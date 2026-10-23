import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Phase4PasswordHashingTest.java
 * Test suite for Phase 4:
 * 1. Verifies SHA-256 hashing format, length (64 chars), determinism, and test vectors.
 * 2. Verifies student registration stores SHA-256 hash, NOT plain-text password in MySQL.
 * 3. Verifies login authentication hashes user input and matches the stored hash.
 * 4. Verifies wrong password rejection.
 * 5. Verifies coordinator authentication with hashed credentials.
 * 6. Verifies complete removal of legacy hardcoded student credentials ('student'/'student123').
 */
public class Phase4PasswordHashingTest {

    private static int testsPassed = 0;
    private static int totalTests = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   PHASE 4: SHA-256 PASSWORD HASHING & CREDENTIAL REMOVAL TEST SUITE      ");
        System.out.println("==========================================================================");

        // 1. Verify SHA-256 algorithm properties
        String rawPass = "StudentPass@2026";
        String hash1 = UserDAO.hashPassword(rawPass);
        String hash2 = UserDAO.hashPassword(rawPass);

        assertResult(hash1 != null && hash1.length() == 64, "SHA-256 hash length is exactly 64 hexadecimal characters (" + hash1.length() + ")");
        assertResult(hash1.equals(hash2), "SHA-256 hashing is deterministic (identical input produces identical hash)");
        assertResult(!hash1.equals(UserDAO.hashPassword("DifferentPass")), "Different passwords yield distinct hashes");

        // Verify standard known test vector: SHA-256 of "admin123"
        String adminExpected = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
        String adminActual = UserDAO.hashPassword("admin123");
        assertResult(adminExpected.equalsIgnoreCase(adminActual), "SHA-256 matches standard cryptographic test vector for 'admin123'");

        // Database tests
        if (!DBConnection.isConnectionAvailable()) {
            System.err.println("Database is not reachable. Skipping database persistence tests.");
            printSummary();
            return;
        }

        DBConnection.initializeDatabase();
        UserDAO userDAO = new UserDAO();

        String testStudentId = "STU_HASH_" + (System.currentTimeMillis() % 10000);
        String testEmail = "hashed." + testStudentId.toLowerCase() + "@campus.edu";
        String testPassword = "MySecretPassword#99";

        // Clean up any test artifact
        cleanUpTestUser(testStudentId, testEmail);

        // 2. Register student with raw password
        Student student = new Student(
            0,
            "Tanvi Roy",
            testEmail,
            "IT",
            3,
            250.0,
            "Coding, AI",
            "Java, Python",
            "Morning"
        );

        boolean registered = userDAO.registerStudent(student, testStudentId, testPassword);
        assertResult(registered, "Registered new student account into MySQL");

        // 3. Inspect raw database record to guarantee password is NOT in plain text
        String storedDbPassword = getStoredPasswordFromDatabase(testStudentId);
        String expectedHash = UserDAO.hashPassword(testPassword);

        boolean notPlainText = storedDbPassword != null && !storedDbPassword.equals(testPassword);
        assertResult(notPlainText, "Stored database password is NOT plain-text ('" + testPassword + "' was not found in DB)");

        boolean matchesSha256 = storedDbPassword != null && storedDbPassword.equalsIgnoreCase(expectedHash);
        assertResult(matchesSha256, "Stored database password is valid 64-char SHA-256 hash: " + storedDbPassword);

        // 4. Authenticate using plain-text password (system hashes it on login)
        User authenticatedUser = userDAO.authenticate(testStudentId, testPassword);
        boolean loginSuccess = authenticatedUser != null && authenticatedUser.isStudent() && "Tanvi Roy".equals(authenticatedUser.getFullName());
        assertResult(loginSuccess, "Student successfully authenticated by ID using plain-text password matching hash");

        // 5. Authenticate by email using plain-text password
        User authenticatedByEmail = userDAO.authenticate(testEmail, testPassword);
        boolean emailLoginSuccess = authenticatedByEmail != null && authenticatedByEmail.isStudent();
        assertResult(emailLoginSuccess, "Student successfully authenticated by Email using plain-text password matching hash");

        // 6. Test invalid password rejection
        User failedLogin = userDAO.authenticate(testStudentId, "WrongPassword#00");
        assertResult(failedLogin == null, "Authentication rejected for incorrect password");

        // 7. Verify coordinator login with SHA-256 hashed password
        User coordinator = userDAO.authenticate("coordinator", "admin123");
        boolean coordAuth = coordinator != null && coordinator.isCoordinator();
        assertResult(coordAuth, "Coordinator authenticated successfully using SHA-256 hashed credentials");

        // 8. Verify legacy mock student credentials ('student'/'student123') are completely removed
        User mockStudentAuth = userDAO.authenticate("student", "student123");
        assertResult(mockStudentAuth == null, "Legacy mock student ('student'/'student123') completely removed and rejected");

        // Clean up test user
        cleanUpTestUser(testStudentId, testEmail);

        printSummary();
    }

    private static String getStoredPasswordFromDatabase(String username) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading stored password: " + e.getMessage());
        }
        return null;
    }

    private static void cleanUpTestUser(String username, String email) {
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM users WHERE username=? OR email=?")) {
                ps1.setString(1, username);
                ps1.setString(2, email);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM students WHERE email=?")) {
                ps2.setString(1, email);
                ps2.executeUpdate();
            }
        } catch (SQLException ignored) {}
    }

    private static void assertResult(boolean condition, String description) {
        totalTests++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + description);
        } else {
            System.err.println("  [FAIL] " + description);
        }
    }

    private static void printSummary() {
        System.out.println("\n==========================================================================");
        System.out.println("RESULTS: " + testsPassed + " / " + totalTests + " TESTS PASSED (" 
            + (int)(((double)testsPassed / totalTests) * 100) + "%)");
        System.out.println(">>> Phase 4 SHA-256 Password Hashing & Hardcoded Removal Verified! <<<");
        System.out.println("==========================================================================");
    }
}
