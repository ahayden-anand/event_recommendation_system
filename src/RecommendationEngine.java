import java.util.ArrayList;

/**
 * RecommendationEngine.java
 * Pure Java rule-based recommendation algorithm.
 * Evaluates student profiles against campus events using a 100-point scoring model:
 * 1. Interest Match       : 30 points
 * 2. Skill Match          : 25 points
 * 3. Budget Compatibility : 20 points
 * 4. Availability Match   : 15 points
 * 5. Academic Eligibility : 10 points
 * 
 * Ineligible or unaffordable events are strictly excluded.
 * Sorts suitable events descending by score using simple loops.
 */
public class RecommendationEngine {

    /**
     * Recommends events tailored to the student's profile.
     * @param student The student profile
     * @param events The full list of campus events
     * @return List of EventRecommendation sorted in descending order of suitability
     */
    public ArrayList<EventRecommendation> recommendEvents(Student student, ArrayList<Event> events) {
        ArrayList<EventRecommendation> recommendations = new ArrayList<EventRecommendation>();

        if (student == null || events == null) {
            return recommendations;
        }

        // Loop through each campus event
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);

            // --- 1. HARD FILTER: Budget Check ---
            // If the fee exceeds student's budget, the event cannot be afforded
            if (event.getFee() > student.getMaxBudget()) {
                continue; // Skip unaffordable event
            }

            // --- 2. HARD FILTER: Academic Eligibility Check ---
            // Check Department
            boolean deptMatches = false;
            String eligibleDept = event.getEligibleDept().trim().toLowerCase();
            String studentDept = student.getDepartment().trim().toLowerCase();

            if (eligibleDept.equals("all") || eligibleDept.contains(studentDept)) {
                deptMatches = true;
            }

            // Check Academic Year (0 means open for all years)
            boolean yearMatches = false;
            if (event.getEligibleYear() == 0 || event.getEligibleYear() == student.getAcademicYear()) {
                yearMatches = true;
            }

            // If not academically eligible, do not recommend
            if (!deptMatches || !yearMatches) {
                continue; // Skip ineligible event
            }

            // --- 3. SCORING SYSTEM (Max 100 points) ---
            int totalScore = 0;
            String reason = "";

            // A. Academic Eligibility Score (10 pts)
            totalScore += 10;
            reason += "Eligible for " + student.getDepartment() + " (Yr " + student.getAcademicYear() + ") [+10]";

            // B. Budget Compatibility Score (20 pts)
            totalScore += 20;
            if (event.getFee() == 0.0) {
                reason += ", Free Event [+20]";
            } else {
                reason += ", Within Budget of Rs. " + student.getMaxBudget() + " [+20]";
            }

            // C. Interest Match (30 pts)
            String studentInterests = student.getInterests().toLowerCase();
            String eventCategory = event.getCategory().toLowerCase();
            if (studentInterests.contains(eventCategory)) {
                totalScore += 30;
                reason += ", Interest matched: " + event.getCategory() + " [+30]";
            }

            // D. Skill Match (25 pts)
            String studentSkills = student.getSkills().toLowerCase();
            String[] requiredSkills = event.getRequiredSkills().split(",");
            boolean matchedAnySkill = false;
            String matchedSkillNames = "";

            for (int k = 0; k < requiredSkills.length; k++) {
                String req = requiredSkills[k].trim().toLowerCase();
                if (!req.isEmpty() && studentSkills.contains(req)) {
                    matchedAnySkill = true;
                    if (matchedSkillNames.isEmpty()) {
                        matchedSkillNames = requiredSkills[k].trim();
                    } else {
                        matchedSkillNames += ", " + requiredSkills[k].trim();
                    }
                }
            }

            if (matchedAnySkill) {
                totalScore += 25;
                reason += ", Skills matched: " + matchedSkillNames + " [+25]";
            }

            // E. Availability Match (15 pts)
            String studentSlot = student.getAvailableSlot().trim().toLowerCase();
            String eventSlot = event.getTimeSlot().trim().toLowerCase();
            if (studentSlot.equals("all") || studentSlot.equals("any") || studentSlot.equals(eventSlot)) {
                totalScore += 15;
                reason += ", Time slot matched: " + event.getTimeSlot() + " [+15]";
            }

            // Add to suitable recommendations list
            recommendations.add(new EventRecommendation(event, totalScore, reason));
        }

        // --- 4. SIMPLE SORTING: Bubble Sort descending by score ---
        // Basic student-level sorting taught in introductory programming
        for (int i = 0; i < recommendations.size() - 1; i++) {
            for (int j = 0; j < recommendations.size() - i - 1; j++) {
                if (recommendations.get(j).getScore() < recommendations.get(j + 1).getScore()) {
                    // Swap adjacent elements
                    EventRecommendation temp = recommendations.get(j);
                    recommendations.set(j, recommendations.get(j + 1));
                    recommendations.set(j + 1, temp);
                }
            }
        }

        return recommendations;
    }
}
