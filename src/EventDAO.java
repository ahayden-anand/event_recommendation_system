import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * EventDAO.java
 * Data Access Object (DAO) for Event CRUD operations using JDBC.
 * Supports course-taught PreparedStatement queries, safe table migrations,
 * and retrieval for Audience Insights, Budget Finder, and Learning Pathways.
 */
public class EventDAO {

    /**
     * Creates the events table if it doesn't already exist.
     * Safely executes ALTER TABLE to support format, target skill level, time bounds, and capacity.
     */
    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS events ("
                   + "event_id INT AUTO_INCREMENT PRIMARY KEY, "
                   + "title VARCHAR(120) NOT NULL, "
                   + "category VARCHAR(50) NOT NULL, "
                   + "event_date VARCHAR(20) NOT NULL, "
                   + "time_slot VARCHAR(20) NOT NULL, "
                   + "fee DOUBLE NOT NULL DEFAULT 0.0, "
                   + "venue VARCHAR(120) NOT NULL, "
                   + "required_skills VARCHAR(255), "
                   + "eligible_dept VARCHAR(100) DEFAULT 'All', "
                   + "eligible_year INT DEFAULT 0, "
                   + "event_format VARCHAR(20) DEFAULT 'In-person', "
                   + "target_skill_level VARCHAR(20) DEFAULT 'All', "
                   + "start_time VARCHAR(20) DEFAULT '10:00', "
                   + "end_time VARCHAR(20) DEFAULT '12:00', "
                   + "max_capacity INT DEFAULT 50"
                   + ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);

            // Safe backward-compatible migrations
            try { stmt.execute("ALTER TABLE events ADD COLUMN event_format VARCHAR(20) DEFAULT 'In-person'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE events ADD COLUMN target_skill_level VARCHAR(20) DEFAULT 'All'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE events ADD COLUMN start_time VARCHAR(20) DEFAULT '10:00'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE events ADD COLUMN end_time VARCHAR(20) DEFAULT '12:00'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE events ADD COLUMN max_capacity INT DEFAULT 50"); } catch (SQLException ignored) {}

        } catch (SQLException e) {
            System.err.println("Error creating events table: " + e.getMessage());
        }
    }

    /**
     * Inserts a new event into the database.
     */
    public boolean addEvent(Event event) {
        String sql = "INSERT INTO events (title, category, event_date, time_slot, fee, venue, required_skills, "
                   + "eligible_dept, eligible_year, event_format, target_skill_level, start_time, end_time, max_capacity) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getCategory());
            ps.setString(3, event.getEventDate());
            ps.setString(4, event.getTimeSlot());
            ps.setDouble(5, event.getFee());
            ps.setString(6, event.getVenue());
            ps.setString(7, event.getRequiredSkills());
            ps.setString(8, event.getEligibleDept());
            ps.setInt(9, event.getEligibleYear());
            ps.setString(10, event.getEventFormat());
            ps.setString(11, event.getTargetSkillLevel());
            ps.setString(12, event.getStartTime());
            ps.setString(13, event.getEndTime());
            ps.setInt(14, event.getMaxCapacity());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        event.setEventId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding event: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves all events from the database.
     */
    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> list = new ArrayList<Event>();
        String sql = "SELECT * FROM events ORDER BY event_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRowToEvent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving events: " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates an existing event's details.
     */
    public boolean updateEvent(Event event) {
        String sql = "UPDATE events SET title=?, category=?, event_date=?, time_slot=?, "
                   + "fee=?, venue=?, required_skills=?, eligible_dept=?, eligible_year=?, "
                   + "event_format=?, target_skill_level=?, start_time=?, end_time=?, max_capacity=? "
                   + "WHERE event_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getCategory());
            ps.setString(3, event.getEventDate());
            ps.setString(4, event.getTimeSlot());
            ps.setDouble(5, event.getFee());
            ps.setString(6, event.getVenue());
            ps.setString(7, event.getRequiredSkills());
            ps.setString(8, event.getEligibleDept());
            ps.setInt(9, event.getEligibleYear());
            ps.setString(10, event.getEventFormat());
            ps.setString(11, event.getTargetSkillLevel());
            ps.setString(12, event.getStartTime());
            ps.setString(13, event.getEndTime());
            ps.setInt(14, event.getMaxCapacity());
            ps.setInt(15, event.getEventId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating event: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes an event by its ID.
     */
    public boolean deleteEvent(int eventId) {
        String sql = "DELETE FROM events WHERE event_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting event: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Finds a single event by ID.
     */
    public Event getEventById(int eventId) {
        String sql = "SELECT * FROM events WHERE event_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEvent(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding event by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Maps a ResultSet row to an Event model object with safe fallbacks.
     */
    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        String format = "In-person";
        String targetLevel = "All";
        String start = "10:00";
        String end = "12:00";
        int capacity = 50;

        try { format = rs.getString("event_format"); } catch (SQLException ignored) {}
        try { targetLevel = rs.getString("target_skill_level"); } catch (SQLException ignored) {}
        try { start = rs.getString("start_time"); } catch (SQLException ignored) {}
        try { end = rs.getString("end_time"); } catch (SQLException ignored) {}
        try { capacity = rs.getInt("max_capacity"); } catch (SQLException ignored) {}

        return new Event(
            rs.getInt("event_id"),
            rs.getString("title"),
            rs.getString("category"),
            rs.getString("event_date"),
            rs.getString("time_slot"),
            rs.getDouble("fee"),
            rs.getString("venue"),
            rs.getString("required_skills"),
            rs.getString("eligible_dept"),
            rs.getInt("eligible_year"),
            format != null ? format : "In-person",
            targetLevel != null ? targetLevel : "All",
            start != null ? start : "10:00",
            end != null ? end : "12:00",
            capacity > 0 ? capacity : 50
        );
    }
}
