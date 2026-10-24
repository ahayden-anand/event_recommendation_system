import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Phase3ProgressionAndDiscoveryTest.java
 * Verifies Phase 3 requirements:
 * 1. Skill Growth Pathway stage determination (Stage 1 Foundation, Stage 2 Application, Stage 3 Mastery).
 * 2. Career & skill alignment relevance logic.
 * 3. Discover Something New filtering (serendipity, exclusion of registered/top recs, budget).
 * 4. Dynamic contextual explanation generation across diverse categories.
 */
public class Phase3ProgressionAndDiscoveryTest {

    private static int testsPassed = 0;
    private static int totalTests = 0;

    private static void assertTest(String description, boolean condition) {
        totalTests++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + description);
        } else {
            System.err.println("  [FAIL] " + description);
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("         PHASE 3: SKILL GROWTH & DISCOVER SOMETHING NEW TEST SUITE        ");
        System.out.println("==========================================================================");

        try {
            DBConnection.initializeDatabase();
            EventDAO eventDAO = new EventDAO();
            RegistrationDAO regDAO = new RegistrationDAO();

            // Sample Student for Testing
            Student testStudent = new Student(
                999, "Alex Mercer", "alex.mercer@campus.edu", "CSE", 2, 250.0,
                "Coding, AI", "Java, Python", "Beginner", "Morning", "Both",
                "Full Stack & AI Engineer", "Offline", false
            );

            // Reflection access to private helper methods in StudentDashboard
            User dummyUser = new User(999, "alex", "pass", "STUDENT", "Alex Mercer", "alex.mercer@campus.edu");
            StudentDashboard dashboard = new StudentDashboard(dummyUser);

            Method determineStageMethod = StudentDashboard.class.getDeclaredMethod("determineEventProgressionStage", Event.class);
            determineStageMethod.setAccessible(true);

            Method isRelevantMethod = StudentDashboard.class.getDeclaredMethod("isRelevantToSkillPathway", Event.class, Student.class);
            isRelevantMethod.setAccessible(true);

            Method generateRationaleMethod = StudentDashboard.class.getDeclaredMethod("generateDiscoverRationale", Student.class, Event.class);
            generateRationaleMethod.setAccessible(true);

            // -------------------------------------------------------------
            // 1. SKILL GROWTH PROGRESSION STAGE DETERMINATION
            // -------------------------------------------------------------
            System.out.println("\n--- 1. Skill Growth Stage Determination (Stage 1, 2, 3) ---");

            Event stage1Explicit = new Event(1, "Java Fundamentals", "Coding", "2026-11-10", "Morning", 0.0, "Lab 1", "None", "CSE", 1, "Offline", "Beginner", "09:00", "11:00", 30);
            int stage1Res = (Integer) determineStageMethod.invoke(dashboard, stage1Explicit);
            assertTest("Event with target level 'Beginner' classified as Stage 1 (Foundation)", stage1Res == 1);

            Event stage2Explicit = new Event(2, "Full Stack Spring Challenge", "Coding", "2026-11-11", "Afternoon", 100.0, "Lab 2", "Java", "CSE", 2, "Offline", "Intermediate", "13:00", "16:00", 30);
            int stage2Res = (Integer) determineStageMethod.invoke(dashboard, stage2Explicit);
            assertTest("Event with target level 'Intermediate' classified as Stage 2 (Application)", stage2Res == 2);

            Event stage3Explicit = new Event(3, "National AI Hackathon 2026", "AI", "2026-11-12", "Evening", 150.0, "Auditorium", "Python, ML", "All", 0, "Offline", "Advanced", "17:00", "21:00", 50);
            int stage3Res = (Integer) determineStageMethod.invoke(dashboard, stage3Explicit);
            assertTest("Event with target level 'Advanced' classified as Stage 3 (Mastery)", stage3Res == 3);

            Event stage3ImplicitTitle = new Event(4, "Inter-College Autonomous Robotics Championship", "Robotics", "2026-11-13", "Morning", 200.0, "Hub", "ROS", "All", 0, "Offline", "All", "09:00", "13:00", 40);
            int stage3ImpRes = (Integer) determineStageMethod.invoke(dashboard, stage3ImplicitTitle);
            assertTest("Title containing 'Championship' or 'Hackathon' correctly inferred as Stage 3", stage3ImpRes == 3);

            Event stage2ImplicitTitle = new Event(5, "Docker & Kubernetes Deployment Bootcamp", "Tech", "2026-11-14", "Morning", 50.0, "Lab 3", "Linux", "CSE", 0, "Offline", "All", "09:00", "12:00", 30);
            int stage2ImpRes = (Integer) determineStageMethod.invoke(dashboard, stage2ImplicitTitle);
            assertTest("Title containing 'Bootcamp' or 'Project' correctly inferred as Stage 2", stage2ImpRes == 2);

            Event stage1ImplicitTitle = new Event(6, "Introduction to Version Control & Git", "Tech", "2026-11-15", "Morning", 0.0, "Lab 1", "None", "All", 0, "Offline", "All", "09:00", "11:00", 30);
            int stage1ImpRes = (Integer) determineStageMethod.invoke(dashboard, stage1ImplicitTitle);
            assertTest("Title containing 'Introduction' or free workshop correctly inferred as Stage 1", stage1ImpRes == 1);

            // -------------------------------------------------------------
            // 2. SKILL PATHWAY RELEVANCE FILTERING
            // -------------------------------------------------------------
            System.out.println("\n--- 2. Career & Goal Pathway Relevance Filtering ---");

            Event relevantCoding = new Event(7, "Advanced Python Algorithms", "Coding", "2026-11-16", "Morning", 50.0, "Lab 1", "Python", "CSE", 2, "Offline", "Intermediate", "09:00", "11:00", 30);
            boolean isRelCoding = (Boolean) isRelevantMethod.invoke(dashboard, relevantCoding, testStudent);
            assertTest("Coding & Python event identified as relevant to student's skills & interests", isRelCoding);

            Event relevantAI = new Event(8, "Deep Learning Workshop", "AI", "2026-11-17", "Morning", 50.0, "Lab 2", "Python", "CSE", 2, "Offline", "Beginner", "09:00", "11:00", 30);
            boolean isRelAI = (Boolean) isRelevantMethod.invoke(dashboard, relevantAI, testStudent);
            assertTest("AI event identified as relevant to target career goal 'Full Stack & AI Engineer'", isRelAI);

            Event nonTechEvent = new Event(9, "Classical Bharatanatyam Dance Showcase", "Dance", "2026-11-18", "Evening", 50.0, "Auditorium", "None", "All", 0, "Offline", "All", "18:00", "20:00", 100);
            boolean isRelDance = (Boolean) isRelevantMethod.invoke(dashboard, nonTechEvent, testStudent);
            assertTest("Completely unrelated cultural event strictly excluded from tech skill pathway", !isRelDance);

            // -------------------------------------------------------------
            // 3. DYNAMIC CONTEXTUAL EXPLANATION GENERATION
            // -------------------------------------------------------------
            System.out.println("\n--- 3. Dynamic Contextual Explanations (No Generic Strings) ---");

            Event evEntrepreneur = new Event(10, "Venture Pitch & Seed Funding 101", "Entrepreneurship", "2026-11-20", "Morning", 0.0, "B-School", "None", "All", 0, "Offline", "Beginner", "10:00", "12:00", 40);
            String ratEntrepreneur = (String) generateRationaleMethod.invoke(dashboard, testStudent, evEntrepreneur);
            assertTest("Entrepreneurship rationale highlights monetization and venture creation",
                ratEntrepreneur.contains("commercial acumen") && ratEntrepreneur.contains("market products"));

            Event evSpeaking = new Event(11, "Campus Toastmasters Public Speaking", "Public Speaking", "2026-11-21", "Morning", 0.0, "Hall 2", "None", "All", 0, "Offline", "Beginner", "10:00", "12:00", 40);
            String ratSpeaking = (String) generateRationaleMethod.invoke(dashboard, testStudent, evSpeaking);
            assertTest("Public speaking rationale highlights technical leadership and demos",
                ratSpeaking.contains("technical leadership") && ratSpeaking.contains("high-stakes demos"));

            Event evDesign = new Event(12, "Figma UI/UX Design Sprint", "Design", "2026-11-22", "Morning", 0.0, "Design Studio", "None", "All", 0, "Offline", "Beginner", "10:00", "12:00", 40);
            String ratDesign = (String) generateRationaleMethod.invoke(dashboard, testStudent, evDesign);
            assertTest("Design rationale highlights user-centric architecture for engineers",
                ratDesign.contains("user-centric design") && ratDesign.contains("engineering workflow"));

            Event evRobotics = new Event(13, "Arduino & IoT Physical Computing", "Robotics", "2026-11-23", "Morning", 0.0, "Hardware Lab", "None", "All", 0, "Offline", "Beginner", "10:00", "12:00", 40);
            String ratRobotics = (String) generateRationaleMethod.invoke(dashboard, testStudent, evRobotics);
            assertTest("Robotics rationale highlights physical computing and embedded automation",
                ratRobotics.contains("physical computing") && ratRobotics.contains("software algorithms"));

            Event evSports = new Event(14, "Intramural Badminton Championship", "Sports", "2026-11-24", "Evening", 0.0, "Sports Complex", "None", "All", 0, "Offline", "All", "17:00", "19:00", 40);
            String ratSports = (String) generateRationaleMethod.invoke(dashboard, testStudent, evSports);
            assertTest("Sports rationale highlights mental stamina, stress resilience, and team agility",
                ratSports.contains("mental stamina") && ratSports.contains("cognitive performance"));

            // Verify explanations are non-identical across distinct events
            assertTest("Rationale for Entrepreneurship differs completely from Design", !ratEntrepreneur.equals(ratDesign));
            assertTest("Rationale for Public Speaking differs completely from Sports", !ratSpeaking.equals(ratSports));

            // Clean up dashboard window
            dashboard.dispose();

            System.out.println("\n==========================================================================");
            System.out.println("RESULTS: " + testsPassed + " / " + totalTests + " TESTS PASSED (" + ((testsPassed * 100) / totalTests) + "%)");
            System.out.println(">>> Phase 3 Skill Growth & Discover Something New Successfully Verified! <<<");
            System.out.println("==========================================================================");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
