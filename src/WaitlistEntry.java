/**
 * WaitlistEntry.java
 * Represents an entry in the Smart Waitlist queue for full-capacity campus events.
 */
public class WaitlistEntry {
    private int waitlistId;
    private int studentId;
    private String studentName;
    private String studentEmail;
    private int eventId;
    private String eventTitle;
    private String addedAt;
    private String status; // 'WAITING', 'PROMOTED', 'CANCELLED'

    public WaitlistEntry(int waitlistId, int studentId, String studentName, String studentEmail,
                         int eventId, String eventTitle, String addedAt, String status) {
        this.waitlistId = waitlistId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentEmail = studentEmail;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.addedAt = addedAt;
        this.status = status;
    }

    public int getWaitlistId() { return waitlistId; }
    public void setWaitlistId(int waitlistId) { this.waitlistId = waitlistId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentEmail() { return studentEmail; }
    public void setStudentEmail(String studentEmail) { this.studentEmail = studentEmail; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

    public String getAddedAt() { return addedAt; }
    public void setAddedAt(String addedAt) { this.addedAt = addedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
