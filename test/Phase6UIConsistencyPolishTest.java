import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Phase6UIConsistencyPolishTest.java
 * Verifies Phase 6 requirements:
 * 1. Unified dark navy navigation sidebars with dynamic active tab highlights.
 * 2. Tab switching properly flips active/inactive button styles and borders.
 * 3. 32px row height, refined table headers, grid color, and selection colors across all tables.
 * 4. Custom cell renderers with clean cell padding.
 * 5. Informative empty state labels on rosters and registrations.
 */
public class Phase6UIConsistencyPolishTest {

    private static int testsPassed = 0;
    private static int totalTests = 0;

    private static void assertTest(String description, boolean condition) {
        totalTests++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + description);
        } else {
            System.err.println("  [FAIL] " + description);
        }
    }

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("        PHASE 6: UI CONSISTENCY & VISUAL POLISH TEST SUITE               ");
        System.out.println("==========================================================================");

        try {
            DBConnection.initializeDatabase();

            // Set headless mode to false if possible, but GUI reflection works in headless or Swing harness
            User mockStudentUser = new User(888, "alex", "pass123", "STUDENT", "Alex Rivera", "alex@campus.edu");
            User mockCoordUser = new User(999, "coordinator", "coord123", "COORDINATOR", "Dr. Robert Smith", "coordinator@campus.edu");

            // -------------------------------------------------------------
            // 1. STUDENT DASHBOARD SIDEBAR & ACTIVE TAB HIGHLIGHT
            // -------------------------------------------------------------
            System.out.println("\n--- 1. Student Dashboard Navigation & Active Tab Highlight ---");

            StudentDashboard studentDash = new StudentDashboard(mockStudentUser);

            // Reflection to access navButtons and currentActiveTab
            Field navButtonsField = StudentDashboard.class.getDeclaredField("navButtons");
            navButtonsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, JButton> studentNavButtons = (Map<String, JButton>) navButtonsField.get(studentDash);

            Field activeTabField = StudentDashboard.class.getDeclaredField("currentActiveTab");
            activeTabField.setAccessible(true);
            String initialActive = (String) activeTabField.get(studentDash);

            assertTest("Student dashboard contains 7 navigation tabs", studentNavButtons.size() == 7);
            assertTest("Initial active student tab is 'REC'", "REC".equals(initialActive));

            JButton btnRec = studentNavButtons.get("REC");
            JButton btnBudget = studentNavButtons.get("BUDGET");

            Color royalBlue = new Color(29, 78, 216);
            Color sidebarBg = new Color(26, 37, 48);

            assertTest("Active tab 'REC' button has Royal Blue background (#1D4ED8)",
                royalBlue.equals(btnRec.getBackground()));
            assertTest("Active tab 'REC' button text is White",
                Color.WHITE.equals(btnRec.getForeground()));
            assertTest("Inactive tab 'BUDGET' button has default sidebar background (#1A2530)",
                sidebarBg.equals(btnBudget.getBackground()));

            // Switch tab to 'BUDGET' via reflection calling showTab
            Method showTabMethod = StudentDashboard.class.getDeclaredMethod("showTab", String.class);
            showTabMethod.setAccessible(true);
            showTabMethod.invoke(studentDash, "BUDGET");

            String updatedActive = (String) activeTabField.get(studentDash);
            assertTest("Switched active tab to 'BUDGET'", "BUDGET".equals(updatedActive));
            assertTest("'BUDGET' button now highlighted with Royal Blue background",
                royalBlue.equals(btnBudget.getBackground()));
            assertTest("Previously active 'REC' button reverted to sidebar background",
                sidebarBg.equals(btnRec.getBackground()));

            // Switch to 'MYREG'
            showTabMethod.invoke(studentDash, "MYREG");
            JButton btnMyReg = studentNavButtons.get("MYREG");
            assertTest("'MYREG' button highlighted when switched",
                royalBlue.equals(btnMyReg.getBackground()));

            // -------------------------------------------------------------
            // 2. COORDINATOR DASHBOARD SIDEBAR & ACTIVE TAB HIGHLIGHT
            // -------------------------------------------------------------
            System.out.println("\n--- 2. Coordinator Dashboard Navigation & Active Tab Highlight ---");

            EventManagerDashboard coordDash = new EventManagerDashboard(mockCoordUser);

            Field coordNavField = EventManagerDashboard.class.getDeclaredField("navButtons");
            coordNavField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, JButton> coordNavButtons = (Map<String, JButton>) coordNavField.get(coordDash);

            Field coordActiveTabField = EventManagerDashboard.class.getDeclaredField("currentActiveTab");
            coordActiveTabField.setAccessible(true);
            String coordInitialActive = (String) coordActiveTabField.get(coordDash);

            assertTest("Coordinator dashboard contains 3 navigation tabs", coordNavButtons.size() == 3);
            assertTest("Initial active coordinator tab is 'MANAGE'", "MANAGE".equals(coordInitialActive));

            JButton btnManage = coordNavButtons.get("MANAGE");
            JButton btnInsights = coordNavButtons.get("INSIGHTS");

            assertTest("Active coordinator tab 'MANAGE' has Royal Blue background",
                royalBlue.equals(btnManage.getBackground()));
            assertTest("Inactive tab 'INSIGHTS' has default sidebar background",
                sidebarBg.equals(btnInsights.getBackground()));

            // Switch tab to 'INSIGHTS'
            Method coordShowTab = EventManagerDashboard.class.getDeclaredMethod("showTab", String.class);
            coordShowTab.setAccessible(true);
            coordShowTab.invoke(coordDash, "INSIGHTS");

            assertTest("'INSIGHTS' button highlighted with Royal Blue background",
                royalBlue.equals(btnInsights.getBackground()));
            assertTest("Previous 'MANAGE' button reverted to sidebar background",
                sidebarBg.equals(btnManage.getBackground()));

            // -------------------------------------------------------------
            // 3. TABLE STYLING STANDARDS (32PX HEIGHT, HEADERS, COLORS)
            // -------------------------------------------------------------
            System.out.println("\n--- 3. Standardized Table Formatting & Cell Renderers ---");

            // Inspect tables in StudentDashboard
            Field recTableField = StudentDashboard.class.getDeclaredField("recTable");
            recTableField.setAccessible(true);
            JTable recTable = (JTable) recTableField.get(studentDash);

            assertTest("recTable row height is strictly 32px", recTable.getRowHeight() == 32);
            assertTest("recTable grid color is refined light slate (226, 232, 240)",
                new Color(226, 232, 240).equals(recTable.getGridColor()));
            assertTest("recTable selection background is soft blue (219, 234, 254)",
                new Color(219, 234, 254).equals(recTable.getSelectionBackground()));

            JTableHeader recHeader = recTable.getTableHeader();
            assertTest("recTable header has preferred height >= 32px",
                recHeader.getPreferredSize().height >= 32);
            assertTest("recTable header background is crisp off-white (241, 245, 249)",
                new Color(241, 245, 249).equals(recHeader.getBackground()));
            assertTest("recTable header text color is dark slate (15, 23, 42)",
                new Color(15, 23, 42).equals(recHeader.getForeground()));

            // Inspect tables in EventManagerDashboard
            Field eventsTableField = EventManagerDashboard.class.getDeclaredField("eventsTable");
            eventsTableField.setAccessible(true);
            JTable eventsTable = (JTable) eventsTableField.get(coordDash);

            assertTest("eventsTable row height is strictly 32px", eventsTable.getRowHeight() == 32);
            assertTest("eventsTable grid color is refined light slate (226, 232, 240)",
                new Color(226, 232, 240).equals(eventsTable.getGridColor()));
            assertTest("eventsTable header background is crisp off-white (241, 245, 249)",
                new Color(241, 245, 249).equals(eventsTable.getTableHeader().getBackground()));

            // Verify custom cell renderer padding
            TableCellRenderer renderer = eventsTable.getDefaultRenderer(Object.class);
            Component cellComp = renderer.getTableCellRendererComponent(eventsTable, "Test Value", false, false, 0, 0);
            assertTest("Table cell renderer produces valid JComponent with padding border",
                cellComp instanceof JComponent && ((JComponent) cellComp).getBorder() != null);

            // -------------------------------------------------------------
            // 4. EMPTY STATES & USER FEEDBACK
            // -------------------------------------------------------------
            System.out.println("\n--- 4. Empty States & User Guidance ---");

            Field lblMyRegStatusField = StudentDashboard.class.getDeclaredField("lblMyRegStatus");
            lblMyRegStatusField.setAccessible(true);
            JLabel lblMyRegStatus = (JLabel) lblMyRegStatusField.get(studentDash);
            assertTest("Student My Registrations panel includes informative status/empty guidance label",
                lblMyRegStatus != null && !lblMyRegStatus.getText().trim().isEmpty());

            Field lblRosterStatusField = EventManagerDashboard.class.getDeclaredField("lblRosterStatus");
            lblRosterStatusField.setAccessible(true);
            JLabel lblRosterStatus = (JLabel) lblRosterStatusField.get(coordDash);
            assertTest("Coordinator Dual Roster panel includes informative attendee/waitlist status label",
                lblRosterStatus != null && !lblRosterStatus.getText().trim().isEmpty());

            // Clean up window resources
            studentDash.dispose();
            coordDash.dispose();

            // -------------------------------------------------------------
            // SUMMARY
            // -------------------------------------------------------------
            System.out.println("\n==========================================================================");
            System.out.println("PHASE 6 TEST RESULTS: " + testsPassed + " / " + totalTests + " TESTS PASSED");
            System.out.println("==========================================================================");

            if (testsPassed == totalTests) {
                System.out.println(">>> ALL PHASE 6 REQUIREMENTS VERIFIED SUCCESSFULLY! <<<");
            } else {
                System.err.println(">>> SOME TESTS FAILED! CHECK OUTPUT ABOVE. <<<");
                System.exit(1);
            }

        } catch (Exception ex) {
            System.err.println("Unexpected exception in Phase 6 tests: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
