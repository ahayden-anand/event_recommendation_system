import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * RegistrationDAO.java
 * Data Access Object for Registration transactions in MySQL using JDBC.
 * Features:
 * - Schedule Clash Detection: Checks if an event date and time interval overlaps with an existing registration.
 * - Smart Waitlist Support: Stores waitlisted entries when event reaches capacity.
 * - Duplicate Registration Prevention.
 */
public class RegistrationDAO {

    /**
     * Creates the registrations and waitlists tables if they do not exist.
     */
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS registrations ("
                   + "reg_id INT AUTO_INCREMENT PRIMARY KEY, "
                   + "student_id INT DEFAULT 0, "
                   + "student_name VARCHAR(100) NOT NULL, "
                   + "student_email VARCHAR(100) NOT NULL, "
                   + "event_id INT NOT NULL, "
                   + "event_title VARCHAR(120) NOT NULL, "
                   + "registered_at VARCHAR(50) NOT NULL, "
                   + "FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE"
                   + ")";
        String sqlWaitlist = "CREATE TABLE IF NOT EXISTS waitlists ("
                           + "waitlist_id INT AUTO_INCREMENT PRIMARY KEY, "
                           + "student_id INT DEFAULT 0, "
                           + "student_name VARCHAR(100) NOT NULL, "
                           + "student_email VARCHAR(100) NOT NULL, "
                           + "event_id INT NOT NULL, "
                           + "event_title VARCHAR(120) NOT NULL, "
                           + "added_at VARCHAR(50) NOT NULL, "
                           + "status VARCHAR(20) DEFAULT 'WAITING', "
                           + "FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE"
                           + ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.execute(sqlWaitlist);
        } catch (SQLException e) {
            System.err.println("Error creating registration/waitlist tables: " + e.getMessage());
        }
    }

    /**
     * Checks whether a student is already registered for an event.
     */
    public boolean isAlreadyRegistered(String studentEmail, int eventId) {
        String sql = "SELECT reg_id FROM registrations WHERE student_email=? AND event_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentEmail);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking registration existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Schedule Clash Detector:
     * Checks whether the target event overlaps in date and time with any event the student is currently registered for.
     * @return Conflicting event title and time if clash exists, or null if schedule is clear.
     */
    public String checkScheduleClash(String studentEmail, Event targetEvent, EventDAO eventDAO) {
        if (targetEvent == null || studentEmail == null) return null;

        ArrayList<Registration> existingRegs = getRegistrationsByStudent(studentEmail);
        for (int i = 0; i < existingRegs.size(); i++) {
            Registration reg = existingRegs.get(i);
            Event regEvent = eventDAO.getEventById(reg.getEventId());
            if (regEvent == null) continue;

            // 1. Check Date match
            if (regEvent.getEventDate() != null && regEvent.getEventDate().equalsIgnoreCase(targetEvent.getEventDate())) {
                // 2. Check Time Slot overlap (e.g. "Morning", "Afternoon", or time range)
                boolean slotOverlap = false;
                String slot1 = regEvent.getTimeSlot() != null ? regEvent.getTimeSlot().toLowerCase() : "";
                String slot2 = targetEvent.getTimeSlot() != null ? targetEvent.getTimeSlot().toLowerCase() : "";

                if (slot1.equals(slot2) || slot1.equals("all") || slot2.equals("all")) {
                    slotOverlap = true;
                }

                // 3. Check exact time bounds overlap if available (e.g. 10:00 to 12:00 vs 11:00 to 13:00)
                int start1 = parseTimeToMinutes(regEvent.getStartTime());
                int end1 = parseTimeToMinutes(regEvent.getEndTime());
                int start2 = parseTimeToMinutes(targetEvent.getStartTime());
                int end2 = parseTimeToMinutes(targetEvent.getEndTime());

                if (start1 >= 0 && end1 >= 0 && start2 >= 0 && end2 >= 0) {
                    // Interval overlap condition: start1 < end2 && start2 < end1
                    if (start1 < end2 && start2 < end1) {
                        slotOverlap = true;
                    } else {
                        slotOverlap = false; // Non-overlapping hours on same slot
                    }
                }

                if (slotOverlap) {
                    return regEvent.getTitle() + " on " + regEvent.getEventDate() + " (" + regEvent.getTimeSlot() + " " + regEvent.getStartTime() + "-" + regEvent.getEndTime() + ")";
                }
            }
        }
        return null; // No clash detected
    }

    /**
     * Helper to parse HH:mm 24-hr time strings into integer minutes from midnight.
     */
    private int parseTimeToMinutes(String timeStr) {
        if (timeStr == null || !timeStr.contains(":")) return -1;
        try {
            String[] parts = timeStr.trim().split(":");
            int hours = Integer.parseInt(parts[0].trim());
            int mins = Integer.parseInt(parts[1].trim());
            return (hours * 60) + mins;
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Inserts a new registration into MySQL.
     */
    public boolean addRegistration(Registration reg) {
        String sql = "INSERT INTO registrations (student_id, student_name, student_email, event_id, event_title, registered_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reg.getStudentId());
            ps.setString(2, reg.getStudentName());
            ps.setString(3, reg.getStudentEmail());
            ps.setInt(4, reg.getEventId());
            ps.setString(5, reg.getEventTitle());
            ps.setString(6, reg.getRegistrationDate());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        reg.setRegId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding registration: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves all registrations ordered by reg_id with event schedule details.
     */
    public ArrayList<Registration> getAllRegistrations() {
        ArrayList<Registration> list = new ArrayList<Registration>();
        String sql = "SELECT r.reg_id, r.student_id, r.student_name, r.student_email, r.event_id, r.event_title, "
                   + "r.registered_at, e.event_date, e.time_slot, e.venue "
                   + "FROM registrations r "
                   + "LEFT JOIN events e ON r.event_id = e.event_id "
                   + "ORDER BY r.reg_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Registration reg = new Registration(
                    rs.getInt("reg_id"),
                    rs.getInt("student_id"),
                    rs.getString("student_name"),
                    rs.getString("student_email"),
                    rs.getInt("event_id"),
                    rs.getString("event_title"),
                    rs.getString("registered_at"),
                    rs.getString("event_date") != null ? rs.getString("event_date") : "TBA",
                    rs.getString("time_slot") != null ? rs.getString("time_slot") : "TBA",
                    rs.getString("venue") != null ? rs.getString("venue") : "Campus",
                    "CONFIRMED"
                );
                list.add(reg);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching registrations: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves registrations filtered by student email (case-insensitive) with full schedule details.
     */
    public ArrayList<Registration> getRegistrationsByStudent(String email) {
        ArrayList<Registration> list = new ArrayList<Registration>();
        if (email == null) return list;

        String sql = "SELECT r.reg_id, r.student_id, r.student_name, r.student_email, r.event_id, r.event_title, "
                   + "r.registered_at, e.event_date, e.time_slot, e.venue "
                   + "FROM registrations r "
                   + "LEFT JOIN events e ON r.event_id = e.event_id "
                   + "WHERE LOWER(TRIM(r.student_email)) = LOWER(TRIM(?)) "
                   + "ORDER BY r.reg_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Registration(
                        rs.getInt("reg_id"),
                        rs.getInt("student_id"),
                        rs.getString("student_name"),
                        rs.getString("student_email"),
                        rs.getInt("event_id"),
                        rs.getString("event_title"),
                        rs.getString("registered_at"),
                        rs.getString("event_date") != null ? rs.getString("event_date") : "TBA",
                        rs.getString("time_slot") != null ? rs.getString("time_slot") : "TBA",
                        rs.getString("venue") != null ? rs.getString("venue") : "Campus",
                        "CONFIRMED"
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student registrations: " + e.getMessage());
        }
        return list;
    }

    /**
     * Calculates the number of students registered for a specific event.
     */
    public int getRegistrationCountForEvent(int eventId) {
        String sql = "SELECT COUNT(*) FROM registrations WHERE event_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting registrations: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Cancels a student registration.
     * Uses atomic JDBC transaction: removes registration and automatically promotes
     * the earliest waitlisted student in FIFO order into confirmed registration.
     * @param regId The registration ID to cancel
     * @return true if successfully cancelled (and waitlist promoted if applicable)
     */
    public boolean cancelRegistration(int regId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin atomic transaction

            // 1. Retrieve the event_id and event_title of the registration being cancelled
            int eventId = -1;
            String eventTitle = "";
            String findSql = "SELECT event_id, event_title FROM registrations WHERE reg_id=?";
            try (PreparedStatement findPs = conn.prepareStatement(findSql)) {
                findPs.setInt(1, regId);
                try (ResultSet rs = findPs.executeQuery()) {
                    if (rs.next()) {
                        eventId = rs.getInt("event_id");
                        eventTitle = rs.getString("event_title");
                    }
                }
            }

            if (eventId == -1) {
                conn.rollback();
                return false;
            }

            // 2. Delete the registration record
            String delSql = "DELETE FROM registrations WHERE reg_id=?";
            try (PreparedStatement delPs = conn.prepareStatement(delSql)) {
                delPs.setInt(1, regId);
                int deleted = delPs.executeUpdate();
                if (deleted == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // 3. Smart Waitlist Promotion: Check if any student is waiting for this event (FIFO)
            String wlSql = "SELECT waitlist_id, student_id, student_name, student_email "
                         + "FROM waitlists WHERE event_id=? AND status='WAITING' "
                         + "ORDER BY waitlist_id ASC LIMIT 1";
            try (PreparedStatement wlPs = conn.prepareStatement(wlSql)) {
                wlPs.setInt(1, eventId);
                try (ResultSet rs = wlPs.executeQuery()) {
                    if (rs.next()) {
                        int waitlistId = rs.getInt("waitlist_id");
                        int studentId = rs.getInt("student_id");
                        String studentName = rs.getString("student_name");
                        String studentEmail = rs.getString("student_email");

                        // Promote to confirmed registration
                        String promoteSql = "INSERT INTO registrations (student_id, student_name, student_email, event_id, event_title, registered_at) "
                                          + "VALUES (?, ?, ?, ?, ?, ?)";
                        try (PreparedStatement prPs = conn.prepareStatement(promoteSql)) {
                            prPs.setInt(1, studentId);
                            prPs.setString(2, studentName);
                            prPs.setString(3, studentEmail);
                            prPs.setInt(4, eventId);
                            prPs.setString(5, eventTitle);
                            prPs.setString(6, java.time.LocalDate.now().toString());
                            prPs.executeUpdate();
                        }

                        // Mark waitlist entry status as PROMOTED
                        String updateWlSql = "UPDATE waitlists SET status='PROMOTED' WHERE waitlist_id=?";
                        try (PreparedStatement upPs = conn.prepareStatement(updateWlSql)) {
                            upPs.setInt(1, waitlistId);
                            upPs.executeUpdate();
                        }
                    }
                }
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            System.err.println("Error cancelling registration with waitlist promotion: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Smart Waitlist: Adds student to waitlist queue when capacity is filled.
     */
    public boolean addToWaitlist(int studentId, String name, String email, int eventId, String title) {
        String sql = "INSERT INTO waitlists (student_id, student_name, student_email, event_id, event_title, added_at, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, 'WAITING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setString(2, name);
            ps.setString(3, email);
            ps.setInt(4, eventId);
            ps.setString(5, title);
            ps.setString(6, java.time.LocalDateTime.now().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding to waitlist: " + e.getMessage());
        }
        return false;
    }

    /**
     * Returns the number of students waiting on the waitlist for an event.
     */
    public int getWaitlistCountForEvent(int eventId) {
        String sql = "SELECT COUNT(*) FROM waitlists WHERE event_id=? AND status='WAITING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting waitlist: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Retrieves all waitlist entries across all events for coordinator reporting.
     * @return List of WaitlistEntry objects in FIFO order per event.
     */
    public ArrayList<WaitlistEntry> getAllWaitlistEntries() {
        ArrayList<WaitlistEntry> list = new ArrayList<WaitlistEntry>();
        String sql = "SELECT waitlist_id, student_id, student_name, student_email, event_id, event_title, added_at, status "
                   + "FROM waitlists ORDER BY event_id ASC, waitlist_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new WaitlistEntry(
                    rs.getInt("waitlist_id"),
                    rs.getInt("student_id"),
                    rs.getString("student_name"),
                    rs.getString("student_email"),
                    rs.getInt("event_id"),
                    rs.getString("event_title"),
                    rs.getString("added_at"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all waitlist entries: " + e.getMessage());
        }
        return list;
    }
}
