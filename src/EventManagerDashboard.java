import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.util.ArrayList;

/**
 * EventManagerDashboard.java - Modern SaaS Desktop Redesign
 * Professional, clean SaaS-style desktop UI for Event Coordinators.
 * Features:
 * 1. Event creation (Add, Update, Delete) with input checks.
 * 2. Active events catalog with attendee counts.
 * 3. Attendee inspection tab with per-event filtering.
 * 4. Refined SaaS styling: white cards, slate borders, dark navy text, and single blue accent.
 */
public class EventManagerDashboard extends JFrame {

    // Authenticated Coordinator Session
    private User currentUser;

    private EventDAO eventDAO;
    private RegistrationDAO registrationDAO;
    private EventManager fallbackEventManager;
    private ArrayList<Registration> fallbackRegistrations;
    private boolean isDatabaseConnected = false;

    private Integer selectedEventId = null;

    // Form inputs
    private JTextField txtTitle;
    private JComboBox<String> cmbCategory;
    private JTextField txtDate;
    private JComboBox<String> cmbSlot;
    private JTextField txtFee;
    private JTextField txtVenue;
    private JTextField txtSkills;
    private JTextField txtEligibleDept;
    private JComboBox<Integer> cmbEligibleYear;

    // Buttons
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnLogout;

    // Tables
    private DefaultTableModel eventsTableModel;
    private JTable eventsTable;

    private DefaultTableModel regTableModel;
    private JTable regTable;
    private JLabel lblRegSummary;
    private JComboBox<String> cmbFilterEvent;

    private JLabel lblStatus;
    private JLabel lblDbStatus;

    // SaaS Color Palette Constants
    private static final Color BG_CANVAS = new Color(248, 250, 252);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER_CARD = new Color(226, 232, 240);
    private static final Color TEXT_NAVY = new Color(30, 41, 59);
    private static final Color TEXT_MUTED = new Color(100, 116, 139);
    private static final Color ACCENT_BLUE = new Color(37, 99, 235);
    private static final Color ACCENT_GREEN = new Color(22, 163, 74);
    private static final Color ACCENT_RED = new Color(220, 38, 38);

    public EventManagerDashboard(EventDAO eDao, RegistrationDAO rDao, User user) {
        this.eventDAO = eDao;
        this.registrationDAO = rDao;
        this.currentUser = (user != null) ? user : new User(2, "coordinator", "", "COORDINATOR", "Dr. Sarah Jenkins", "sarah.jenkins@campus.edu");
        this.fallbackRegistrations = new ArrayList<Registration>();

        checkDatabaseConnection();

        setTitle("Campus Event Recommendation System - Coordinator Portal (" + currentUser.getFullName() + ")");
        setSize(1080, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BG_CANVAS);

        initComponents();
        refreshEventsTable();
        refreshRegistrationFilterDropdown();
    }

    public EventManagerDashboard(EventDAO eDao, RegistrationDAO rDao) {
        this(eDao, rDao, null);
    }

    public EventManagerDashboard(EventManager manager, ArrayList<Registration> regList) {
        this(new EventDAO(), new RegistrationDAO(), null);
        this.fallbackEventManager = manager;
        this.fallbackRegistrations = (regList != null) ? regList : new ArrayList<Registration>();
    }

    private void checkDatabaseConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            isDatabaseConnected = true;
            eventDAO.createTable();
            registrationDAO.createTable();

