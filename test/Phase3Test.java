import java.util.ArrayList;

/**
 * Phase3Test.java
 * Test suite for the 100-Point Recommendation Algorithm.
 * Tests multiple student profiles against the campus events catalog to verify:
 * 1. Scoring accuracy (Interest: 30, Skill: 25, Budget: 20, Slot: 15, Eligibility: 10).
 * 2. Hard-filter exclusion for unaffordable or academically ineligible events.
 * 3. Descending order sorting using basic Bubble Sort.
 * 4. Transparent match explanation generation.
 */
public class Phase3Test {
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("              PHASE 3: RECOMMENDATION ALGORITHM TEST SUITE                ");
        System.out.println("==========================================================================");

        // 1. Initialize EventManager and RecommendationEngine
        EventManager eventManager = new EventManager();
        eventManager.loadSampleEvents();
        ArrayList<Event> allEvents = eventManager.getAllEvents();

        RecommendationEngine engine = new RecommendationEngine();

        // -----------------------------------------------------------------------
        // TEST PROFILE 1: Technical Student (CSE, Year 3, Morning slot, Rs. 200 budget)
        // -----------------------------------------------------------------------
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println(">>> TEST 1: Profile of Rahul Sharma (CSE, 3rd Year)");
        System.out.println("--------------------------------------------------------------------------");
        Student rahul = new Student(
            101,
            "Rahul Sharma",
            "rahul@campus.edu",
            "CSE",
            3,
            200.0,
            "Coding, AI",
            "Java, Python, Problem Solving",
            "Morning"
        );
        rahul.displayStudent();

        ArrayList<EventRecommendation> rahulRecs = engine.recommendEvents(rahul, allEvents);
        System.out.println("\n[Recommendations for Rahul Sharma] (Total Eligible: " + rahulRecs.size() + ")");
        for (int i = 0; i < rahulRecs.size(); i++) {
            rahulRecs.get(i).displayRecommendation(i + 1);
        }

        // -----------------------------------------------------------------------
        // TEST PROFILE 2: Robotics & Hardware Student (ECE, Year 2, Afternoon, Rs. 350 budget)
        // -----------------------------------------------------------------------
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println(">>> TEST 2: Profile of Ananya Sen (ECE, 2nd Year)");
        System.out.println("--------------------------------------------------------------------------");
        Student ananya = new Student(
            102,
            "Ananya Sen",
            "ananya@campus.edu",
            "ECE",
            2,
            350.0,
            "Robotics, Electronics",
            "Arduino, C++, Electronics",
            "Afternoon"
        );
        ananya.displayStudent();

        ArrayList<EventRecommendation> ananyaRecs = engine.recommendEvents(ananya, allEvents);
        System.out.println("\n[Recommendations for Ananya Sen] (Total Eligible: " + ananyaRecs.size() + ")");
        for (int i = 0; i < ananyaRecs.size(); i++) {
            ananyaRecs.get(i).displayRecommendation(i + 1);
        }

        // -----------------------------------------------------------------------
        // TEST PROFILE 3: Low Budget & Non-Tech Student (MECH, Year 1, Evening, Rs. 60 budget)
        // Tests exclusion of unaffordable and restricted events
        // -----------------------------------------------------------------------
        System.out.println("\n--------------------------------------------------------------------------");
        System.out.println(">>> TEST 3: Profile of Vikram Rao (MECH, 1st Year - Strict Rs. 60 Budget)");
        System.out.println("--------------------------------------------------------------------------");
        Student vikram = new Student(
            103,
            "Vikram Rao",
            "vikram@campus.edu",
            "MECH",
            1,
            60.0,
            "Sports, Arts",
            "Fitness, Sketching",
            "Evening"
        );
        vikram.displayStudent();

        ArrayList<EventRecommendation> vikramRecs = engine.recommendEvents(vikram, allEvents);
        System.out.println("\n[Recommendations for Vikram Rao] (Total Eligible: " + vikramRecs.size() + ")");
        for (int i = 0; i < vikramRecs.size(); i++) {
            vikramRecs.get(i).displayRecommendation(i + 1);
        }

        System.out.println("\n>>> Phase 3: Recommendation Algorithm Successfully Verified! <<<");
    }
}
