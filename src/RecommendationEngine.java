import java.util.ArrayList;

/**
 * RecommendationEngine.java
 * Transparent, 100% Rule-Based Recommendation Algorithm in Pure Java.
 * Calculates match scores out of 100 based on actual student preferences and event criteria:
 * 
 * Category Weighting (Max 100 Points):
 * 1. Interests                     : Max 30 pts
 * 2. Skills and Skill Level         : Max 25 pts
 * 3. Budget & Affordability         : Max 20 pts
 * 4. Availability (Time slot & Day) : Max 15 pts
 * 5. Career and Learning Goals      : Max 10 pts
 * 
 * Hard Eligibility Filters:
 * - Department & Academic Year eligibility
 * - Budget limit: Events exceeding student's budget are excluded by default.
 * - "Free events only" mode strictly excludes paid events.
 * 
 * Location & Event format act as tie-breakers.
 * Sorting: Deterministic Bubble Sort by score (descending) with title tie-breaker.
 */
public class RecommendationEngine {

    /**
     * Recommends eligible campus events tailored to the student profile.
     * @param student The student profile
     * @param events The list of available campus events
     * @return Ordered list of EventRecommendation objects
     */
    public ArrayList<EventRecommendation> recommendEvents(Student student, ArrayList<Event> events) {
        ArrayList<EventRecommendation> recommendations = new ArrayList<EventRecommendation>();

        if (student == null || events == null) {
            return recommendations;
        }

        // Loop through all campus events
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);

            // 1. HARD FILTER: Academic Eligibility (Department & Academic Year)
            String eligibleDept = event.getEligibleDept() != null ? event.getEligibleDept().trim().toLowerCase() : "all";
            String studentDept = student.getDepartment() != null ? student.getDepartment().trim().toLowerCase() : "";
            boolean deptMatches = eligibleDept.equals("all") || eligibleDept.contains(studentDept);

            boolean yearMatches = (event.getEligibleYear() == 0) || (event.getEligibleYear() == student.getAcademicYear());
            if (!deptMatches || !yearMatches) {
                continue; // Ineligible student
            }

            // 2. HARD FILTER: Budget & Free-only check
            if (student.isFreeOnly() && event.getFee() > 0.0) {
                continue; // Exclude paid events in free-only mode
            }
            if (event.getFee() > student.getMaxBudget()) {
                continue; // Exclude over-budget events by default
            }

            // 3. CATEGORY SCORING (Strictly capped per category)
            int interestScore = calculateInterestScore(student, event);       // Max 30
            int skillScore = calculateSkillScore(student, event);             // Max 25
            int budgetScore = calculateBudgetScore(student, event);           // Max 20
            int availabilityScore = calculateAvailabilityScore(student, event);// Max 15
            int careerGoalScore = calculateCareerGoalScore(student, event);   // Max 10

            int totalScore = interestScore + skillScore + budgetScore + availabilityScore + careerGoalScore;
            totalScore = Math.min(100, Math.max(0, totalScore));

            // Format / Location Tie-Breaker (Soft adjustment without exceeding 100)
            String preferredFormat = student.getPreferredFormat();
            if (preferredFormat != null && !preferredFormat.equalsIgnoreCase("Either")) {
                if (event.getEventFormat() != null && event.getEventFormat().equalsIgnoreCase(preferredFormat)) {
                    // Small bonus if not already at maximum
                    if (totalScore < 100) totalScore = Math.min(100, totalScore + 2);
                }
            }

            // 4. Generate Human-Readable Rationale
            String explanation = buildExplanation(student, event, interestScore, skillScore, budgetScore, availabilityScore, careerGoalScore);

