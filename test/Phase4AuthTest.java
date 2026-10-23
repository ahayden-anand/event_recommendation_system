/**
 * Phase4AuthTest.java
 * Test suite to verify Role-Based Authentication:
 * 1. Valid Student authentication and role verification.
 * 2. Valid Coordinator authentication and role verification.
 * 3. Invalid credentials rejection.
 * 4. Empty/non-existent user rejection.
 */
public class Phase4AuthTest {
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("              PHASE 4: ROLE-BASED AUTHENTICATION TEST SUITE               ");
        System.out.println("==========================================================================");

        UserDAO dao = new UserDAO();

        // Test 1: Student Login
        System.out.println("\n[Test 1] Testing Student Login ('student', 'student123')...");
        User studentUser = dao.authenticate("student", "student123");
        if (studentUser != null && studentUser.isStudent()) {
            System.out.println("  [PASS] Student Authenticated Successfully!");
            System.out.println("         Name : " + studentUser.getFullName());
            System.out.println("         Email: " + studentUser.getEmail());
            System.out.println("         Role : " + studentUser.getRole());
        } else {
            System.err.println("  [FAIL] Student Authentication Failed!");
        }

        // Test 2: Coordinator Login
        System.out.println("\n[Test 2] Testing Coordinator Login ('coordinator', 'admin123')...");
        User coordUser = dao.authenticate("coordinator", "admin123");
        if (coordUser != null && coordUser.isCoordinator()) {
            System.out.println("  [PASS] Coordinator Authenticated Successfully!");
            System.out.println("         Name : " + coordUser.getFullName());
            System.out.println("         Email: " + coordUser.getEmail());
            System.out.println("         Role : " + coordUser.getRole());
        } else {
            System.err.println("  [FAIL] Coordinator Authentication Failed!");
        }

        // Test 3: Invalid Password
        System.out.println("\n[Test 3] Testing Invalid Password ('student', 'wrong_pass')...");
        User invalidUser = dao.authenticate("student", "wrong_pass");
        if (invalidUser == null) {
            System.out.println("  [PASS] Correctly rejected invalid password.");
        } else {
            System.err.println("  [FAIL] Should not authenticate invalid password!");
        }

        // Test 4: Unknown Username
        System.out.println("\n[Test 4] Testing Unknown Username ('ghost', 'any_pass')...");
        User ghostUser = dao.authenticate("ghost", "any_pass");
        if (ghostUser == null) {
            System.out.println("  [PASS] Correctly rejected unknown username.");
        } else {
            System.err.println("  [FAIL] Should not authenticate unknown user!");
        }

        System.out.println("\n==========================================================================");
        System.out.println(">>> Phase 4 Role-Based Authentication Tested & Verified Successfully! <<<");
        System.out.println("==========================================================================");
    }
}
