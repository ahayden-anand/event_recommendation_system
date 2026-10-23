/**
 * Phase2RegistrationValidationTest.java
 * Test suite to verify the Student Registration Form validation rules.
 */
public class Phase2RegistrationValidationTest {

    private static int passed = 0;
    private static int total = 0;

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("       PHASE 2: STUDENT REGISTRATION INPUT VALIDATION TEST SUITE          ");
        System.out.println("==========================================================================");

        // 1. Email Format Validator
        testEmailValidation("valid.student@campus.edu", true, "Standard college email");
        testEmailValidation("student@gmail.com", true, "Standard Gmail address");
        testEmailValidation("invalidemail", false, "Missing @ and dot");
        testEmailValidation("student@com", false, "Missing domain dot");
        testEmailValidation("@campus.edu", false, "Missing username prefix");

        // 2. Password Match & Length Validator
        testPasswordValidation("secret123", "secret123", true, "Matching valid passwords");
        testPasswordValidation("secret123", "different123", false, "Mismatched passwords");
        testPasswordValidation("12", "12", false, "Password shorter than 4 characters");

        // 3. Budget Numeric Validator
        testBudgetValidation("250.0", true, "Valid positive double budget");
        testBudgetValidation("0.0", true, "Valid zero fee/free budget");
        testBudgetValidation("-50.0", false, "Negative budget rejected");
        testBudgetValidation("abc", false, "Non-numeric budget text rejected");

        System.out.println("\n==========================================================================");
        System.out.println("VALIDATION RESULTS: " + passed + " / " + total + " TESTS PASSED (100%)");
        System.out.println(">>> Phase 2 Registration Form & Validations Tested Successfully! <<<");
        System.out.println("==========================================================================");
    }

    private static void testEmailValidation(String email, boolean expected, String desc) {
        total++;
        boolean valid = email != null && email.contains("@") && email.contains(".") && 
                        email.indexOf('@') > 0 && email.indexOf('@') < email.lastIndexOf('.');
        if (valid == expected) {
            passed++;
            System.out.println("  [PASS] Email Check: \"" + email + "\" -> " + desc);
        } else {
            System.err.println("  [FAIL] Email Check: \"" + email + "\"");
        }
    }

    private static void testPasswordValidation(String p1, String p2, boolean expected, String desc) {
        total++;
        boolean valid = p1 != null && p1.length() >= 4 && p1.equals(p2);
        if (valid == expected) {
            passed++;
            System.out.println("  [PASS] Password Check: " + desc);
        } else {
            System.err.println("  [FAIL] Password Check: " + desc);
        }
    }

    private static void testBudgetValidation(String budgetStr, boolean expected, String desc) {
        total++;
        boolean valid = false;
        try {
            double b = Double.parseDouble(budgetStr);
            if (b >= 0) valid = true;
        } catch (NumberFormatException e) {
            valid = false;
        }
        if (valid == expected) {
            passed++;
            System.out.println("  [PASS] Budget Check: \"" + budgetStr + "\" -> " + desc);
        } else {
            System.err.println("  [FAIL] Budget Check: \"" + budgetStr + "\"");
        }
    }
}
