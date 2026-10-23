import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * RegistrationDAO.java
 * Data Access Object for Registration transactions in MySQL using JDBC.
 */
public class RegistrationDAO {

    /**
     * Creates the registrations table if it does not exist.
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
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating registrations table: " + e.getMessage());
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
}
