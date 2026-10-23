/**
 * EventRecommendation.java
 * Holds an Event paired with its calculated recommendation score (0-100)
 * and a clear explanation of which aspects matched the student profile.
 */
public class EventRecommendation {
    private Event event;
    private int score;             // 0 to 100
    private String matchReason;    // Detailed explanation of matching aspects

    // Constructor
    public EventRecommendation(Event event, int score, String matchReason) {
        this.event = event;
        this.score = score;
        this.matchReason = matchReason;
    }

    // Getters and Setters
    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getMatchReason() {
        return matchReason;
    }

    public void setMatchReason(String matchReason) {
        this.matchReason = matchReason;
    }

    // Display formatted recommendation
    public void displayRecommendation(int rank) {
        System.out.println("Rank #" + rank + " | Score: " + score + "/100");
        System.out.println("Event       : " + event.getTitle() + " (" + event.getCategory() + ")");
        System.out.println("Date & Slot : " + event.getEventDate() + " | Slot: " + event.getTimeSlot());
        System.out.println("Venue & Fee : " + event.getVenue() + " | Rs. " + event.getFee());
        System.out.println("Match Reason: " + matchReason);
        System.out.println("--------------------------------------------------------------------------");
    }
}
