import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * EventManagerDashboard.java - Complete Campus SaaS Redesign
 * Professional, clean SaaS-style desktop UI for Event Coordinators.
 * 
 * Features:
 * 1. Event Management (CRUD: Create, Read, Update, Delete) with format, level, times, and capacity.
 * 2. Real Event Audience Insights:
 *    - Number of matching student profiles
 *    - Common matching interests breakdown
 *    - Suitable skill levels distribution
 *    - Budget affordability count
 *    - Time availability alignment count
 * 3. Attendee & Waitlist Management with capacity metrics.
 */
public class EventManagerDashboard extends JFrame {

    private User currentUser;
    private EventDAO eventDAO;
    private RegistrationDAO registrationDAO;
    private StudentDAO studentDAO;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;

    // Tab 1: Manage Events
    private DefaultTableModel eventsTableModel;
    private JTable eventsTable;
    private JTextField txtTitle, txtDate, txtFee, txtVenue, txtSkills, txtDept, txtStartTime, txtEndTime, txtCapacity;
    private JComboBox<String> cmbCategory, cmbSlot, cmbFormat, cmbLevel;
    private JComboBox<Integer> cmbYear;
    private Integer selectedEventId = null;

    // Tab 2: Audience Insights
    private JComboBox<String> cmbInsightEvent;
    private JTextArea txtAudienceMetrics;
    private ArrayList<Event> cachedEvents;

    // Tab 3: Registrations & Waitlist
    private DefaultTableModel regTableModel;
    private JTable regTable;

    // Visual Palette
    private static final Color BG_CANVAS = new Color(245, 247, 250);
    private static final Color SIDEBAR_BG = new Color(26, 37, 48);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color CARD_BORDER = new Color(220, 224, 230);
    private static final Color TEXT_NAVY = new Color(26, 37, 48);
    private static final Color TEXT_MUTED = new Color(110, 120, 135);
    private static final Color ACCENT_BLUE = new Color(41, 128, 185);

    public EventManagerDashboard(User user) {
        this.currentUser = user;
        this.eventDAO = new EventDAO();
        this.registrationDAO = new RegistrationDAO();
        this.studentDAO = new StudentDAO();

        setTitle("Campus Event Recommendation System - Coordinator Portal (" + currentUser.getFullName() + ")");
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initUI();
        refreshAllData();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_CANVAS);

