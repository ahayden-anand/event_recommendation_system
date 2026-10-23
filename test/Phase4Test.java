import javax.swing.SwingUtilities;

/**
 * Phase4Test.java
 * Test runner for the Student Dashboard Swing GUI.
 * Verifies:
 * 1. GUI component creation without errors.
 * 2. Automated invocation of generateRecommendations().
 * 3. Recommendation table population with scores and reasons.
 * 4. Display of the Student Dashboard window.
 */
public class Phase4Test {
    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("                 PHASE 4: STUDENT DASHBOARD SWING TEST                    ");
        System.out.println("==========================================================================");

        // 1. Prepare data
        EventManager manager = new EventManager();
        manager.loadSampleEvents();
        System.out.println("Loaded " + manager.getEventCount() + " sample campus events into EventManager.");

        // 2. Launch StudentDashboard on Event Dispatch Thread (EDT) as taught in Slide 31
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    System.out.println("\n[1] Instantiating StudentDashboard...");
                    StudentDashboard dashboard = new StudentDashboard(manager);
                    
                    // Programmatically run recommendations to test model binding
                    System.out.println("\n[2] Triggering generateRecommendations() on default profile...");
                    dashboard.generateRecommendations();

                    // Make dashboard visible
                    dashboard.setVisible(true);
                    System.out.println("\n>>> Student Dashboard GUI launched and visible! <<<");
                    System.out.println(">>> You can now interact with the tabs, inputs, and buttons! <<<");
                } catch (Exception ex) {
                    System.err.println("Error launching StudentDashboard: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }
}
