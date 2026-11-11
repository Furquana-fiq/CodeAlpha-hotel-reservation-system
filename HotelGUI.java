import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class HotelGUI extends JFrame {

    private final HotelManager manager;
    private JTabbedPane tabs;

    // Tab 1: Search Rooms
    private JComboBox<String> categoryFilterBox;
    private JCheckBox availableOnlyCheck;
    private DefaultTableModel roomsTableModel;
    private JTable roomsTable;

    // Tab 2: Make Reservation & Payment Simulation
    private JComboBox<Room> bookingRoomCombo;
    private JLabel bookingRoomInfoLabel;
    private JTextField guestNameField;
    private JTextField guestContactField;
    private JTextField checkInDateField;
    private JSpinner nightsSpinner;
    private JLabel bookingSummaryLabel;

    private JComboBox<String> paymentMethodBox;
    private JLabel paymentRefLabel;
    private JTextField paymentRefField;
    private JButton fillDemoPaymentBtn;
    private JLabel paymentHintLabel;
    private JComboBox<String> simulationOutcomeBox;
    private JButton dryRunPayBtn;
    private JButton confirmBookingBtn;
    private JTextArea simulationOutputArea;
    private boolean isRefreshingBookingRooms = false;

    // Tab 3: Reservations & Details
    private JComboBox<String> reservationStatusFilterBox;
    private JTextField reservationSearchField;
    private DefaultTableModel reservationsTableModel;
    private JTable reservationsTable;

    // Bottom Status Bar
    private JLabel availableLabel;
    private JLabel bookedLabel;
    private JLabel activeResLabel;
    private JLabel revenueLabel;

    public HotelGUI(HotelManager manager) {
        this.manager = manager;
        setupWindow();
        refreshAll();
    }

    private void setupWindow() {
        setTitle("Hotel Reservation System");
        setSize(1040, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(new EmptyBorder(14, 14, 14, 14));
        setContentPane(root);

        root.add(createHeaderPanel(), BorderLayout.NORTH);
        root.add(createTabs(), BorderLayout.CENTER);
        root.add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(44, 62, 80));
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("Hotel Reservation System");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Search rooms, make reservations, simulate payments, and manage bookings");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(220, 230, 240));

        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 2));
        textGroup.setOpaque(false);
        textGroup.add(title);
        textGroup.add(subtitle);

        header.add(textGroup, BorderLayout.WEST);
        return header;
    }

    private JTabbedPane createTabs() {
        tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("Search Rooms", createRoomsTab());
        tabs.addTab("Make Reservation & Payment", createBookingAndPaymentTab());
        tabs.addTab("Reservations & Details", createReservationsTab());
        return tabs;
    }

    private JPanel createRoomsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.add(new JLabel("Category:"));

        categoryFilterBox = new JComboBox<>(new String[]{"All", "Standard", "Deluxe", "Suite"});
        categoryFilterBox.addActionListener(e -> refreshRoomsTable());
        filterBar.add(categoryFilterBox);

        availableOnlyCheck = new JCheckBox("Available Only");
        availableOnlyCheck.addActionListener(e -> refreshRoomsTable());
        filterBar.add(availableOnlyCheck);

        JButton addRoomBtn = new JButton("Add New Room");
        addRoomBtn.addActionListener(e -> openAddRoomDialog());
        filterBar.add(addRoomBtn);

        panel.add(filterBar, BorderLayout.NORTH);

        String[] columns = {"Room #", "Category", "Price / Night", "Amenities", "Status"};
        roomsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomsTable = new JTable(roomsTableModel);
        roomsTable.setRowHeight(26);
        roomsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        roomsTable.getColumnModel().getColumn(0).setPreferredWidth(75);
        roomsTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        roomsTable.getColumnModel().getColumn(2).setPreferredWidth(105);
        roomsTable.getColumnModel().getColumn(3).setPreferredWidth(410);
        roomsTable.getColumnModel().getColumn(4).setPreferredWidth(95);

        roomsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && roomsTable.getSelectedRow() >= 0) {
                    goToMakeReservationForSelectedRoom();
                }
            }
        });

        panel.add(new JScrollPane(roomsTable), BorderLayout.CENTER);

        JPanel bottomActions = new JPanel(new BorderLayout(10, 5));
        JLabel tipLabel = new JLabel("Tip: Select an available room and click 'Make Reservation & Simulate Payment' (or double-click a row).");
        tipLabel.setForeground(new Color(90, 95, 105));
        tipLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bottomActions.add(tipLabel, BorderLayout.WEST);

        JButton bookBtn = new JButton("Make Reservation & Simulate Payment");
        bookBtn.setFont(bookBtn.getFont().deriveFont(Font.BOLD));
        bookBtn.addActionListener(e -> goToMakeReservationForSelectedRoom());
        bottomActions.add(bookBtn, BorderLayout.EAST);

        panel.add(bottomActions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBookingAndPaymentTab() {
        JPanel tabPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        tabPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Left Column: 1. Make Reservation + 2. Payment Simulation
        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));

        // --- Section 1: Reservation Details ---
        JPanel reservationPanel = new JPanel(new GridBagLayout());
        reservationPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("1. Make Reservation"),
                new EmptyBorder(8, 10, 10, 10)
        ));

        bookingRoomCombo = new JComboBox<>();
        bookingRoomCombo.addActionListener(e -> {
            if (!isRefreshingBookingRooms) {
                updateBookingSummary();
            }
        });

        bookingRoomInfoLabel = new JLabel(" ");
        bookingRoomInfoLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        bookingRoomInfoLabel.setForeground(new Color(75, 85, 95));

        guestNameField = new JTextField(18);
        guestContactField = new JTextField(18);
        checkInDateField = new JTextField(LocalDate.now().toString(), 18);
        nightsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));

        bookingSummaryLabel = new JLabel("Total: $0.00");
        bookingSummaryLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        bookingSummaryLabel.setForeground(new Color(28, 78, 128));

        nightsSpinner.addChangeListener(e -> updateBookingSummary());
        checkInDateField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateBookingSummary();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateBookingSummary();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateBookingSummary();
            }
        });

        int row = 0;
        addFormRow(reservationPanel, row++, "Available Room:", bookingRoomCombo);
        addFormRow(reservationPanel, row++, "Amenities:", bookingRoomInfoLabel);
        addFormRow(reservationPanel, row++, "Guest Full Name:", guestNameField);
        addFormRow(reservationPanel, row++, "Phone / Email:", guestContactField);
        addFormRow(reservationPanel, row++, "Check-In (YYYY-MM-DD):", checkInDateField);
        addFormRow(reservationPanel, row++, "Number of Nights:", nightsSpinner);
        addFormRow(reservationPanel, row, "Stay & Total Cost:", bookingSummaryLabel);

        // --- Section 2: Payment Simulation ---
        JPanel paymentPanel = new JPanel(new GridBagLayout());
        paymentPanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("2. Payment Simulation"),
                new EmptyBorder(8, 10, 10, 10)
        ));

        paymentMethodBox = new JComboBox<>(PaymentSimulator.getPaymentMethods());
        paymentMethodBox.addActionListener(e -> updatePaymentMethodUI());

        paymentRefLabel = new JLabel("Card Number (12-19 digits):");
        paymentRefField = new JTextField(14);
        fillDemoPaymentBtn = new JButton("Fill Test");
        fillDemoPaymentBtn.setToolTipText("Auto-fill valid sample card or UPI ID for testing");
        fillDemoPaymentBtn.addActionListener(e -> fillDemoPaymentReference());

        JPanel refInputRow = new JPanel(new BorderLayout(6, 0));
        refInputRow.add(paymentRefField, BorderLayout.CENTER);
        refInputRow.add(fillDemoPaymentBtn, BorderLayout.EAST);

        paymentHintLabel = new JLabel(" ");
        paymentHintLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        paymentHintLabel.setForeground(new Color(75, 85, 95));

        simulationOutcomeBox = new JComboBox<>(new String[]{
                "Approve Payment (Simulate Success)",
                "Decline Payment (Simulate Failure)"
        });

        int pRow = 0;
        addFormRow(paymentPanel, pRow++, "Payment Method:", paymentMethodBox);
        addFormRow(paymentPanel, pRow++, paymentRefLabel, refInputRow);
        addFormRow(paymentPanel, pRow++, "Format Hint:", paymentHintLabel);
        addFormRow(paymentPanel, pRow++, "Simulation Outcome:", simulationOutcomeBox);

        JPanel actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        JButton resetBtn = new JButton("Reset");
        dryRunPayBtn = new JButton("Simulate Payment Only");
        confirmBookingBtn = new JButton("Simulate Payment & Confirm Booking");
        confirmBookingBtn.setFont(confirmBookingBtn.getFont().deriveFont(Font.BOLD));

        resetBtn.addActionListener(e -> resetBookingForm());
        dryRunPayBtn.addActionListener(e -> runDryRunPaymentSimulation());
        confirmBookingBtn.addActionListener(e -> submitReservationAndPayment());

        actionButtonsPanel.add(resetBtn);
        actionButtonsPanel.add(dryRunPayBtn);
        actionButtonsPanel.add(confirmBookingBtn);

        GridBagConstraints btnGbc = new GridBagConstraints();
        btnGbc.gridx = 0;
        btnGbc.gridy = pRow;
        btnGbc.gridwidth = 2;
        btnGbc.fill = GridBagConstraints.HORIZONTAL;
        btnGbc.insets = new Insets(10, 2, 2, 2);
        paymentPanel.add(actionButtonsPanel, btnGbc);

        leftColumn.add(reservationPanel);
        leftColumn.add(Box.createVerticalStrut(10));
        leftColumn.add(paymentPanel);

        JScrollPane leftScroll = new JScrollPane(leftColumn);
        leftScroll.setBorder(null);
        leftScroll.getVerticalScrollBar().setUnitIncrement(16);

        // Right Column: Live Payment Simulation Log & Booking Receipt
        JPanel rightColumn = new JPanel(new BorderLayout(8, 8));
        rightColumn.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("3. Payment Simulation Log & Booking Receipt"),
                new EmptyBorder(8, 10, 10, 10)
        ));

        simulationOutputArea = new JTextArea();
        simulationOutputArea.setEditable(false);
        simulationOutputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        simulationOutputArea.setMargin(new Insets(10, 10, 10, 10));
        setInitialSimulationOutput();

        rightColumn.add(new JScrollPane(simulationOutputArea), BorderLayout.CENTER);

        JPanel rightBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        JButton viewReservationsBtn = new JButton("View Reservations & Details");
        viewReservationsBtn.addActionListener(e -> tabs.setSelectedIndex(2));
        rightBottom.add(viewReservationsBtn);
        rightColumn.add(rightBottom, BorderLayout.SOUTH);

        tabPanel.add(leftScroll);
        tabPanel.add(rightColumn);

        updatePaymentMethodUI();
        return tabPanel;
    }

    private void addFormRow(JPanel panel, int row, String labelText, Component field) {
        addFormRow(panel, row, new JLabel(labelText), field);
    }

    private void addFormRow(JPanel panel, int row, JLabel label, Component field) {
        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.anchor = GridBagConstraints.WEST;
        labelGbc.insets = new Insets(5, 4, 5, 10);
        panel.add(label, labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.weightx = 1.0;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.insets = new Insets(5, 4, 5, 4);
        panel.add(field, fieldGbc);
    }

    private JPanel createReservationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterBar.add(new JLabel("Status:"));
        reservationStatusFilterBox = new JComboBox<>(new String[]{"All", "CONFIRMED", "CANCELLED"});
        reservationStatusFilterBox.addActionListener(e -> refreshReservationsTable());
        filterBar.add(reservationStatusFilterBox);

        filterBar.add(new JLabel("Search (ID / Guest):"));
        reservationSearchField = new JTextField(18);
        reservationSearchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshReservationsTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshReservationsTable();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshReservationsTable();
            }
        });
        filterBar.add(reservationSearchField);

        panel.add(filterBar, BorderLayout.NORTH);

        String[] columns = {
                "Reservation ID", "Guest Name", "Room #", "Category",
                "Check-In", "Nights", "Total Paid", "Payment", "Status"
        };
        reservationsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        reservationsTable = new JTable(reservationsTableModel);
        reservationsTable.setRowHeight(26);
        reservationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reservationsTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        reservationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && reservationsTable.getSelectedRow() >= 0) {
                    viewSelectedReservationDetails();
                }
            }
        });

        panel.add(new JScrollPane(reservationsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton newBookingBtn = new JButton("New Reservation");
        JButton viewDetailsBtn = new JButton("View Booking Details");
        JButton cancelResBtn = new JButton("Cancel Reservation (Simulate Refund)");

        newBookingBtn.addActionListener(e -> tabs.setSelectedIndex(1));
        viewDetailsBtn.addActionListener(e -> viewSelectedReservationDetails());
        cancelResBtn.addActionListener(e -> cancelSelectedReservation());

        actions.add(newBookingBtn);
        actions.add(viewDetailsBtn);
        actions.add(cancelResBtn);
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 10, 0));
        bar.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder("Hotel Overview"),
                new EmptyBorder(6, 10, 6, 10)
        ));

        availableLabel = new JLabel("Available Rooms: 0");
        bookedLabel = new JLabel("Booked Rooms: 0");
        activeResLabel = new JLabel("Active Reservations: 0");
        revenueLabel = new JLabel("Total Revenue: $0.00");

        Font font = new Font("SansSerif", Font.BOLD, 13);
        availableLabel.setFont(font);
        bookedLabel.setFont(font);
        activeResLabel.setFont(font);
        revenueLabel.setFont(font);

        bar.add(availableLabel);
        bar.add(bookedLabel);
        bar.add(activeResLabel);
        bar.add(revenueLabel);

        return bar;
    }

    private void goToMakeReservationForSelectedRoom() {
        int row = roomsTable.getSelectedRow();
        if (row < 0) {
            if (bookingRoomCombo.getItemCount() == 0) {
                showError("No available rooms right now. Add a new room or cancel an existing reservation.");
                return;
            }
            tabs.setSelectedIndex(1);
            return;
        }

        int roomNumber = (int) roomsTableModel.getValueAt(row, 0);
        Room room = manager.findRoom(roomNumber);
        if (room == null || !room.isAvailable()) {
            showError("Room " + roomNumber + " is already booked. Please select an available room.");
            return;
        }

        selectRoomInBookingCombo(roomNumber);
        tabs.setSelectedIndex(1);
    }

    private void selectRoomInBookingCombo(int roomNumber) {
        for (int i = 0; i < bookingRoomCombo.getItemCount(); i++) {
            Room item = bookingRoomCombo.getItemAt(i);
            if (item != null && item.getRoomNumber() == roomNumber) {
                bookingRoomCombo.setSelectedIndex(i);
                break;
            }
        }
        updateBookingSummary();
    }

    private void updateBookingSummary() {
        Room selectedRoom = (Room) bookingRoomCombo.getSelectedItem();
        if (selectedRoom == null) {
            bookingRoomInfoLabel.setText("No available rooms left to book.");
            bookingSummaryLabel.setText("Total: $0.00");
            return;
        }

        bookingRoomInfoLabel.setText(selectedRoom.getCategory().getDescription());
        int nights = (int) nightsSpinner.getValue();
        double total = selectedRoom.getPricePerNight() * nights;

        String checkOutText = "";
        String rawDate = checkInDateField.getText().trim();
        try {
            LocalDate checkIn = LocalDate.parse(rawDate);
            LocalDate checkOut = checkIn.plusDays(nights);
            checkOutText = "Check-Out: " + checkOut + "  |  ";
        } catch (DateTimeParseException ignored) {
            checkOutText = "Enter YYYY-MM-DD  |  ";
        }

        bookingSummaryLabel.setText(String.format("%sTotal: $%.2f (%d x $%.2f)",
                checkOutText, total, nights, selectedRoom.getPricePerNight()));
    }

    private void updatePaymentMethodUI() {
        String method = (String) paymentMethodBox.getSelectedItem();
        if ("Credit Card".equals(method) || "Debit Card".equals(method)) {
            paymentRefLabel.setText("Card Number (12-19 digits):");
            paymentRefField.setEnabled(true);
            fillDemoPaymentBtn.setEnabled(true);
            paymentHintLabel.setText("Enter 12-19 digits or click 'Fill Test' for a sample card.");
        } else if ("UPI / Digital Wallet".equals(method)) {
            paymentRefLabel.setText("UPI / Wallet ID:");
            paymentRefField.setEnabled(true);
            fillDemoPaymentBtn.setEnabled(true);
            paymentHintLabel.setText("Enter an ID with '@' (e.g. guest@bank) or click 'Fill Test'.");
        } else {
            paymentRefLabel.setText("Payment Reference:");
            paymentRefField.setText("");
            paymentRefField.setEnabled(false);
            fillDemoPaymentBtn.setEnabled(false);
            paymentHintLabel.setText("Payable in cash at reception upon check-in.");
        }
    }

    private void fillDemoPaymentReference() {
        String method = (String) paymentMethodBox.getSelectedItem();
        if ("Credit Card".equals(method)) {
            paymentRefField.setText("4532-1234-5678-9012");
        } else if ("Debit Card".equals(method)) {
            paymentRefField.setText("5412-7512-3456-7890");
        } else if ("UPI / Digital Wallet".equals(method)) {
            paymentRefField.setText("guest@okbank");
        }
    }

    private boolean isSimulateDeclineSelected() {
        return simulationOutcomeBox.getSelectedIndex() == 1;
    }

    private void runDryRunPaymentSimulation() {
        Room selectedRoom = (Room) bookingRoomCombo.getSelectedItem();
        if (selectedRoom == null) {
            showError("No available room is selected.");
            return;
        }

        int nights = (int) nightsSpinner.getValue();
        double amount = selectedRoom.getPricePerNight() * nights;
        String method = (String) paymentMethodBox.getSelectedItem();
        String ref = paymentRefField.getText();
        boolean simulateDecline = isSimulateDeclineSelected();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder log = new StringBuilder();
        log.append("================================================\n");
        log.append("         PAYMENT SIMULATION (DRY RUN)           \n");
        log.append("================================================\n");
        log.append("Timestamp      : ").append(timestamp).append("\n");
        log.append("Room           : Room ").append(selectedRoom.getRoomNumber())
                .append(" (").append(selectedRoom.getCategory().getDisplayName()).append(")\n");
        log.append("Nights         : ").append(nights).append("\n");
        log.append(String.format("Amount         : $%.2f%n", amount));
        log.append("Method         : ").append(method).append("\n");
        log.append("Reference      : ").append(PaymentSimulator.maskReference(method, ref)).append("\n");
        log.append("------------------------------------------------\n");
        log.append("Contacting simulated payment gateway...\n");

        try {
            String txnId = PaymentSimulator.processPayment(method, ref, amount, simulateDecline);
            log.append("Gateway Status : APPROVED\n");
            log.append("Transaction ID : ").append(txnId).append("\n");
            log.append("------------------------------------------------\n");
            log.append("Note: Dry-run simulation only. Room was NOT booked.\n");
            log.append("Click 'Simulate Payment & Confirm Booking' to finalize.\n");
            log.append("================================================\n");
            simulationOutputArea.setText(log.toString());
            simulationOutputArea.setCaretPosition(0);
        } catch (IllegalArgumentException ex) {
            log.append("Gateway Status : DECLINED / VALIDATION FAILED\n");
            log.append("Reason         : ").append(ex.getMessage()).append("\n");
            log.append("================================================\n");
            simulationOutputArea.setText(log.toString());
            simulationOutputArea.setCaretPosition(0);
            showError(ex.getMessage());
        }
    }

    private void submitReservationAndPayment() {
        Room selectedRoom = (Room) bookingRoomCombo.getSelectedItem();
        if (selectedRoom == null) {
            showError("No available room is selected for booking.");
            return;
        }

        String method = (String) paymentMethodBox.getSelectedItem();
        String ref = paymentRefField.getText();
        int nights = (int) nightsSpinner.getValue();
        double amount = selectedRoom.getPricePerNight() * nights;
        boolean simulateDecline = isSimulateDeclineSelected();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try {
            Reservation reservation = manager.bookRoom(
                    selectedRoom.getRoomNumber(),
                    guestNameField.getText(),
                    guestContactField.getText(),
                    checkInDateField.getText(),
                    nights,
                    method,
                    ref,
                    simulateDecline
            );

            String receipt = "================================================\n"
                    + "         PAYMENT SIMULATION APPROVED            \n"
                    + "================================================\n"
                    + "Timestamp      : " + timestamp + "\n"
                    + "Reference      : " + PaymentSimulator.maskReference(method, ref) + "\n\n"
                    + PaymentSimulator.formatPaymentReceipt(
                            reservation.getTransactionId(),
                            reservation.getPaymentMethod(),
                            reservation.getTotalAmount()
                    )
                    + "\n\n"
                    + reservation.getBookingDetailsText();

            simulationOutputArea.setText(receipt);
            simulationOutputArea.setCaretPosition(0);

            guestNameField.setText("");
            guestContactField.setText("");
            paymentRefField.setText("");

            refreshAll();
            showBookingDetailsDialog(reservation, true);
        } catch (IllegalArgumentException ex) {
            String failureLog = "================================================\n"
                    + "      RESERVATION / PAYMENT FAILED              \n"
                    + "================================================\n"
                    + "Timestamp      : " + timestamp + "\n"
                    + String.format("Attempted Amt  : $%.2f%n", amount)
                    + "Method         : " + method + "\n"
                    + "Error          : " + ex.getMessage() + "\n"
                    + "================================================\n";
            simulationOutputArea.setText(failureLog);
            simulationOutputArea.setCaretPosition(0);
            showError(ex.getMessage());
        }
    }

    private void resetBookingForm() {
        guestNameField.setText("");
        guestContactField.setText("");
        checkInDateField.setText(LocalDate.now().toString());
        nightsSpinner.setValue(1);
        paymentMethodBox.setSelectedIndex(0);
        paymentRefField.setText("");
        simulationOutcomeBox.setSelectedIndex(0);
        updatePaymentMethodUI();
        updateBookingSummary();
        setInitialSimulationOutput();
    }

    private void setInitialSimulationOutput() {
        simulationOutputArea.setText(
                "================================================\n"
                + "     MAKE RESERVATION & PAYMENT SIMULATOR       \n"
                + "================================================\n\n"
                + "How to book & simulate a payment:\n\n"
                + "1. Select an available room on the left\n"
                + "   (or double-click a room in 'Search Rooms').\n\n"
                + "2. Fill in Guest Name, Contact, Check-In Date,\n"
                + "   and Number of Nights.\n\n"
                + "3. Choose a Payment Method (Credit Card,\n"
                + "   Debit Card, UPI / Digital Wallet, or Cash).\n"
                + "   Tip: Click 'Fill Test' for sample details.\n\n"
                + "4. Optional: Choose 'Approve' or 'Decline'\n"
                + "   under Simulation Outcome and test with\n"
                + "   'Simulate Payment Only'.\n\n"
                + "5. Click 'Simulate Payment & Confirm Booking'\n"
                + "   to process payment and save the booking.\n"
                + "================================================\n"
        );
    }

    private void openAddRoomDialog() {
        JTextField numberField = new JTextField();
        JComboBox<RoomCategory> categoryBox = new JComboBox<>(RoomCategory.values());
        JTextField priceField = new JTextField(String.valueOf(RoomCategory.STANDARD.getDefaultPrice()));

        categoryBox.addActionListener(e -> {
            RoomCategory selected = (RoomCategory) categoryBox.getSelectedItem();
            if (selected != null) {
                priceField.setText(String.valueOf(selected.getDefaultPrice()));
            }
        });

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.add(new JLabel("Room Number:"));
        form.add(numberField);
        form.add(new JLabel("Category:"));
        form.add(categoryBox);
        form.add(new JLabel("Price per Night ($):"));
        form.add(priceField);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Add New Room", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                int roomNumber = Integer.parseInt(numberField.getText().trim());
                double price = Double.parseDouble(priceField.getText().trim());
                RoomCategory category = (RoomCategory) categoryBox.getSelectedItem();

                if (roomNumber <= 0 || price <= 0) {
                    showError("Room number and price must be positive.");
                    return;
                }

                if (!manager.addRoom(roomNumber, category, price)) {
                    showError("Room " + roomNumber + " already exists.");
                    return;
                }

                refreshAll();
                JOptionPane.showMessageDialog(this, "Room " + roomNumber + " added successfully!");
            } catch (NumberFormatException ex) {
                showError("Please enter valid numeric values.");
            }
        }
    }

    private void viewSelectedReservationDetails() {
        int row = reservationsTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a reservation from the table.");
            return;
        }

        String resId = (String) reservationsTableModel.getValueAt(row, 0);
        Reservation reservation = manager.findReservationById(resId);
        if (reservation != null) {
            showBookingDetailsDialog(reservation, false);
        }
    }

    private void cancelSelectedReservation() {
        int row = reservationsTable.getSelectedRow();
        if (row < 0) {
            showError("Please select a reservation to cancel.");
            return;
        }

        String resId = (String) reservationsTableModel.getValueAt(row, 0);
        Reservation reservation = manager.findReservationById(resId);
        if (reservation == null) {
            return;
        }
        if ("CANCELLED".equalsIgnoreCase(reservation.getStatus())) {
            showError("Reservation " + resId + " is already cancelled.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                String.format("Cancel reservation %s for %s?\nRoom %d will become available again and $%.2f will be refunded.",
                        resId, reservation.getGuestName(), reservation.getRoomNumber(), reservation.getTotalAmount()),
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (manager.cancelReservation(resId)) {
                refreshAll();
                JOptionPane.showMessageDialog(
                        this,
                        String.format("Reservation %s cancelled. Simulated refund of $%.2f processed.",
                                resId, reservation.getTotalAmount()),
                        "Reservation Cancelled",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }

    private void showBookingDetailsDialog(Reservation reservation, boolean newBooking) {
        String text = reservation.getBookingDetailsText();
        if (newBooking) {
            text = PaymentSimulator.formatPaymentReceipt(
                    reservation.getTransactionId(),
                    reservation.getPaymentMethod(),
                    reservation.getTotalAmount()
            ) + "\n\n" + text;
        }

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(480, 360));

        JOptionPane.showMessageDialog(
                this,
                scroll,
                newBooking ? "Booking Confirmed" : "Booking Details - " + reservation.getReservationId(),
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void refreshAll() {
        refreshRoomsTable();
        refreshBookingRoomsCombo();
        refreshReservationsTable();
        refreshStatusBar();
    }

    private void refreshRoomsTable() {
        roomsTableModel.setRowCount(0);

        String selectedCat = (String) categoryFilterBox.getSelectedItem();
        RoomCategory categoryFilter = null;
        if ("Standard".equals(selectedCat)) {
            categoryFilter = RoomCategory.STANDARD;
        } else if ("Deluxe".equals(selectedCat)) {
            categoryFilter = RoomCategory.DELUXE;
        } else if ("Suite".equals(selectedCat)) {
            categoryFilter = RoomCategory.SUITE;
        }

        boolean availableOnly = availableOnlyCheck.isSelected();
        ArrayList<Room> filtered = manager.searchRooms(categoryFilter, availableOnly);

        for (Room r : filtered) {
            roomsTableModel.addRow(new Object[]{
                    r.getRoomNumber(),
                    r.getCategory().getDisplayName(),
                    String.format("$%.2f", r.getPricePerNight()),
                    r.getCategory().getDescription(),
                    r.isAvailable() ? "Available" : "Booked"
            });
        }
    }

    private void refreshBookingRoomsCombo() {
        isRefreshingBookingRooms = true;
        Room previous = (Room) bookingRoomCombo.getSelectedItem();
        int previousRoomNum = previous != null ? previous.getRoomNumber() : -1;

        bookingRoomCombo.removeAllItems();
        ArrayList<Room> availableRooms = manager.searchRooms(null, true);
        int restoreIndex = 0;
        for (int i = 0; i < availableRooms.size(); i++) {
            Room r = availableRooms.get(i);
            bookingRoomCombo.addItem(r);
            if (r.getRoomNumber() == previousRoomNum) {
                restoreIndex = i;
            }
        }

        boolean hasAvailable = !availableRooms.isEmpty();
        bookingRoomCombo.setEnabled(hasAvailable);
        dryRunPayBtn.setEnabled(hasAvailable);
        confirmBookingBtn.setEnabled(hasAvailable);
        if (hasAvailable) {
            bookingRoomCombo.setSelectedIndex(restoreIndex);
        }

        isRefreshingBookingRooms = false;
        updateBookingSummary();
    }

    private void refreshReservationsTable() {
        reservationsTableModel.setRowCount(0);
        String statusFilter = reservationStatusFilterBox != null
                ? (String) reservationStatusFilterBox.getSelectedItem()
                : "All";
        String query = reservationSearchField != null
                ? reservationSearchField.getText().trim().toLowerCase()
                : "";

        for (Reservation res : manager.getReservations()) {
            if (!"All".equals(statusFilter) && !statusFilter.equalsIgnoreCase(res.getStatus())) {
                continue;
            }
            if (!query.isEmpty()) {
                boolean matchesId = res.getReservationId().toLowerCase().contains(query);
                boolean matchesGuest = res.getGuestName().toLowerCase().contains(query);
                boolean matchesRoom = String.valueOf(res.getRoomNumber()).contains(query);
                if (!matchesId && !matchesGuest && !matchesRoom) {
                    continue;
                }
            }
            reservationsTableModel.addRow(new Object[]{
                    res.getReservationId(),
                    res.getGuestName(),
                    res.getRoomNumber(),
                    res.getCategory().getDisplayName(),
                    res.getCheckInDate(),
                    res.getNights(),
                    String.format("$%.2f", res.getTotalAmount()),
                    res.getPaymentMethod(),
                    res.getStatus()
            });
        }
    }

    private void refreshStatusBar() {
        availableLabel.setText("Available Rooms: " + manager.getAvailableRoomCount());
        bookedLabel.setText("Booked Rooms: " + manager.getBookedRoomCount());
        activeResLabel.setText("Active Reservations: " + manager.getActiveReservationsCount());
        revenueLabel.setText(String.format("Total Revenue: $%.2f", manager.getTotalRevenue()));
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.ERROR_MESSAGE);
    }
}
