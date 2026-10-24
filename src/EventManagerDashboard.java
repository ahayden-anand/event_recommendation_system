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
    private java.util.Map<String, JButton> navButtons = new java.util.LinkedHashMap<String, JButton>();
    private String currentActiveTab = "MANAGE";

    // Tab 1: Manage Events
    private DefaultTableModel eventsTableModel;
    private JTable eventsTable;
    private JTextField txtTitle, txtDate, txtFee, txtVenue, txtSkills, txtDept, txtStartTime, txtEndTime, txtCapacity;
    private JComboBox<String> cmbCategory, cmbSlot, cmbFormat, cmbLevel;
    private JComboBox<Integer> cmbYear;
    private Integer selectedEventId = null;

    // Tab 2: Audience Insights & Analytics
    private JComboBox<String> cmbInsightEvent;
    private JTextArea txtAudienceMetrics;
    private ArrayList<Event> cachedEvents;
    private JLabel lblKpiTotalEvents, lblKpiTotalRegs, lblKpiWaitlist, lblKpiRevenue;
    private JLabel lblKpiUtilization, lblKpiFreePaid, lblKpiTopCategory, lblKpiAvgFee;
    private DefaultTableModel insightsTableModel;
    private JTable insightsTable;

    // Tab 3: Registrations & Waitlist Management
    private DefaultTableModel regTableModel;
    private JTable regTable;
    private DefaultTableModel waitlistTableModel;
    private JTable waitlistTable;
    private JTextField txtRegSearch;
    private JComboBox<String> cmbRegFilterEvent;
    private JLabel lblRosterStatus;

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

        JButton btnNavManage = createNavButton("Manage Events", "MANAGE");
        JButton btnNavInsights = createNavButton("Audience Insights", "INSIGHTS");
        JButton btnNavRegs = createNavButton("Registrations & Waitlist", "REGS");

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

        updateActiveNavButtons();
        add(root);
    }

    private void showTab(String key) {
        currentActiveTab = key;
        cardLayout.show(mainContentPanel, key);
        updateActiveNavButtons();
        refreshAllData();
    }

    private void updateActiveNavButtons() {
        for (java.util.Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            String tabKey = entry.getKey();
            JButton btn = entry.getValue();
            if (tabKey.equals(currentActiveTab)) {
                btn.setBackground(new Color(29, 78, 216)); // Vivid Royal Blue (#1D4ED8)
                btn.setForeground(Color.WHITE);
                btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(96, 165, 250)),
                    BorderFactory.createEmptyBorder(10, 12, 10, 15)
                ));
            } else {
                btn.setBackground(SIDEBAR_BG);
                btn.setForeground(new Color(203, 213, 225));
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 15));
            }
        }
    }

    private JButton createNavButton(String title, final String tabKey) {
        final JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(SIDEBAR_BG);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 15));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!tabKey.equals(currentActiveTab)) {
                    btn.setBackground(new Color(30, 41, 59));
                    btn.setForeground(Color.WHITE);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!tabKey.equals(currentActiveTab)) {
                    btn.setBackground(SIDEBAR_BG);
                    btn.setForeground(new Color(203, 213, 225));
                }
            }
        });

        navButtons.put(tabKey, btn);
        return btn;
    }

    // --- TAB 1: MANAGE EVENTS ---
    private JPanel createManageEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 12));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel header = createHeaderPanel("Event Creation & Management", "Add, update, or remove campus events with structured category and capacity controls.");
        panel.add(header, BorderLayout.NORTH);

        // 3 Grouped Sections for Form Fields
        JPanel groupsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        groupsPanel.setOpaque(false);

        // Group 1: Event Identity (Blue accent)
        JPanel g1 = createFormGroupCard("1. Event Identity", new Color(41, 128, 185));
        JPanel g1Body = new JPanel(new GridLayout(4, 2, 8, 8));
        g1Body.setOpaque(false);
        g1Body.add(new JLabel("Title: *"));
        txtTitle = new JTextField();
        g1Body.add(txtTitle);
        g1Body.add(new JLabel("Category: *"));
        cmbCategory = new JComboBox<String>(new String[]{"Coding", "AI", "Robotics", "Entrepreneurship", "Sports", "Arts", "Public Speaking", "Design", "Tech"});
        g1Body.add(cmbCategory);
        g1Body.add(new JLabel("Format:"));
        cmbFormat = new JComboBox<String>(new String[]{"In-person", "Online", "Hybrid"});
        g1Body.add(cmbFormat);
        g1Body.add(new JLabel("Target Level:"));
        cmbLevel = new JComboBox<String>(new String[]{"All", "Beginner", "Intermediate", "Advanced"});
        g1Body.add(cmbLevel);
        g1.add(g1Body, BorderLayout.CENTER);

        // Group 2: Schedule & Venue (Green accent)
        JPanel g2 = createFormGroupCard("2. Schedule & Venue", new Color(39, 174, 96));
        JPanel g2Body = new JPanel(new GridLayout(4, 2, 8, 8));
        g2Body.setOpaque(false);
        g2Body.add(new JLabel("Date (YYYY-MM-DD): *"));
        txtDate = new JTextField("2026-11-15");
        g2Body.add(txtDate);
        g2Body.add(new JLabel("Time Slot:"));
        cmbSlot = new JComboBox<String>(new String[]{"Morning", "Afternoon", "Evening", "All"});
        g2Body.add(cmbSlot);
        g2Body.add(new JLabel("Times (HH:mm):"));
        JPanel timesPanel = new JPanel(new GridLayout(1, 2, 4, 0));
        timesPanel.setOpaque(false);
        txtStartTime = new JTextField("10:00");
        txtEndTime = new JTextField("12:00");
        timesPanel.add(txtStartTime);
        timesPanel.add(txtEndTime);
        g2Body.add(timesPanel);
        g2Body.add(new JLabel("Venue: *"));
        txtVenue = new JTextField("Main Auditorium");
        g2Body.add(txtVenue);
        g2.add(g2Body, BorderLayout.CENTER);

        // Group 3: Eligibility & Capacity (Purple accent)
        JPanel g3 = createFormGroupCard("3. Eligibility & Capacity", new Color(142, 68, 173));
        JPanel g3Body = new JPanel(new GridLayout(4, 2, 8, 8));
        g3Body.setOpaque(false);
        g3Body.add(new JLabel("Department:"));
        txtDept = new JTextField("All");
        g3Body.add(txtDept);
        g3Body.add(new JLabel("Year & Capacity:"));
        JPanel yearCap = new JPanel(new GridLayout(1, 2, 4, 0));
        yearCap.setOpaque(false);
        cmbYear = new JComboBox<Integer>(new Integer[]{0, 1, 2, 3, 4});
        txtCapacity = new JTextField("50");
        yearCap.add(cmbYear);
        yearCap.add(txtCapacity);
        g3Body.add(yearCap);
        g3Body.add(new JLabel("Fee (Rs):"));
        txtFee = new JTextField("0.0");
        g3Body.add(txtFee);
        g3Body.add(new JLabel("Required Skills:"));
        txtSkills = new JTextField("Java, Python");
        g3Body.add(txtSkills);
        g3.add(g3Body, BorderLayout.CENTER);

        groupsPanel.add(g1);
        groupsPanel.add(g2);
        groupsPanel.add(g3);

        // Buttons Bar with distinct color coding
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnBar.setOpaque(false);

        JButton btnAdd = new JButton("+ Add Event");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setBackground(new Color(39, 174, 96));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnUpdate = new JButton("✎ Update Selected");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUpdate.setBackground(new Color(41, 128, 185));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnClear = new JButton("↺ Clear Form");
        btnClear.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClear.setBackground(new Color(127, 140, 141));
        btnClear.setForeground(Color.WHITE);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnDelete = new JButton("✕ Delete Event");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setBackground(new Color(192, 57, 43));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnBar.add(btnAdd);
        btnBar.add(btnUpdate);
        btnBar.add(btnClear);
        btnBar.add(btnDelete);

        JPanel topContainer = new JPanel(new BorderLayout(8, 8));
        topContainer.setOpaque(false);
        topContainer.add(groupsPanel, BorderLayout.CENTER);
        topContainer.add(btnBar, BorderLayout.SOUTH);

        // Events Table
        String[] cols = {"ID", "Title", "Category", "Date", "Slot", "Time", "Fee", "Venue", "Format", "Level", "Capacity"};
        eventsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        eventsTable = createStyledTable(eventsTableModel);

        eventsTable.getSelectionModel().addListSelectionListener(e -> {
            int row = eventsTable.getSelectedRow();
            if (row >= 0 && row < eventsTableModel.getRowCount()) {
                selectedEventId = (Integer) eventsTableModel.getValueAt(row, 0);
                Event fullEv = eventDAO.getEventById(selectedEventId);
                if (fullEv != null) {
                    txtTitle.setText(fullEv.getTitle());
                    cmbCategory.setSelectedItem(fullEv.getCategory());
                    txtDate.setText(fullEv.getEventDate());
                    cmbSlot.setSelectedItem(fullEv.getTimeSlot());
                    txtFee.setText(String.valueOf(fullEv.getFee()));
                    txtVenue.setText(fullEv.getVenue());
                    cmbFormat.setSelectedItem(fullEv.getEventFormat());
                    cmbLevel.setSelectedItem(fullEv.getTargetSkillLevel());
                    txtCapacity.setText(String.valueOf(fullEv.getMaxCapacity()));
                    txtSkills.setText(fullEv.getRequiredSkills() != null ? fullEv.getRequiredSkills() : "");
                    txtDept.setText(fullEv.getEligibleDept() != null ? fullEv.getEligibleDept() : "All");
                    cmbYear.setSelectedItem(fullEv.getEligibleYear());
                    txtStartTime.setText(fullEv.getStartTime() != null ? fullEv.getStartTime() : "10:00");
                    txtEndTime.setText(fullEv.getEndTime() != null ? fullEv.getEndTime() : "12:00");
                }
            }
        });

        btnAdd.addActionListener(e -> handleAddEvent());
        btnUpdate.addActionListener(e -> handleUpdateEvent());
        btnDelete.addActionListener(e -> handleDeleteEvent());
        btnClear.addActionListener(e -> clearForm());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topContainer, new JScrollPane(eventsTable));
        split.setResizeWeight(0.48);
        split.setBorder(null);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // --- TAB 2: AUDIENCE INSIGHTS ---
    private JPanel createAudienceInsightsPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 12));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel header = createHeaderPanel("Audience Insights & Campus Analytics", "Real-time key performance indicators and matching student demand statistics.");
        panel.add(header, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // 8 KPI Metric Cards Grid (2 rows x 4 cols)
        JPanel kpiGrid = new JPanel(new GridLayout(2, 4, 12, 10));
        kpiGrid.setOpaque(false);
        kpiGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        lblKpiTotalEvents = new JLabel("0");
        lblKpiTotalRegs = new JLabel("0");
        lblKpiWaitlist = new JLabel("0");
        lblKpiRevenue = new JLabel("Rs. 0");
        lblKpiUtilization = new JLabel("0.0%");
        lblKpiFreePaid = new JLabel("0 / 0");
        lblKpiTopCategory = new JLabel("None");
        lblKpiAvgFee = new JLabel("Rs. 0");

        kpiGrid.add(createKpiCard("Total Events Hosted", lblKpiTotalEvents, "Active campus events catalog", new Color(41, 128, 185)));
        kpiGrid.add(createKpiCard("Total Confirmed Regs", lblKpiTotalRegs, "Enrolled students across events", new Color(39, 174, 96)));
        kpiGrid.add(createKpiCard("Active Waitlist Queue", lblKpiWaitlist, "Students awaiting seat openings", new Color(211, 84, 0)));
        kpiGrid.add(createKpiCard("Estimated Revenue", lblKpiRevenue, "Gross registration receipts", new Color(142, 68, 173)));
        kpiGrid.add(createKpiCard("Avg Capacity Utilization", lblKpiUtilization, "Overall campus seat fill rate", new Color(22, 160, 133)));
        kpiGrid.add(createKpiCard("Free vs Paid Events", lblKpiFreePaid, "No-cost vs ticketed events", new Color(52, 73, 94)));
        kpiGrid.add(createKpiCard("Most Popular Category", lblKpiTopCategory, "Highest student enrollments", new Color(230, 126, 34)));
        kpiGrid.add(createKpiCard("Average Event Fee", lblKpiAvgFee, "Per-attendee fee baseline", new Color(41, 128, 185)));

        centerPanel.add(kpiGrid);
        centerPanel.add(Box.createVerticalStrut(12));

        // Event Attendance Breakdown Table
        JPanel tableCard = new JPanel(new BorderLayout(0, 6));
        tableCard.setBackground(CARD_BG);
        tableCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblTableTitle = new JLabel("Event Attendance & Capacity Utilization Breakdown");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTableTitle.setForeground(TEXT_NAVY);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Event Title", "Category", "Schedule", "Capacity", "Confirmed", "Waitlist", "Utilization", "Status"};
        insightsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        insightsTable = createStyledTable(insightsTableModel);

        // Color renderer for utilization column
        DefaultTableCellRenderer utilRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected && value != null) {
                    String str = value.toString();
                    if (str.contains("100%")) {
                        c.setForeground(new Color(192, 57, 43));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if (str.startsWith("7") || str.startsWith("8") || str.startsWith("9")) {
                        c.setForeground(new Color(39, 174, 96));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else {
                        c.setForeground(TEXT_NAVY);
                    }
                }
                return c;
            }
        };
        insightsTable.getColumnModel().getColumn(7).setCellRenderer(utilRenderer);

        JScrollPane scrollTable = new JScrollPane(insightsTable);
        scrollTable.setPreferredSize(new Dimension(0, 180));
        tableCard.add(scrollTable, BorderLayout.CENTER);
        centerPanel.add(tableCard);
        centerPanel.add(Box.createVerticalStrut(12));

        // Deep-Dive Persona Selector & Narrative Panel
        JPanel deepDiveCard = new JPanel(new BorderLayout(0, 8));
        deepDiveCard.setBackground(CARD_BG);
        deepDiveCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JPanel ddHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        ddHeader.setOpaque(false);
        JLabel lblSel = new JLabel("Deep-Dive Persona Analysis for Event:");
        lblSel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbInsightEvent = new JComboBox<String>();
        cmbInsightEvent.setPreferredSize(new Dimension(320, 28));

        JButton btnCalc = new JButton("Analyze Audience Matching");
        stylePrimaryButton(btnCalc);
        btnCalc.addActionListener(e -> calculateAudienceInsights());

        ddHeader.add(lblSel);
        ddHeader.add(cmbInsightEvent);
        ddHeader.add(btnCalc);
        deepDiveCard.add(ddHeader, BorderLayout.NORTH);

        txtAudienceMetrics = new JTextArea(6, 40);
        txtAudienceMetrics.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtAudienceMetrics.setEditable(false);
        txtAudienceMetrics.setBackground(new Color(248, 250, 253));
        txtAudienceMetrics.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 230, 245), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        deepDiveCard.add(new JScrollPane(txtAudienceMetrics), BorderLayout.CENTER);
        centerPanel.add(deepDiveCard);

        JScrollPane mainScroll = new JScrollPane(centerPanel);
        mainScroll.setBorder(null);
        mainScroll.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(mainScroll, BorderLayout.CENTER);

        return panel;
    }

    // --- TAB 3: REGISTRATIONS & WAITLIST ---
    private JPanel createRegistrationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBackground(BG_CANVAS);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel header = createHeaderPanel("Confirmed Registrations & Smart Waitlist Queue", "Inspect attendee rosters, filter by event, and manage waitlisted applicants.");
        panel.add(header, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterBar.setBackground(CARD_BG);
        filterBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        JLabel lblSearch = new JLabel("Search Student:");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtRegSearch = new JTextField(14);

        JLabel lblFilterEv = new JLabel("Event Filter:");
        lblFilterEv.setFont(new Font("Segoe UI", Font.BOLD, 12));
        cmbRegFilterEvent = new JComboBox<String>();
        cmbRegFilterEvent.setPreferredSize(new Dimension(280, 28));

        JButton btnFilterReg = new JButton("Filter");
        stylePrimaryButton(btnFilterReg);
        btnFilterReg.setPreferredSize(new Dimension(80, 28));
        btnFilterReg.addActionListener(e -> refreshRegistrationsTable());

        JButton btnResetReg = new JButton("Reset");
        btnResetReg.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnResetReg.setPreferredSize(new Dimension(80, 28));
        btnResetReg.setFocusPainted(false);
        btnResetReg.addActionListener(e -> {
            txtRegSearch.setText("");
            if (cmbRegFilterEvent.getItemCount() > 0) cmbRegFilterEvent.setSelectedIndex(0);
            refreshRegistrationsTable();
        });

        filterBar.add(lblSearch);
        filterBar.add(txtRegSearch);
        filterBar.add(lblFilterEv);
        filterBar.add(cmbRegFilterEvent);
        filterBar.add(btnFilterReg);
        filterBar.add(btnResetReg);

        // Sub-Tabbed Pane for Confirmed vs Waitlist
        JTabbedPane regTabs = new JTabbedPane();
        regTabs.setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Sub-Tab 1: Confirmed Attendees
        String[] confCols = {"Reg ID", "Student Name", "Student Email", "Event ID", "Event Title", "Registered Date", "Status"};
        regTableModel = new DefaultTableModel(confCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        regTable = createStyledTable(regTableModel);
        regTabs.addTab("Confirmed Attendees", new JScrollPane(regTable));

        // Sub-Tab 2: Smart Waitlist Queue
        String[] wlCols = {"Waitlist ID", "Queue Pos", "Student Name", "Student Email", "Event Title", "Added At", "Status"};
        waitlistTableModel = new DefaultTableModel(wlCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        waitlistTable = createStyledTable(waitlistTableModel);
        regTabs.addTab("Smart Waitlist Queue", new JScrollPane(waitlistTable));

        lblRosterStatus = new JLabel("Loading registrations & waitlists...");
        lblRosterStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRosterStatus.setForeground(TEXT_MUTED);
        lblRosterStatus.setBorder(BorderFactory.createEmptyBorder(6, 4, 4, 4));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.add(filterBar, BorderLayout.NORTH);
        centerPanel.add(regTabs, BorderLayout.CENTER);
        centerPanel.add(lblRosterStatus, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);
        return panel;
    }

    // --- LOGIC METHODS ---
    private void refreshAllData() {
        cachedEvents = eventDAO.getAllEvents();
        refreshEventsTable();
        refreshInsightsDropdown();
        calculateOverallKPIs();
        calculateAudienceInsights();
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
        if (cmbInsightEvent != null) {
            cmbInsightEvent.removeAllItems();
            for (int i = 0; i < cachedEvents.size(); i++) {
                Event ev = cachedEvents.get(i);
                cmbInsightEvent.addItem(ev.getEventId() + ": " + ev.getTitle());
            }
        }
        if (cmbRegFilterEvent != null) {
            cmbRegFilterEvent.removeAllItems();
            cmbRegFilterEvent.addItem("All Events");
            for (int i = 0; i < cachedEvents.size(); i++) {
                Event ev = cachedEvents.get(i);
                cmbRegFilterEvent.addItem(ev.getEventId() + ": " + ev.getTitle());
            }
        }
    }

    private void calculateOverallKPIs() {
        if (lblKpiTotalEvents == null || cachedEvents == null) return;
        lblKpiTotalEvents.setText(String.valueOf(cachedEvents.size()));

        ArrayList<Registration> allRegs = registrationDAO.getAllRegistrations();
        lblKpiTotalRegs.setText(String.valueOf(allRegs.size()));

        ArrayList<WaitlistEntry> allWl = registrationDAO.getAllWaitlistEntries();
        int activeWl = 0;
        for (int i = 0; i < allWl.size(); i++) {
            if ("WAITING".equalsIgnoreCase(allWl.get(i).getStatus())) activeWl++;
        }
        lblKpiWaitlist.setText(String.valueOf(activeWl));

        double revenue = 0.0;
        double feeSum = 0.0;
        int freeCount = 0;
        int paidCount = 0;
        double totalUtil = 0.0;
        java.util.HashMap<String, Integer> catCounts = new java.util.HashMap<String, Integer>();

        if (insightsTableModel != null) {
            insightsTableModel.setRowCount(0);
        }

        for (int i = 0; i < cachedEvents.size(); i++) {
            Event ev = cachedEvents.get(i);
            feeSum += ev.getFee();
            if (ev.getFee() == 0.0) freeCount++;
            else paidCount++;

            int regCount = registrationDAO.getRegistrationCountForEvent(ev.getEventId());
            int wlCount = registrationDAO.getWaitlistCountForEvent(ev.getEventId());
            revenue += (regCount * ev.getFee());

            double util = ev.getMaxCapacity() > 0 ? (regCount * 100.0 / ev.getMaxCapacity()) : 0.0;
            totalUtil += util;

            String cat = ev.getCategory();
            catCounts.put(cat, catCounts.getOrDefault(cat, 0) + regCount);

            String status = (regCount >= ev.getMaxCapacity()) ? "FULL (" + wlCount + " waiting)" : "Open (" + (ev.getMaxCapacity() - regCount) + " left)";
            if (insightsTableModel != null) {
                insightsTableModel.addRow(new Object[]{
                    ev.getEventId(),
                    ev.getTitle(),
                    ev.getCategory(),
                    ev.getEventDate() + " (" + ev.getTimeSlot() + ")",
                    ev.getMaxCapacity(),
                    regCount,
                    wlCount,
                    String.format("%.1f%%", util),
                    status
                });
            }
        }

        lblKpiRevenue.setText("Rs. " + String.format("%.0f", revenue));
        lblKpiFreePaid.setText(freeCount + " Free / " + paidCount + " Paid");
        double avgUtil = cachedEvents.isEmpty() ? 0.0 : (totalUtil / cachedEvents.size());
        lblKpiUtilization.setText(String.format("%.1f%%", avgUtil));
        double avgFee = cachedEvents.isEmpty() ? 0.0 : (feeSum / cachedEvents.size());
        lblKpiAvgFee.setText("Rs. " + String.format("%.1f", avgFee));

        String topCat = "None";
        int maxCatCount = 0;
        for (String c : catCounts.keySet()) {
            if (catCounts.get(c) > maxCatCount) {
                maxCatCount = catCounts.get(c);
                topCat = c + " (" + maxCatCount + " regs)";
            }
        }
        lblKpiTopCategory.setText(topCat);
    }

    private void refreshRegistrationsTable() {
        if (regTableModel == null || waitlistTableModel == null) return;
        regTableModel.setRowCount(0);
        waitlistTableModel.setRowCount(0);

        String search = txtRegSearch != null ? txtRegSearch.getText().trim().toLowerCase() : "";
        String selEvent = cmbRegFilterEvent != null ? (String) cmbRegFilterEvent.getSelectedItem() : "All Events";
        int filterEventId = -1;
        if (selEvent != null && selEvent.contains(":")) {
            try {
                filterEventId = Integer.parseInt(selEvent.split(":")[0].trim());
            } catch (Exception ignored) {}
        }

        // 1. Confirmed Registrations
        ArrayList<Registration> allRegs = registrationDAO.getAllRegistrations();
        for (int i = 0; i < allRegs.size(); i++) {
            Registration r = allRegs.get(i);
            if (filterEventId != -1 && r.getEventId() != filterEventId) continue;
            if (!search.isEmpty()) {
                String name = r.getStudentName().toLowerCase();
                String email = r.getStudentEmail().toLowerCase();
                String title = r.getEventTitle().toLowerCase();
                if (!name.contains(search) && !email.contains(search) && !title.contains(search)) {
                    continue;
                }
            }
            regTableModel.addRow(new Object[]{
                r.getRegId(),
                r.getStudentName(),
                r.getStudentEmail(),
                r.getEventId(),
                r.getEventTitle(),
                r.getRegistrationDate(),
                r.getStatus() != null ? r.getStatus() : "CONFIRMED"
            });
        }

        // 2. Smart Waitlist Queue
        ArrayList<WaitlistEntry> allWl = registrationDAO.getAllWaitlistEntries();
        java.util.HashMap<Integer, Integer> eventQueueTracker = new java.util.HashMap<Integer, Integer>();
        for (int i = 0; i < allWl.size(); i++) {
            WaitlistEntry w = allWl.get(i);
            if (filterEventId != -1 && w.getEventId() != filterEventId) continue;
            if (!search.isEmpty()) {
                String name = w.getStudentName().toLowerCase();
                String email = w.getStudentEmail().toLowerCase();
                String title = w.getEventTitle().toLowerCase();
                if (!name.contains(search) && !email.contains(search) && !title.contains(search)) {
                    continue;
                }
            }

            int queuePos = eventQueueTracker.getOrDefault(w.getEventId(), 0) + 1;
            if ("WAITING".equalsIgnoreCase(w.getStatus())) {
                eventQueueTracker.put(w.getEventId(), queuePos);
            }

            String posStr = "WAITING".equalsIgnoreCase(w.getStatus()) ? "#" + queuePos : "-";
            waitlistTableModel.addRow(new Object[]{
                w.getWaitlistId(),
                posStr,
                w.getStudentName(),
                w.getStudentEmail(),
                w.getEventTitle(),
                w.getAddedAt(),
                w.getStatus()
            });
        }

        if (lblRosterStatus != null) {
            int confCount = regTableModel.getRowCount();
            int wlCount = waitlistTableModel.getRowCount();
            if (confCount == 0 && wlCount == 0) {
                lblRosterStatus.setText("No confirmed attendees or waitlisted students found matching your criteria.");
            } else {
                lblRosterStatus.setText("Confirmed Attendees: " + confCount + "  |  Active Waitlist Queue: " + wlCount + " students.");
            }
        }
    }

    /**
     * Validates event form inputs before adding or updating.
     * @return validation error message, or null if valid.
     */
    public static String validateEventInputs(String title, String category, String date,
                                            String startTime, String endTime,
                                            double fee, int capacity, String venue) {
        if (title == null || title.trim().isEmpty()) {
            return "Event title is required.";
        }
        if (venue == null || venue.trim().isEmpty()) {
            return "Event venue is required.";
        }
        if (date == null || !date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return "Date must follow YYYY-MM-DD format (e.g., 2026-11-15).";
        }
        if (fee < 0) {
            return "Registration fee cannot be negative.";
        }
        if (capacity <= 0) {
            return "Event capacity must be greater than zero.";
        }
        // Time format validation (HH:mm 24-hr)
        if (startTime == null || !startTime.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            return "Start time must be in HH:mm 24-hour format (e.g., 10:00).";
        }
        if (endTime == null || !endTime.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            return "End time must be in HH:mm 24-hour format (e.g., 12:00).";
        }

        // Chronological order validation
        String[] sParts = startTime.split(":");
        String[] eParts = endTime.split(":");
        int sMins = Integer.parseInt(sParts[0]) * 60 + Integer.parseInt(sParts[1]);
        int eMins = Integer.parseInt(eParts[0]) * 60 + Integer.parseInt(eParts[1]);

        if (sMins >= eMins) {
            return "Event start time (" + startTime + ") must be earlier than end time (" + endTime + ").";
        }

        return null; // All valid
    }

    private void handleAddEvent() {
        try {
            String title = txtTitle.getText().trim();
            String category = (String) cmbCategory.getSelectedItem();
            String date = txtDate.getText().trim();
            String slot = (String) cmbSlot.getSelectedItem();
            double fee = Double.parseDouble(txtFee.getText().trim());
            String venue = txtVenue.getText().trim();
            String skills = txtSkills.getText().trim();
            String dept = txtDept.getText().trim();
            String format = (String) cmbFormat.getSelectedItem();
            String level = (String) cmbLevel.getSelectedItem();
            String start = txtStartTime.getText().trim();
            String end = txtEndTime.getText().trim();
            int capacity = Integer.parseInt(txtCapacity.getText().trim());

            String validationError = validateEventInputs(title, category, date, start, end, fee, capacity, venue);
            if (validationError != null) {
                JOptionPane.showMessageDialog(this, validationError, "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Event ev = new Event(
                0, title, category, date, slot, fee, venue, skills, dept, 0,
                format, level, start, end, capacity
            );
            if (eventDAO.addEvent(ev)) {
                JOptionPane.showMessageDialog(this, "Event successfully added!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save event to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Fee must be a valid number and Capacity must be an integer.", "Input Error", JOptionPane.WARNING_MESSAGE);
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
            String title = txtTitle.getText().trim();
            String category = (String) cmbCategory.getSelectedItem();
            String date = txtDate.getText().trim();
            String slot = (String) cmbSlot.getSelectedItem();
            double fee = Double.parseDouble(txtFee.getText().trim());
            String venue = txtVenue.getText().trim();
            String skills = txtSkills.getText().trim();
            String dept = txtDept.getText().trim();
            String format = (String) cmbFormat.getSelectedItem();
            String level = (String) cmbLevel.getSelectedItem();
            String start = txtStartTime.getText().trim();
            String end = txtEndTime.getText().trim();
            int capacity = Integer.parseInt(txtCapacity.getText().trim());

            String validationError = validateEventInputs(title, category, date, start, end, fee, capacity, venue);
            if (validationError != null) {
                JOptionPane.showMessageDialog(this, validationError, "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Event ev = new Event(
                selectedEventId, title, category, date, slot, fee, venue, skills, dept, 0,
                format, level, start, end, capacity
            );
            if (eventDAO.updateEvent(ev)) {
                JOptionPane.showMessageDialog(this, "Event successfully updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                refreshAllData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update event in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Fee must be a valid number and Capacity must be an integer.", "Input Error", JOptionPane.WARNING_MESSAGE);
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

    private JPanel createFormGroupCard(String title, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
            )
        ));
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(TEXT_NAVY);
        card.add(lblTitle, BorderLayout.NORTH);
        return card;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel, String subtitle, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
            )
        ));

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTitle.setForeground(TEXT_MUTED);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(TEXT_NAVY);

        JLabel lblSub = new JLabel(subtitle);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(TEXT_MUTED);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);

        return card;
    }

    private JTable createStyledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(226, 232, 240));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(new Color(30, 58, 138));

        JTableHeader th = table.getTableHeader();
        th.setPreferredSize(new Dimension(0, 34));
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setBackground(new Color(241, 245, 249));
        th.setForeground(new Color(15, 23, 42));
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(203, 213, 225)));

        DefaultTableCellRenderer center = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return c;
            }
        };
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