        // Sidebar Navigation
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(240, 0));

        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        brandPanel.setOpaque(false);
        brandPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 20, 20));

        JLabel lblBrand = new JLabel("Coordinator Hub");
        lblBrand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblBrand.setForeground(Color.WHITE);

        JLabel lblUser = new JLabel(currentUser.getFullName());
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(new Color(180, 190, 205));

        brandPanel.add(lblBrand);
        brandPanel.add(lblUser);
        sidebar.add(brandPanel, BorderLayout.NORTH);

        JPanel navPanel = new JPanel(new GridLayout(4, 1, 0, 6));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JButton btnNavManage = createNavButton("Manage Events");
        JButton btnNavInsights = createNavButton("Audience Insights");
        JButton btnNavRegs = createNavButton("Registrations & Waitlist");

        navPanel.add(btnNavManage);
        navPanel.add(btnNavInsights);
        navPanel.add(btnNavRegs);
        sidebar.add(navPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 20, 15));

        JButton btnLogout = new JButton("Sign Out");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(new Color(192, 57, 43));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> {
            dispose();
            new Main().setVisible(true);
        });
        footerPanel.add(btnLogout, BorderLayout.CENTER);
        sidebar.add(footerPanel, BorderLayout.SOUTH);

        root.add(sidebar, BorderLayout.WEST);

        // Main Content Cards
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(BG_CANVAS);

        mainContentPanel.add(createManageEventsPanel(), "MANAGE");
        mainContentPanel.add(createAudienceInsightsPanel(), "INSIGHTS");
        mainContentPanel.add(createRegistrationsPanel(), "REGS");

        root.add(mainContentPanel, BorderLayout.CENTER);

        btnNavManage.addActionListener(e -> showTab("MANAGE"));
        btnNavInsights.addActionListener(e -> showTab("INSIGHTS"));
        btnNavRegs.addActionListener(e -> showTab("REGS"));

        add(root);
    }

    private void showTab(String key) {
        cardLayout.show(mainContentPanel, key);
        refreshAllData();
    }

    private JButton createNavButton(String title) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(230, 235, 245));
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    // --- TAB 1: MANAGE EVENTS ---
    private JPanel createManageEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Event Creation & Management", "Add, modify, or remove campus events.");
        panel.add(header, BorderLayout.NORTH);

        // Split: Form on Top, Catalog Table on Bottom
        JPanel formCard = new JPanel(new GridLayout(4, 4, 10, 8));
        formCard.setBackground(CARD_BG);
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Row 1
        formCard.add(new JLabel("Title: *"));
        txtTitle = new JTextField();
        formCard.add(txtTitle);

        formCard.add(new JLabel("Category: *"));
        cmbCategory = new JComboBox<String>(new String[]{"Coding", "AI", "Robotics", "Entrepreneurship", "Sports", "Arts", "Public Speaking", "Design"});
        formCard.add(cmbCategory);

        // Row 2
        formCard.add(new JLabel("Date (YYYY-MM-DD): *"));
        txtDate = new JTextField("2026-11-15");
        formCard.add(txtDate);

        formCard.add(new JLabel("Time Slot & Format:"));
        JPanel slotFormat = new JPanel(new GridLayout(1, 2, 5, 0));
        slotFormat.setOpaque(false);
        cmbSlot = new JComboBox<String>(new String[]{"Morning", "Afternoon", "Evening", "All"});
        cmbFormat = new JComboBox<String>(new String[]{"In-person", "Online", "Either"});
        slotFormat.add(cmbSlot);
        slotFormat.add(cmbFormat);
        formCard.add(slotFormat);

        // Row 3
        formCard.add(new JLabel("Fee (Rs) & Max Capacity:"));
        JPanel feeCap = new JPanel(new GridLayout(1, 2, 5, 0));
        feeCap.setOpaque(false);
        txtFee = new JTextField("0.0");
        txtCapacity = new JTextField("50");
        feeCap.add(txtFee);
        feeCap.add(txtCapacity);
        formCard.add(feeCap);

        formCard.add(new JLabel("Target Level & Venue:"));
        JPanel lvlVenue = new JPanel(new GridLayout(1, 2, 5, 0));
        lvlVenue.setOpaque(false);
        cmbLevel = new JComboBox<String>(new String[]{"All", "Beginner", "Intermediate", "Advanced"});
        txtVenue = new JTextField("Main Auditorium");
        lvlVenue.add(cmbLevel);
        lvlVenue.add(txtVenue);
        formCard.add(lvlVenue);

        // Row 4
        formCard.add(new JLabel("Skills & Department:"));
        JPanel skillsDept = new JPanel(new GridLayout(1, 2, 5, 0));
        skillsDept.setOpaque(false);
        txtSkills = new JTextField("Java, Python");
        txtDept = new JTextField("All");
        skillsDept.add(txtSkills);
        skillsDept.add(txtDept);
        formCard.add(skillsDept);

        formCard.add(new JLabel("Start & End Time (HH:mm):"));
        JPanel times = new JPanel(new GridLayout(1, 2, 5, 0));
        times.setOpaque(false);
        txtStartTime = new JTextField("10:00");
        txtEndTime = new JTextField("12:00");
        times.add(txtStartTime);
        times.add(txtEndTime);
        formCard.add(times);

        // Buttons Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);
        JButton btnAdd = new JButton("Add Event");
        JButton btnUpdate = new JButton("Update Selected");
        JButton btnDelete = new JButton("Delete Event");
        JButton btnClear = new JButton("Clear Form");

        stylePrimaryButton(btnAdd);
        stylePrimaryButton(btnUpdate);
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setBackground(new Color(192, 57, 43));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        btnBar.add(btnAdd);
        btnBar.add(btnUpdate);
        btnBar.add(btnDelete);
        btnBar.add(btnClear);

        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setOpaque(false);
        topContainer.add(formCard, BorderLayout.CENTER);
        topContainer.add(btnBar, BorderLayout.SOUTH);

        // Events Table
        String[] cols = {"ID", "Title", "Category", "Date", "Slot", "Time", "Fee", "Venue", "Format", "Level", "Capacity"};
        eventsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        eventsTable = createStyledTable(eventsTableModel);

        eventsTable.getSelectionModel().addListSelectionListener(e -> {
            int row = eventsTable.getSelectedRow();
            if (row >= 0) {
                selectedEventId = (Integer) eventsTableModel.getValueAt(row, 0);
                txtTitle.setText((String) eventsTableModel.getValueAt(row, 1));
                cmbCategory.setSelectedItem((String) eventsTableModel.getValueAt(row, 2));
                txtDate.setText((String) eventsTableModel.getValueAt(row, 3));
                cmbSlot.setSelectedItem((String) eventsTableModel.getValueAt(row, 4));
                txtFee.setText(String.valueOf(eventsTableModel.getValueAt(row, 6)));
                txtVenue.setText((String) eventsTableModel.getValueAt(row, 7));
                cmbFormat.setSelectedItem((String) eventsTableModel.getValueAt(row, 8));
                cmbLevel.setSelectedItem((String) eventsTableModel.getValueAt(row, 9));
            }
        });

        btnAdd.addActionListener(e -> handleAddEvent());
        btnUpdate.addActionListener(e -> handleUpdateEvent());
        btnDelete.addActionListener(e -> handleDeleteEvent());
        btnClear.addActionListener(e -> clearForm());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topContainer, new JScrollPane(eventsTable));
        split.setResizeWeight(0.45);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 2: AUDIENCE INSIGHTS ---
    private JPanel createAudienceInsightsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Event Audience Insights", "Calculate real matching student profiles and potential attendee statistics.");
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        selectorPanel.setOpaque(false);

        JLabel lblSel = new JLabel("Select Event:");
        lblSel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbInsightEvent = new JComboBox<String>();
        cmbInsightEvent.setPreferredSize(new Dimension(300, 28));

        JButton btnCalc = new JButton("Calculate Insights");
        stylePrimaryButton(btnCalc);
        btnCalc.addActionListener(e -> calculateAudienceInsights());

        selectorPanel.add(lblSel);
        selectorPanel.add(cmbInsightEvent);
        selectorPanel.add(btnCalc);
        header.add(selectorPanel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        txtAudienceMetrics = new JTextArea();
        txtAudienceMetrics.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtAudienceMetrics.setEditable(false);
        txtAudienceMetrics.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));

        panel.add(new JScrollPane(txtAudienceMetrics), BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 3: REGISTRATIONS & WAITLIST ---
    private JPanel createRegistrationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = createHeaderPanel("Confirmed Registrations & Waitlist", "View students registered across all campus activities.");
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Reg ID", "Student Name", "Student Email", "Event ID", "Event Title", "Registered Date"};
        regTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        regTable = createStyledTable(regTableModel);

        panel.add(new JScrollPane(regTable), BorderLayout.CENTER);
        return panel;
    }

    // --- LOGIC METHODS ---
    private void refreshAllData() {
        cachedEvents = eventDAO.getAllEvents();
        refreshEventsTable();
        refreshInsightsDropdown();
        refreshRegistrationsTable();
    }

    private void refreshEventsTable() {
        eventsTableModel.setRowCount(0);
        for (int i = 0; i < cachedEvents.size(); i++) {
            Event ev = cachedEvents.get(i);
            eventsTableModel.addRow(new Object[]{
                ev.getEventId(),
                ev.getTitle(),
                ev.getCategory(),
                ev.getEventDate(),
                ev.getTimeSlot(),
                ev.getStartTime() + "-" + ev.getEndTime(),
                ev.getFee(),
                ev.getVenue(),
                ev.getEventFormat(),
                ev.getTargetSkillLevel(),
                ev.getMaxCapacity()
            });
        }
    }

    private void refreshInsightsDropdown() {
        cmbInsightEvent.removeAllItems();
        for (int i = 0; i < cachedEvents.size(); i++) {
            Event ev = cachedEvents.get(i);
            cmbInsightEvent.addItem(ev.getEventId() + ": " + ev.getTitle());
        }
    }

    private void refreshRegistrationsTable() {
        regTableModel.setRowCount(0);
        ArrayList<Registration> list = registrationDAO.getAllRegistrations();
        for (int i = 0; i < list.size(); i++) {
            Registration r = list.get(i);
            regTableModel.addRow(new Object[]{
                r.getRegId(),
                r.getStudentName(),
                r.getStudentEmail(),
                r.getEventId(),
                r.getEventTitle(),
                r.getRegistrationDate()
            });
        }
    }

    private void handleAddEvent() {
        try {
            Event ev = new Event(
                0, txtTitle.getText().trim(), (String) cmbCategory.getSelectedItem(),
                txtDate.getText().trim(), (String) cmbSlot.getSelectedItem(),
                Double.parseDouble(txtFee.getText().trim()), txtVenue.getText().trim(),
                txtSkills.getText().trim(), txtDept.getText().trim(), 0,
                (String) cmbFormat.getSelectedItem(), (String) cmbLevel.getSelectedItem(),
                txtStartTime.getText().trim(), txtEndTime.getText().trim(),
                Integer.parseInt(txtCapacity.getText().trim())
            );
            if (eventDAO.addEvent(ev)) {
                JOptionPane.showMessageDialog(this, "Event successfully added!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshAllData();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid form data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdateEvent() {
        if (selectedEventId == null) {
            JOptionPane.showMessageDialog(this, "Please select an event from the table to update.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Event ev = new Event(
                selectedEventId, txtTitle.getText().trim(), (String) cmbCategory.getSelectedItem(),
                txtDate.getText().trim(), (String) cmbSlot.getSelectedItem(),
                Double.parseDouble(txtFee.getText().trim()), txtVenue.getText().trim(),
                txtSkills.getText().trim(), txtDept.getText().trim(), 0,
                (String) cmbFormat.getSelectedItem(), (String) cmbLevel.getSelectedItem(),
                txtStartTime.getText().trim(), txtEndTime.getText().trim(),
                Integer.parseInt(txtCapacity.getText().trim())
            );
            if (eventDAO.updateEvent(ev)) {
                JOptionPane.showMessageDialog(this, "Event successfully updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshAllData();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error updating event: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDeleteEvent() {
        if (selectedEventId == null) {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this event?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION) {
            if (eventDAO.deleteEvent(selectedEventId)) {
                JOptionPane.showMessageDialog(this, "Event deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshAllData();
            }
        }
    }

    private void clearForm() {
        selectedEventId = null;
        txtTitle.setText("");
        txtFee.setText("0.0");
        txtVenue.setText("Main Auditorium");
        txtSkills.setText("");
        txtDept.setText("All");
        txtCapacity.setText("50");
    }

    private void calculateAudienceInsights() {
        int idx = cmbInsightEvent.getSelectedIndex();
        if (idx < 0 || idx >= cachedEvents.size()) return;

        Event event = cachedEvents.get(idx);
        ArrayList<Student> allStudents = studentDAO.getAllStudents();
        int registeredCount = registrationDAO.getRegistrationCountForEvent(event.getEventId());
        int waitlistCount = registrationDAO.getWaitlistCountForEvent(event.getEventId());

        int eligibleCount = 0;
        int matchingInterestCount = 0;
        int budgetAffordableCount = 0;
        int slotAvailableCount = 0;
        int skillLevelMatchCount = 0;

        for (int i = 0; i < allStudents.size(); i++) {
            Student s = allStudents.get(i);

            // Dept check
            String eDept = event.getEligibleDept().toLowerCase();
            if (eDept.equals("all") || eDept.contains(s.getDepartment().toLowerCase())) {
                eligibleCount++;
            }

            // Interest check
            if (s.getInterests() != null && s.getInterests().toLowerCase().contains(event.getCategory().toLowerCase())) {
                matchingInterestCount++;
            }

            // Budget check
            if (s.getMaxBudget() >= event.getFee() && (!s.isFreeOnly() || event.getFee() == 0)) {
                budgetAffordableCount++;
            }

            // Slot check
            if (s.getAvailableSlot() != null && (s.getAvailableSlot().equalsIgnoreCase("all") || s.getAvailableSlot().toLowerCase().contains(event.getTimeSlot().toLowerCase()))) {
                slotAvailableCount++;
            }

            // Level check
            if (event.getTargetSkillLevel().equalsIgnoreCase("all") || event.getTargetSkillLevel().equalsIgnoreCase(s.getSkillLevel())) {
                skillLevelMatchCount++;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                     REAL AUDIENCE INSIGHTS REPORT FOR COORDINATOR                      \n");
        sb.append("========================================================================================\n\n");
        sb.append("Event Title           : ").append(event.getTitle()).append(" [ID: ").append(event.getEventId()).append("]\n");
        sb.append("Category              : ").append(event.getCategory()).append(" | Venue: ").append(event.getVenue()).append(" (").append(event.getEventFormat()).append(")\n");
        sb.append("Date & Time           : ").append(event.getEventDate()).append(" (").append(event.getTimeSlot()).append(" ").append(event.getStartTime()).append("-").append(event.getEndTime()).append(")\n");
        sb.append("Registration Fee      : Rs. ").append(event.getFee()).append(" | Seat Capacity: ").append(event.getMaxCapacity()).append("\n\n");

        sb.append("--- AUDIENCE DEMAND METRICS (Based on ").append(allStudents.size()).append(" Registered Student Profiles) ---\n");
        sb.append("1. Total Academically Eligible Students : ").append(eligibleCount).append(" students\n");
        sb.append("2. Students with Matching Interest (").append(event.getCategory()).append(") : ").append(matchingInterestCount).append(" students\n");
        sb.append("3. Students Affording the Fee (<= Rs. ").append(event.getFee()).append(")  : ").append(budgetAffordableCount).append(" students\n");
        sb.append("4. Students Available at Time Slot (").append(event.getTimeSlot()).append(")   : ").append(slotAvailableCount).append(" students\n");
        sb.append("5. Target Skill Level Match (").append(event.getTargetSkillLevel()).append(")       : ").append(skillLevelMatchCount).append(" students\n\n");

        sb.append("--- ENROLLMENT STATUS ---\n");
        sb.append("Confirmed Attendees Enrolled            : ").append(registeredCount).append(" / ").append(event.getMaxCapacity()).append(" seats\n");
        sb.append("Students on Smart Waitlist Queue        : ").append(waitlistCount).append(" waiting\n\n");

        double fillRate = event.getMaxCapacity() > 0 ? (registeredCount * 100.0 / event.getMaxCapacity()) : 0;
        sb.append("Capacity Utilization                    : ").append(String.format("%.1f", fillRate)).append("%\n");
        sb.append("========================================================================================\n");

        txtAudienceMetrics.setText(sb.toString());
    }

    private JPanel createHeaderPanel(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        JLabel l1 = new JLabel(title);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l1.setForeground(TEXT_NAVY);

        JLabel l2 = new JLabel(subtitle);
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l2.setForeground(TEXT_MUTED);

        JPanel titles = new JPanel(new GridLayout(2, 1, 2, 2));
        titles.setOpaque(false);
        titles.add(l1);
        titles.add(l2);

        p.add(titles, BorderLayout.WEST);
        return p;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(235, 238, 242));

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(240, 243, 248));
        th.setForeground(TEXT_NAVY);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.setDefaultRenderer(Object.class, center);

        return table;
    }

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(ACCENT_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
