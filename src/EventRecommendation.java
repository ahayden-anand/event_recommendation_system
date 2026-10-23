/**
 * EventRecommendation.java
 * Holds an Event paired with its calculated recommendation score (0-100),
 * detailed category-wise scoring breakdown, and a clear explanation.
 * 
 * Scoring Model:
 * 1. Interests Match       : 30 pts max
 * 2. Skills & Skill Level  : 25 pts max
 * 3. Budget Affordability  : 20 pts max
 * 4. Availability (Day/Slot: 15 pts max
 * 5. Career & Learning Goal: 10 pts max
 * Total Score              : 100 pts max
 */
public class EventRecommendation {
    private Event event;
    private int score;             // 0 to 100
    private int interestScore;     // Max 30
    private int skillScore;        // Max 25
    private int budgetScore;       // Max 20
    private int availabilityScore; // Max 15
    private int careerGoalScore;   // Max 10
    private String matchReason;    // Human-readable rationale
    private String breakdownText;  // Category-wise summary string

    // Constructor with full score breakdown
    public EventRecommendation(Event event, int score, int interestScore, int skillScore,
                               int budgetScore, int availabilityScore, int careerGoalScore,
                               String matchReason) {
        this.event = event;
        this.score = Math.min(100, Math.max(0, score));
        this.interestScore = interestScore;
        this.skillScore = skillScore;
        this.budgetScore = budgetScore;
        this.availabilityScore = availabilityScore;
        this.careerGoalScore = careerGoalScore;
        this.matchReason = matchReason;
        this.breakdownText = "Interests: " + interestScore + "/30 | Skills: " + skillScore + "/25 | Budget: " 
                           + budgetScore + "/20 | Availability: " + availabilityScore + "/15 | Goals: " + careerGoalScore + "/10";
    }

    // Backward-compatible constructor
    public EventRecommendation(Event event, int score, String matchReason) {
        this(event, score, 0, 0, 0, 0, 0, matchReason);
    }

    // Getters and Setters
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getInterestScore() { return interestScore; }
    public int getSkillScore() { return skillScore; }
    public int getBudgetScore() { return budgetScore; }
    public int getAvailabilityScore() { return availabilityScore; }
    public int getCareerGoalScore() { return careerGoalScore; }

    public String getMatchReason() { return matchReason; }
    public void setMatchReason(String matchReason) { this.matchReason = matchReason; }

    public String getBreakdownText() { return breakdownText; }

    // Formatted viva display
    public void displayRecommendation(int rank) {
        System.out.println("Rank #" + rank + " | Score: " + score + "/100");
        System.out.println("Event       : " + event.getTitle() + " (" + event.getCategory() + ")");
        System.out.println("Breakdown   : " + breakdownText);
        System.out.println("Venue & Fee : " + event.getVenue() + " | Rs. " + event.getFee());
        System.out.println("Explanation : " + matchReason);
        System.out.println("--------------------------------------------------------------------------");
    }
}
