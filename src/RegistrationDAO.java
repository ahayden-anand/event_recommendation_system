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
     * Retrieves all registrations ordered by reg_id.
     */
    public ArrayList<Registration> getAllRegistrations() {
        ArrayList<Registration> list = new ArrayList<Registration>();
        String sql = "SELECT reg_id, student_id, student_name, student_email, event_id, event_title, registered_at "
                   + "FROM registrations ORDER BY reg_id ASC";
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
                    rs.getString("registered_at")
                );
                list.add(reg);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching registrations: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves registrations filtered by student email.
     */
    public ArrayList<Registration> getRegistrationsByStudent(String email) {
        ArrayList<Registration> list = new ArrayList<Registration>();
        String sql = "SELECT reg_id, student_id, student_name, student_email, event_id, event_title, registered_at "
                   + "FROM registrations WHERE student_email=? ORDER BY reg_id ASC";
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
                        rs.getString("registered_at")
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
     */
    public boolean cancelRegistration(int regId) {
        String sql = "DELETE FROM registrations WHERE reg_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, regId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error cancelling registration: " + e.getMessage());
        }
        return false;
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
}
