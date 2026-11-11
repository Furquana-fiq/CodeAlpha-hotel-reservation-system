import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class ConsoleUI {
    private final HotelManager manager;
    private final Scanner scanner;

    public ConsoleUI(HotelManager manager) {
        this.manager = manager;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;

        while (running) {
            printMenu();
            if (!scanner.hasNextLine()) {
                break;
            }
            int choice = readInt("Choose an option (1-7): ");

            switch (choice) {
                case 1 -> searchRooms();
                case 2 -> makeReservation();
                case 3 -> viewAllReservations();
                case 4 -> viewBookingDetails();
                case 5 -> cancelReservation();
                case 6 -> addNewRoom();
                case 7 -> {
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid choice. Please enter a number from 1 to 7.");
            }
        }

        scanner.close();
    }

    private void printMenu() {
        System.out.println();
        System.out.println("================================================");
        System.out.println("           HOTEL RESERVATION SYSTEM             ");
        System.out.println("================================================");
        System.out.println("1. Search & View Rooms");
        System.out.println("2. Make a Reservation (with Payment Simulation)");
        System.out.println("3. View All Reservations");
        System.out.println("4. View Booking Details by ID");
        System.out.println("5. Cancel Reservation");
        System.out.println("6. Add New Room");
        System.out.println("7. Exit");
        System.out.println("================================================");
        System.out.printf("Available Rooms: %d | Active Bookings: %d | Revenue: $%.2f%n",
                manager.getAvailableRoomCount(),
                manager.getActiveReservationsCount(),
                manager.getTotalRevenue());
        System.out.println("================================================");
    }

    private void searchRooms() {
        System.out.println("Filter by category:");
        System.out.println("0. All Categories");
        System.out.println("1. Standard");
        System.out.println("2. Deluxe");
        System.out.println("3. Suite");
        int catChoice = readInt("Select category (0-3): ");

        RoomCategory filter = switch (catChoice) {
            case 1 -> RoomCategory.STANDARD;
            case 2 -> RoomCategory.DELUXE;
            case 3 -> RoomCategory.SUITE;
            default -> null;
        };

        String availInput = readString("Show available rooms only? (y/n): ");
        boolean availableOnly = "y".equalsIgnoreCase(availInput) || "yes".equalsIgnoreCase(availInput);

        ArrayList<Room> results = manager.searchRooms(filter, availableOnly);
        if (results.isEmpty()) {
            System.out.println("No rooms matched your search.");
            return;
        }

        System.out.println();
        System.out.printf("%-8s %-12s %-12s %-12s %s%n", "Room #", "Category", "Price/Night", "Status", "Amenities");
        System.out.println("--------------------------------------------------------------------------------");
        for (Room room : results) {
            System.out.printf("%-8d %-12s $%-11.2f %-12s %s%n",
                    room.getRoomNumber(),
                    room.getCategory().getDisplayName(),
                    room.getPricePerNight(),
                    room.isAvailable() ? "Available" : "Booked",
                    room.getCategory().getDescription());
        }
    }

    private void makeReservation() {
        ArrayList<Room> available = manager.searchRooms(null, true);
        if (available.isEmpty()) {
            System.out.println("Sorry, no rooms are currently available.");
            return;
        }

        System.out.println("Available Rooms:");
        for (Room r : available) {
            System.out.println(" - " + r);
        }

        int roomNumber = readInt("Enter Room Number to book: ");
        Room selected = manager.findRoom(roomNumber);
        if (selected == null || !selected.isAvailable()) {
            System.out.println("Room " + roomNumber + " is not available.");
            return;
        }

        String guestName = readNonEmptyString("Enter guest full name: ");
        if (guestName == null) {
            return;
        }
        String phone = readNonEmptyString("Enter guest phone or email: ");
        if (phone == null) {
            return;
        }
        String checkIn = readString("Enter check-in date (YYYY-MM-DD, press Enter for today): ");
        if (checkIn.isEmpty()) {
            checkIn = LocalDate.now().toString();
        }

        int nights = readInt("Enter number of nights: ");
        if (nights < 1) {
            System.out.println("Nights must be at least 1.");
            return;
        }

        double total = selected.getPricePerNight() * nights;
        System.out.printf("Total Cost: $%.2f%n", total);

        String[] methods = PaymentSimulator.getPaymentMethods();
        System.out.println("Select Payment Method:");
        for (int i = 0; i < methods.length; i++) {
            System.out.println((i + 1) + ". " + methods[i]);
        }
        int methodIndex = readInt("Choose method (1-" + methods.length + "): ") - 1;
        if (methodIndex < 0 || methodIndex >= methods.length) {
            System.out.println("Invalid payment method. Booking cancelled.");
            return;
        }

        String paymentMethod = methods[methodIndex];
        String paymentRef = "";
        if ("Credit Card".equals(paymentMethod) || "Debit Card".equals(paymentMethod)) {
            paymentRef = readNonEmptyString("Enter 12-19 digit card number: ");
        } else if ("UPI / Digital Wallet".equals(paymentMethod)) {
            paymentRef = readNonEmptyString("Enter UPI / Wallet ID (e.g. user@bank): ");
        }

        try {
            Reservation reservation = manager.bookRoom(
                    roomNumber, guestName, phone, checkIn, nights, paymentMethod, paymentRef
            );
            System.out.println();
            System.out.println(PaymentSimulator.formatPaymentReceipt(
                    reservation.getTransactionId(), paymentMethod, reservation.getTotalAmount()));
            System.out.println();
            System.out.println(reservation.getBookingDetailsText());
        } catch (IllegalArgumentException e) {
            System.out.println("Booking failed: " + e.getMessage());
        }
    }

    private void viewAllReservations() {
        ArrayList<Reservation> list = manager.getReservations();
        if (list.isEmpty()) {
            System.out.println("No reservations recorded yet.");
            return;
        }

        System.out.println();
        System.out.printf("%-10s %-18s %-8s %-10s %-12s %-7s %-10s %-10s%n",
                "ID", "Guest", "Room #", "Category", "Check-In", "Nights", "Total", "Status");
        System.out.println("------------------------------------------------------------------------------------------");
        for (Reservation r : list) {
            System.out.printf("%-10s %-18s %-8d %-10s %-12s %-7d $%-9.2f %-10s%n",
                    r.getReservationId(),
                    r.getGuestName().length() > 17 ? r.getGuestName().substring(0, 14) + "..." : r.getGuestName(),
                    r.getRoomNumber(),
                    r.getCategory().getDisplayName(),
                    r.getCheckInDate(),
                    r.getNights(),
                    r.getTotalAmount(),
                    r.getStatus());
        }
    }

    private void viewBookingDetails() {
        String id = readNonEmptyString("Enter Reservation ID (e.g. RES-1001): ");
        if (id == null) {
            return;
        }
        Reservation res = manager.findReservationById(id);
        if (res == null) {
            System.out.println("No reservation found with ID: " + id);
            return;
        }
        System.out.println();
        System.out.println(res.getBookingDetailsText());
    }

    private void cancelReservation() {
        String id = readNonEmptyString("Enter Reservation ID to cancel: ");
        if (id == null) {
            return;
        }
        Reservation res = manager.findReservationById(id);
        if (res == null) {
            System.out.println("Reservation not found.");
            return;
        }
        if ("CANCELLED".equalsIgnoreCase(res.getStatus())) {
            System.out.println("Reservation " + res.getReservationId() + " is already cancelled.");
            return;
        }

        String confirm = readString("Confirm cancellation for " + res.getGuestName() + " (y/n)? ");
        if ("y".equalsIgnoreCase(confirm) || "yes".equalsIgnoreCase(confirm)) {
            manager.cancelReservation(id);
            System.out.printf("Reservation %s cancelled. Room %d is available again. Simulated refund: $%.2f%n",
                    res.getReservationId(), res.getRoomNumber(), res.getTotalAmount());
        } else {
            System.out.println("Cancellation aborted.");
        }
    }

    private void addNewRoom() {
        int number = readInt("Enter new room number: ");
        if (number <= 0) {
            System.out.println("Room number must be positive.");
            return;
        }
        if (manager.findRoom(number) != null) {
            System.out.println("Room " + number + " already exists.");
            return;
        }

        System.out.println("Select Category: 1. Standard | 2. Deluxe | 3. Suite");
        int catChoice = readInt("Choice (1-3): ");
        RoomCategory category = switch (catChoice) {
            case 2 -> RoomCategory.DELUXE;
            case 3 -> RoomCategory.SUITE;
            default -> RoomCategory.STANDARD;
        };

        double price = readDouble("Enter price per night (default $" + category.getDefaultPrice() + "): ");
        if (price <= 0) {
            price = category.getDefaultPrice();
        }

        manager.addRoom(number, category, price);
        System.out.println("Added Room " + number + " (" + category.getDisplayName() + ") at $" + price + "/night.");
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return "";
        }
        return scanner.nextLine().trim();
    }

    private String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return null;
            }
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return -1;
            }
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextLine()) {
                return -1.0;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return -1.0;
            }
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}

