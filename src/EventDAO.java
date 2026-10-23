import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * EventDAO.java
 * Data Access Object (DAO) for Event CRUD operations using JDBC.
 * Follows the patterns taught in Slide 9, 13, 14, and 15 of the Database notes:
 * - Uses PreparedStatement for parameterized queries to prevent SQL injection.
 * - Maps SQL rows to Event model objects.
 */
public class EventDAO {

    /**
     * Creates the events table if it doesn't already exist.
     * Matches the createTable() method from Slide 13.
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
                   + "eligible_year INT DEFAULT 0"
                   + ")";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating events table: " + e.getMessage());
        }
    }

    /**
     * Inserts a new event into the database.
     * Matches the addStudent() pattern from Slide 13.
     */
    public boolean addEvent(Event event) {
        String sql = "INSERT INTO events (title, category, event_date, time_slot, fee, venue, required_skills, eligible_dept, eligible_year) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        event.setEventId(generatedKeys.getInt(1));
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
     * Retrieves all events from the database ordered by event_id.
     * Matches the getAllStudents() pattern from Slide 14.
     */
    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> list = new ArrayList<Event>();
        String sql = "SELECT event_id, title, category, event_date, time_slot, fee, venue, required_skills, eligible_dept, eligible_year "
                   + "FROM events ORDER BY event_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Event ev = new Event(
                    rs.getInt("event_id"),
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getString("event_date"),
                    rs.getString("time_slot"),
                    rs.getDouble("fee"),
                    rs.getString("venue"),
                    rs.getString("required_skills"),
                    rs.getString("eligible_dept"),
                    rs.getInt("eligible_year")
                );
                list.add(ev);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching events: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Updates an existing event's details.
     * Matches the updateStudent() pattern from Slide 15.
     */
    public boolean updateEvent(Event event) {
        String sql = "UPDATE events SET title=?, category=?, event_date=?, time_slot=?, "
                   + "fee=?, venue=?, required_skills=?, eligible_dept=?, eligible_year=? "
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
            ps.setInt(10, event.getEventId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating event: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes an event by its ID.
     * Matches the deleteStudent() pattern from Slide 15.
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
                        rs.getInt("eligible_year")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding event by ID: " + e.getMessage());
        }
        return null;
    }
}