            if (eventDAO.getAllEvents().isEmpty()) {
                EventManager seed = new EventManager();
                seed.loadSampleEvents();
                for (Event ev : seed.getAllEvents()) {
                    eventDAO.addEvent(ev);
                }
            }
        } catch (Exception e) {
            isDatabaseConnected = false;
            if (fallbackEventManager == null) {
                fallbackEventManager = new EventManager();
                fallbackEventManager.loadSampleEvents();
            }
        }
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 0));

        // 1. SaaS Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42)); // Deep Slate Navy
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JPanel brandPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        brandPanel.setOpaque(false);
        JLabel lblApp = new JLabel("Campus Event Recommendation System");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblApp.setForeground(Color.WHITE);
        brandPanel.add(lblApp);

        JLabel lblSub = new JLabel("Event Coordinator Portal  •  " + currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(148, 163, 184));
        brandPanel.add(lblSub);
        headerPanel.add(brandPanel, BorderLayout.WEST);

        // Right Nav: Connection indicator & Logout button
        JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        rightNav.setOpaque(false);

        lblDbStatus = new JLabel(isDatabaseConnected ? "● MySQL Online" : "● Standalone Mode");
        lblDbStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDbStatus.setForeground(isDatabaseConnected ? new Color(74, 222, 128) : new Color(251, 146, 60));
        rightNav.add(lblDbStatus);

        btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.setBackground(ACCENT_RED);
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { performLogout(); }
        });
        rightNav.add(btnLogout);

        headerPanel.add(rightNav, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Main Content Tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.setBackground(BG_CANVAS);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        tabbedPane.addTab("Manage Campus Events (CRUD)", buildEventCrudTab());
        tabbedPane.addTab("View Registrations & Attendee Counts", buildRegistrationsTab());
        add(tabbedPane, BorderLayout.CENTER);

        // 3. Status Bar
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        statusBar.setBackground(Color.WHITE);
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_CARD));
        lblStatus = new JLabel("Welcome, " + currentUser.getFullName() + ". Click any event in the table to edit, or fill the form to create a new one.");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(TEXT_MUTED);
        statusBar.add(lblStatus);
        add(statusBar, BorderLayout.SOUTH);
    }

    private JPanel buildEventCrudTab() {
        JPanel container = new JPanel(new BorderLayout(14, 14));
        container.setBackground(BG_CANVAS);
        container.setBorder(BorderFactory.createEmptyBorder(12, 4, 12, 4));

        // Top Card: Event Input Form
        JPanel formCard = createCardPanel();
        formCard.setLayout(new BorderLayout(10, 10));

        JLabel lblFormTitle = new JLabel("Event Details Form");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFormTitle.setForeground(TEXT_NAVY);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(5, 4, 10, 8));
        grid.setOpaque(false);

        grid.add(createFormLabel("Event Title:"));
        txtTitle = createTextField("");
        grid.add(txtTitle);

        grid.add(createFormLabel("Category:"));
        cmbCategory = new JComboBox<String>(new String[]{"Coding", "AI", "Robotics", "Sports", "Arts", "Entrepreneurship"});
        cmbCategory.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        grid.add(cmbCategory);

        grid.add(createFormLabel("Date (YYYY-MM-DD):"));
        txtDate = createTextField("2026-11-25");
        grid.add(txtDate);

        grid.add(createFormLabel("Time Slot:"));
        cmbSlot = new JComboBox<String>(new String[]{"Morning", "Afternoon", "Evening"});
        cmbSlot.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        grid.add(cmbSlot);

        grid.add(createFormLabel("Fee (Rs., 0 = Free):"));
        txtFee = createTextField("100.0");
        grid.add(txtFee);

        grid.add(createFormLabel("Venue:"));
        txtVenue = createTextField("Main Seminar Hall");
        grid.add(txtVenue);

        grid.add(createFormLabel("Required Skills:"));
        txtSkills = createTextField("Java, Communication");
        grid.add(txtSkills);

        grid.add(createFormLabel("Eligible Department:"));
        txtEligibleDept = createTextField("All");
        grid.add(txtEligibleDept);

        grid.add(createFormLabel("Eligible Year (0 = All):"));
        cmbEligibleYear = new JComboBox<Integer>(new Integer[]{0, 1, 2, 3, 4});
        cmbEligibleYear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        grid.add(cmbEligibleYear);

        // Action Buttons Row
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        btnAdd = new JButton("Add Event");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setBackground(ACCENT_GREEN);
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onAddEvent(); }
        });

        btnUpdate = new JButton("Update Event");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUpdate.setBackground(ACCENT_BLUE);
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFocusPainted(false);
        btnUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnUpdate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onUpdateEvent(); }
        });

        btnDelete = new JButton("Delete Event");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setBackground(ACCENT_RED);
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFocusPainted(false);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { onDeleteEvent(); }
        });

        btnClear = new JButton("Clear");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClear.setBackground(Color.WHITE);
        btnClear.setForeground(TEXT_NAVY);
        btnClear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { clearForm(); }
        });

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        grid.add(new JLabel(""));
        grid.add(btnPanel);

        formCard.add(grid, BorderLayout.CENTER);
        container.add(formCard, BorderLayout.NORTH);

        // Center Card: Events Catalog Table
        JPanel tableCard = createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JLabel lblTableTitle = new JLabel("Active Events Catalog (Click any row to select & edit)");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTableTitle.setForeground(TEXT_NAVY);
        tableCard.add(lblTableTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "Title", "Category", "Date", "Slot", "Fee (Rs.)", "Venue", "Required Skills", "Dept", "Year", "Registrations"};
        eventsTableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        eventsTable = new JTable(eventsTableModel);
        styleTable(eventsTable);
        eventsTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        eventsTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        eventsTable.getColumnModel().getColumn(10).setPreferredWidth(95);

        eventsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) { onRowSelect(); }
            }
        });

        tableCard.add(new JScrollPane(eventsTable), BorderLayout.CENTER);
        container.add(tableCard, BorderLayout.CENTER);

        return container;
    }

    private JPanel buildRegistrationsTab() {
        JPanel container = new JPanel(new BorderLayout(12, 12));
        container.setBackground(BG_CANVAS);
        container.setBorder(BorderFactory.createEmptyBorder(12, 4, 12, 4));

        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        // Filter Bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        filterPanel.setOpaque(false);

        JLabel lblFilter = new JLabel("Filter Registrations by Event:");
        lblFilter.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblFilter.setForeground(TEXT_NAVY);
        filterPanel.add(lblFilter);

        cmbFilterEvent = new JComboBox<String>();
        cmbFilterEvent.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cmbFilterEvent.setPreferredSize(new Dimension(320, 28));
        cmbFilterEvent.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { displayRegistrationsForSelectedEvent(); }
        });
        filterPanel.add(cmbFilterEvent);

        lblRegSummary = new JLabel("Total Registrations: 0");
        lblRegSummary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblRegSummary.setForeground(ACCENT_BLUE);
        filterPanel.add(lblRegSummary);

        card.add(filterPanel, BorderLayout.NORTH);

        String[] regCols = {"Reg ID", "Student Name", "Student Email", "Event ID", "Event Title", "Registered Timestamp"};
        regTableModel = new DefaultTableModel(regCols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        regTable = new JTable(regTableModel);
        styleTable(regTable);
        card.add(new JScrollPane(regTable), BorderLayout.CENTER);

        container.add(card, BorderLayout.CENTER);
        return container;
    }

    private void onRowSelect() {
        int row = eventsTable.getSelectedRow();
        if (row == -1) return;

        selectedEventId = (Integer) eventsTableModel.getValueAt(row, 0);
        txtTitle.setText((String) eventsTableModel.getValueAt(row, 1));
        cmbCategory.setSelectedItem((String) eventsTableModel.getValueAt(row, 2));
        txtDate.setText((String) eventsTableModel.getValueAt(row, 3));
        cmbSlot.setSelectedItem((String) eventsTableModel.getValueAt(row, 4));
        txtFee.setText(String.valueOf(eventsTableModel.getValueAt(row, 5)));
        txtVenue.setText((String) eventsTableModel.getValueAt(row, 6));
        txtSkills.setText((String) eventsTableModel.getValueAt(row, 7));
        txtEligibleDept.setText((String) eventsTableModel.getValueAt(row, 8));

        Object yrObj = eventsTableModel.getValueAt(row, 9);
        if (yrObj instanceof Integer) {
            cmbEligibleYear.setSelectedItem(yrObj);
        } else {
            String yrStr = yrObj.toString();
            if (yrStr.equalsIgnoreCase("All")) {
                cmbEligibleYear.setSelectedItem(0);
            } else {
                try {
                    cmbEligibleYear.setSelectedItem(Integer.parseInt(yrStr.replace("Year ", "").trim()));
                } catch (Exception ex) {
                    cmbEligibleYear.setSelectedItem(0);
                }
            }
        }

        lblStatus.setText("Selected Event ID #" + selectedEventId + ": \"" + txtTitle.getText() + "\" (Ready to Update or Delete)");
    }

    private void onAddEvent() {
        String title = txtTitle.getText().trim();
        String category = (String) cmbCategory.getSelectedItem();
        String date = txtDate.getText().trim();
        String slot = (String) cmbSlot.getSelectedItem();
        String feeStr = txtFee.getText().trim();
        String venue = txtVenue.getText().trim();
        String skills = txtSkills.getText().trim();
        String dept = txtEligibleDept.getText().trim();
        int year = (Integer) cmbEligibleYear.getSelectedItem();

        if (title.isEmpty() || date.isEmpty() || venue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Event Title, Date, and Venue are required.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double fee = 0.0;
        try {
            fee = Double.parseDouble(feeStr);
            if (fee < 0) {
                JOptionPane.showMessageDialog(this, "Fee cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric fee.", "Invalid Fee", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Event newEvent = new Event(0, title, category, date, slot, fee, venue, skills, dept, year);

        if (isDatabaseConnected) {
            eventDAO.addEvent(newEvent);
        } else {
            int newId = fallbackEventManager.getEventCount() + 1;
            newEvent.setEventId(newId);
            fallbackEventManager.addEvent(newEvent);
        }

        JOptionPane.showMessageDialog(this, "Event Added Successfully!\nEvent ID: #" + newEvent.getEventId() + "\nTitle: " + title, "Success", JOptionPane.INFORMATION_MESSAGE);

        refreshEventsTable();
        refreshRegistrationFilterDropdown();
        clearForm();
        lblStatus.setText("Added new event: \"" + title + "\"");
    }

    private void onUpdateEvent() {
        if (selectedEventId == null) {
            JOptionPane.showMessageDialog(this, "Please select an event row from the table first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String title = txtTitle.getText().trim();
        String category = (String) cmbCategory.getSelectedItem();
        String date = txtDate.getText().trim();
        String slot = (String) cmbSlot.getSelectedItem();
        String feeStr = txtFee.getText().trim();
        String venue = txtVenue.getText().trim();
        String skills = txtSkills.getText().trim();
        String dept = txtEligibleDept.getText().trim();
        int year = (Integer) cmbEligibleYear.getSelectedItem();

        if (title.isEmpty() || date.isEmpty() || venue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Event Title, Date, and Venue are required.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double fee = 0.0;
        try {
            fee = Double.parseDouble(feeStr);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric fee.", "Invalid Fee", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Event updated = new Event(selectedEventId, title, category, date, slot, fee, venue, skills, dept, year);

        if (isDatabaseConnected) {
            eventDAO.updateEvent(updated);
        } else {
            Event existing = fallbackEventManager.findEventById(selectedEventId);
            if (existing != null) {
                existing.setTitle(title);
                existing.setCategory(category);
                existing.setEventDate(date);
                existing.setTimeSlot(slot);
                existing.setFee(fee);
                existing.setVenue(venue);
                existing.setRequiredSkills(skills);
                existing.setEligibleDept(dept);
                existing.setEligibleYear(year);
            }
        }

        JOptionPane.showMessageDialog(this, "Event #" + selectedEventId + " updated successfully!", "Updated", JOptionPane.INFORMATION_MESSAGE);

        refreshEventsTable();
        refreshRegistrationFilterDropdown();
        clearForm();
        lblStatus.setText("Updated event #" + selectedEventId);
    }

    private void onDeleteEvent() {
        if (selectedEventId == null) {
            JOptionPane.showMessageDialog(this, "Please select an event row to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete event #" + selectedEventId + ": \"" + txtTitle.getText() + "\"?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (isDatabaseConnected) {
                eventDAO.deleteEvent(selectedEventId);
            } else {
                ArrayList<Event> list = fallbackEventManager.getAllEvents();
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getEventId() == selectedEventId) {
                        list.remove(i);
                        break;
                    }
                }
            }

            JOptionPane.showMessageDialog(this, "Event #" + selectedEventId + " deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);

            refreshEventsTable();
            refreshRegistrationFilterDropdown();
            clearForm();
            lblStatus.setText("Deleted event #" + selectedEventId);
        }
    }

    private void clearForm() {
        selectedEventId = null;
        txtTitle.setText("");
        cmbCategory.setSelectedIndex(0);
        txtDate.setText("2026-11-25");
        cmbSlot.setSelectedIndex(0);
        txtFee.setText("0.0");
        txtVenue.setText("");
        txtSkills.setText("");
        txtEligibleDept.setText("All");
        cmbEligibleYear.setSelectedIndex(0);
        eventsTable.clearSelection();
        lblStatus.setText("Form cleared. Ready for new input.");
    }

    public void refreshEventsTable() {
        eventsTableModel.setRowCount(0);
        ArrayList<Event> list = isDatabaseConnected ? eventDAO.getAllEvents() : fallbackEventManager.getAllEvents();

        for (int i = 0; i < list.size(); i++) {
            Event e = list.get(i);
            int regCount = isDatabaseConnected ?
                registrationDAO.getRegistrationCountForEvent(e.getEventId()) : countFallbackRegistrations(e.getEventId());

            eventsTableModel.addRow(new Object[]{
                e.getEventId(),
                e.getTitle(),
                e.getCategory(),
                e.getEventDate(),
                e.getTimeSlot(),
                e.getFee(),
                e.getVenue(),
                e.getRequiredSkills(),
                e.getEligibleDept(),
                e.getEligibleYear() == 0 ? "All" : "Year " + e.getEligibleYear(),
                regCount + " student(s)"
            });
        }
    }

    private int countFallbackRegistrations(int eventId) {
        int c = 0;
        for (int i = 0; i < fallbackRegistrations.size(); i++) {
            if (fallbackRegistrations.get(i).getEventId() == eventId) c++;
        }
        return c;
    }

    private void refreshRegistrationFilterDropdown() {
        cmbFilterEvent.removeAllItems();
        cmbFilterEvent.addItem("--- All Events ---");
        ArrayList<Event> list = isDatabaseConnected ? eventDAO.getAllEvents() : fallbackEventManager.getAllEvents();
        for (int i = 0; i < list.size(); i++) {
            Event e = list.get(i);
            cmbFilterEvent.addItem(e.getEventId() + ": " + e.getTitle());
        }
    }

    private void displayRegistrationsForSelectedEvent() {
        regTableModel.setRowCount(0);
        String selectedItem = (String) cmbFilterEvent.getSelectedItem();
        if (selectedItem == null) return;

        int targetEventId = -1;
        if (!selectedItem.equals("--- All Events ---")) {
            try {
                String idPart = selectedItem.split(":")[0].trim();
                targetEventId = Integer.parseInt(idPart);
            } catch (Exception ex) {
                targetEventId = -1;
            }
        }

        ArrayList<Registration> list = isDatabaseConnected ?
            registrationDAO.getAllRegistrations() : fallbackRegistrations;

        int count = 0;
        for (int i = 0; i < list.size(); i++) {
            Registration r = list.get(i);
            if (targetEventId == -1 || r.getEventId() == targetEventId) {
                regTableModel.addRow(new Object[]{
                    r.getRegId(),
                    r.getStudentName(),
                    r.getStudentEmail(),
                    r.getEventId(),
                    r.getEventTitle(),
                    r.getRegistrationDate()
                });
                count++;
            }
        }
        lblRegSummary.setText("Total Registrations: " + count + " student(s)");
    }

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to log out of the Coordinator Portal?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            new Main().setVisible(true);
        }
    }

    // --- SaaS UI Helper Methods ---
    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CARD, 1),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        return card;
    }

    private JLabel createFormLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(TEXT_NAVY);
        return lbl;
    }

    private JTextField createTextField(String text) {
        JTextField tf = new JTextField(text, 15);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return tf;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(26);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setGridColor(BORDER_CARD);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT_NAVY);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(TEXT_NAVY);
        header.setPreferredSize(new Dimension(0, 30));
    }
}