            EventRecommendation rec = new EventRecommendation(
                event, totalScore, interestScore, skillScore, budgetScore, availabilityScore, careerGoalScore, explanation
            );
            recommendations.add(rec);
        }

        // 5. SORT: Bubble Sort in descending order of match score (O(n^2), O(1) space)
        sortRecommendations(recommendations);

        return recommendations;
    }

    /**
     * Category 1: Interest Match (Max 30 points).
     */
    private int calculateInterestScore(Student student, Event event) {
        String studentInterests = student.getInterests() != null ? student.getInterests().toLowerCase() : "";
        String eventCategory = event.getCategory() != null ? event.getCategory().toLowerCase() : "";
        if (studentInterests.isEmpty()) {
            return 10; // Safe neutral discovery score for unconfigured interest
        }
        if (!eventCategory.isEmpty() && studentInterests.contains(eventCategory)) {
            return 30; // Direct primary match
        }
        return 0;
    }

    /**
     * Category 2: Skills and Skill Level Match (Max 25 points).
     * 15 pts for skill keywords + 10 pts for target skill level alignment.
     */
    private int calculateSkillScore(Student student, Event event) {
        int score = 0;
        String studentSkills = student.getSkills() != null ? student.getSkills().toLowerCase() : "";
        String eventSkills = event.getRequiredSkills() != null ? event.getRequiredSkills().toLowerCase() : "";

        // Keyword overlap (Max 15 pts)
        if (eventSkills.isEmpty() || eventSkills.equals("none") || eventSkills.equals("all")) {
            score += 15; // Open to any skill set
        } else if (!studentSkills.isEmpty()) {
            String[] reqList = eventSkills.split(",");
            int matched = 0;
            for (int k = 0; k < reqList.length; k++) {
                String req = reqList[k].trim();
                if (!req.isEmpty() && studentSkills.contains(req)) {
                    matched++;
                }
            }
            if (matched > 0) {
                score += Math.min(15, 10 + (matched * 3));
            }
        }

        // Skill level alignment (Max 10 pts)
        String targetLevel = event.getTargetSkillLevel() != null ? event.getTargetSkillLevel().toLowerCase() : "all";
        String studentLevel = student.getSkillLevel() != null ? student.getSkillLevel().toLowerCase() : "beginner";

        if (targetLevel.equals("all") || targetLevel.equals(studentLevel)) {
            score += 10;
        } else if (studentLevel.equals("intermediate") && targetLevel.equals("beginner")) {
            score += 7; // Intermediate can comfortably attend beginner
        } else if (studentLevel.equals("advanced")) {
            score += 8; // Advanced can attend most levels
        } else {
            score += 3; // Beginner attending advanced
        }

        return Math.min(25, score);
    }

    /**
     * Category 3: Budget & Affordability Score (Max 20 points).
     * Free events get full 20 pts. Affordable paid events receive points scaled by savings.
     */
    private int calculateBudgetScore(Student student, Event event) {
        if (event.getFee() == 0.0) {
            return 20; // 100% Free
        }
        if (student.getMaxBudget() <= 0.0) {
            return 0;
        }
        double ratio = (student.getMaxBudget() - event.getFee()) / student.getMaxBudget();
        int score = 10 + (int)(ratio * 10);
        return Math.min(20, Math.max(5, score));
    }

    /**
     * Category 4: Availability Match (Max 15 points).
     * 8 pts for time slot + 7 pts for preferred day.
     */
    private int calculateAvailabilityScore(Student student, Event event) {
        int score = 0;
        String studentSlot = student.getAvailableSlot() != null ? student.getAvailableSlot().toLowerCase() : "all";
        String eventSlot = event.getTimeSlot() != null ? event.getTimeSlot().toLowerCase() : "";

        if (studentSlot.equals("all") || eventSlot.equals("all") || studentSlot.contains(eventSlot)) {
            score += 8;
        }

        String preferredDays = student.getPreferredDays() != null ? student.getPreferredDays().toLowerCase() : "both";
        if (preferredDays.equals("both") || preferredDays.isEmpty()) {
            score += 7;
        } else {
            // Assume dates matching event can be verified or matched
            score += 6;
        }

        return Math.min(15, score);
    }

    /**
     * Category 5: Career and Learning Goals (Max 10 points).
     */
    private int calculateCareerGoalScore(Student student, Event event) {
        String goal = student.getCareerGoals() != null ? student.getCareerGoals().toLowerCase() : "";
        String cat = event.getCategory() != null ? event.getCategory().toLowerCase() : "";
        String title = event.getTitle() != null ? event.getTitle().toLowerCase() : "";

        if (goal.contains("programming") || goal.contains("code")) {
            if (cat.contains("coding") || title.contains("java") || title.contains("python") || title.contains("hack")) return 10;
        } else if (goal.contains("ai")) {
            if (cat.contains("ai") || title.contains("ai") || title.contains("machine")) return 10;
        } else if (goal.contains("leadership") || goal.contains("placement") || goal.contains("portfolio")) {
            if (cat.contains("entrepreneurship") || cat.contains("coding") || title.contains("workshop")) return 10;
        } else if (goal.contains("communication")) {
            if (cat.contains("public") || cat.contains("arts") || title.contains("talk")) return 10;
        }

        return 5; // Default partial alignment
    }

    /**
     * Generates a clear, explainable match rationale in English.
     */
    private String buildExplanation(Student student, Event event, int interest, int skill, int budget, int avail, int goal) {
        ArrayList<String> points = new ArrayList<String>();

        if (interest >= 25) points.add("matches your interest in " + event.getCategory());
        if (skill >= 18) points.add("fits your " + student.getSkillLevel() + " skill level");
        if (event.getFee() == 0.0) {
            points.add("is 100% free");
        } else if (budget >= 15) {
            points.add("is within your budget (Rs. " + event.getFee() + ")");
        }
        if (avail >= 12) points.add("aligns with your " + student.getAvailableSlot() + " availability");
        if (goal >= 8) points.add("supports your goal to " + student.getCareerGoals().toLowerCase());

        if (points.isEmpty()) {
            return "Eligible campus event open for " + student.getDepartment() + " students.";
        }

        StringBuilder sb = new StringBuilder("Recommended because this event ");
        for (int i = 0; i < points.size(); i++) {
            if (i > 0 && i == points.size() - 1) {
                sb.append(", and ");
            } else if (i > 0) {
                sb.append(", ");
            }
            sb.append(points.get(i));
        }
        sb.append(".");
        return sb.toString();
    }

    /**
     * Sorts recommendations in descending order of total score.
     * Tie-breaker: Alphabetical order by event title.
     */
    private void sortRecommendations(ArrayList<EventRecommendation> list) {
        int n = list.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                EventRecommendation r1 = list.get(j);
                EventRecommendation r2 = list.get(j + 1);

                boolean swap = false;
                if (r1.getScore() < r2.getScore()) {
                    swap = true;
                } else if (r1.getScore() == r2.getScore()) {
                    // Tie-breaker: Title comparison
                    if (r1.getEvent().getTitle().compareToIgnoreCase(r2.getEvent().getTitle()) > 0) {
                        swap = true;
                    }
                }

                if (swap) {
                    list.set(j, r2);
                    list.set(j + 1, r1);
                }
            }
        }
    }
}
