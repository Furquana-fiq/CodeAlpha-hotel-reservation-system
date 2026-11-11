import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class HotelManager {
    private static final String ROOMS_FILE = "rooms.txt";
    private static final String RESERVATIONS_FILE = "reservations.txt";

    private final ArrayList<Room> rooms;
    private final ArrayList<Reservation> reservations;
    private int nextReservationNumber;

    public HotelManager() {
        this.rooms = new ArrayList<>();
        this.reservations = new ArrayList<>();
        this.nextReservationNumber = 1001;
        loadRooms();
        loadReservations();
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public ArrayList<Reservation> getReservations() {
        return reservations;
    }

    public ArrayList<Room> searchRooms(RoomCategory categoryFilter, boolean availableOnly) {
        ArrayList<Room> results = new ArrayList<>();
        for (Room room : rooms) {
            boolean matchesCategory = (categoryFilter == null) || (room.getCategory() == categoryFilter);
            boolean matchesAvailability = !availableOnly || room.isAvailable();
            if (matchesCategory && matchesAvailability) {
                results.add(room);
            }
        }
        return results;
    }

    public Room findRoom(int roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                return room;
            }
        }
        return null;
    }

    public boolean addRoom(int roomNumber, RoomCategory category, double pricePerNight) {
        if (findRoom(roomNumber) != null) {
            return false;
        }
        rooms.add(new Room(roomNumber, category, pricePerNight, true));
        saveRooms();
        return true;
    }

    public Reservation findReservationById(String reservationId) {
        for (Reservation res : reservations) {
            if (res.getReservationId().equalsIgnoreCase(reservationId.trim())) {
                return res;
            }
        }
        return null;
    }

    public Reservation bookRoom(
            int roomNumber,
            String guestName,
            String guestPhone,
            String checkInDate,
            int nights,
            String paymentMethod,
            String paymentReference
    ) {
        return bookRoom(roomNumber, guestName, guestPhone, checkInDate, nights, paymentMethod, paymentReference, false);
    }

    public Reservation bookRoom(
            int roomNumber,
            String guestName,
            String guestPhone,
            String checkInDate,
            int nights,
            String paymentMethod,
            String paymentReference,
            boolean simulatePaymentFailure
    ) {
        Room room = findRoom(roomNumber);
        if (room == null) {
            throw new IllegalArgumentException("Room " + roomNumber + " does not exist.");
        }
        if (!room.isAvailable()) {
            throw new IllegalArgumentException("Room " + roomNumber + " is already booked.");
        }
        if (guestName == null || guestName.trim().isEmpty()) {
            throw new IllegalArgumentException("Guest name is required.");
        }
        if (guestPhone == null || guestPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Guest contact is required.");
        }
        if (checkInDate == null || checkInDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Check-in date is required.");
        }
        try {
            LocalDate.parse(checkInDate.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Check-in date must be in YYYY-MM-DD format.");
        }
        if (nights < 1 || nights > 365) {
            throw new IllegalArgumentException("Number of nights must be between 1 and 365.");
        }

        double totalAmount = room.getPricePerNight() * nights;
        String transactionId = PaymentSimulator.processPayment(paymentMethod, paymentReference, totalAmount, simulatePaymentFailure);

        String reservationId = "RES-" + nextReservationNumber;
        nextReservationNumber++;

        Reservation reservation = new Reservation(
                reservationId,
                guestName,
                guestPhone,
                room.getRoomNumber(),
                room.getCategory(),
                checkInDate,
                nights,
                totalAmount,
                paymentMethod,
                transactionId,
                "CONFIRMED"
        );

        room.setAvailable(false);
        reservations.add(reservation);

        saveRooms();
        saveReservations();

        return reservation;
    }

    public boolean cancelReservation(String reservationId) {
        Reservation reservation = findReservationById(reservationId);
        if (reservation == null || "CANCELLED".equalsIgnoreCase(reservation.getStatus())) {
            return false;
        }

        reservation.setStatus("CANCELLED");

        Room room = findRoom(reservation.getRoomNumber());
        if (room != null) {
            room.setAvailable(true);
        }

        saveRooms();
        saveReservations();
        return true;
    }

    public int getAvailableRoomCount() {
        int count = 0;
        for (Room room : rooms) {
            if (room.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    public int getBookedRoomCount() {
        return rooms.size() - getAvailableRoomCount();
    }

    public int getActiveReservationsCount() {
        int count = 0;
        for (Reservation res : reservations) {
            if ("CONFIRMED".equalsIgnoreCase(res.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public double getTotalRevenue() {
        double total = 0.0;
        for (Reservation res : reservations) {
            if ("CONFIRMED".equalsIgnoreCase(res.getStatus())) {
                total += res.getTotalAmount();
            }
        }
        return total;
    }

    private void loadRooms() {
        rooms.clear();
        File file = new File(ROOMS_FILE);
        if (!file.exists()) {
            initializeDefaultRooms();
            saveRooms();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Room room = Room.fromFileString(line);
                    if (room != null) {
                        rooms.add(room);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read rooms file: " + e.getMessage());
        }

        if (rooms.isEmpty()) {
            initializeDefaultRooms();
            saveRooms();
        }
    }

    private void saveRooms() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ROOMS_FILE))) {
            for (Room room : rooms) {
                writer.write(room.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not save rooms file: " + e.getMessage());
        }
    }

    private void loadReservations() {
        reservations.clear();
        File file = new File(RESERVATIONS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Reservation res = Reservation.fromFileString(line);
                    if (res != null) {
                        reservations.add(res);
                        updateNextReservationNumber(res.getReservationId());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Could not read reservations file: " + e.getMessage());
        }
    }

    private void saveReservations() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RESERVATIONS_FILE))) {
            for (Reservation res : reservations) {
                writer.write(res.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Could not save reservations file: " + e.getMessage());
        }
    }

    private void updateNextReservationNumber(String reservationId) {
        if (reservationId != null && reservationId.startsWith("RES-")) {
            try {
                int num = Integer.parseInt(reservationId.substring(4));
                if (num >= nextReservationNumber) {
                    nextReservationNumber = num + 1;
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private void initializeDefaultRooms() {
        rooms.add(new Room(101, RoomCategory.STANDARD, 100.0, true));
        rooms.add(new Room(102, RoomCategory.STANDARD, 100.0, true));
        rooms.add(new Room(103, RoomCategory.STANDARD, 110.0, true));
        rooms.add(new Room(201, RoomCategory.DELUXE, 180.0, true));
        rooms.add(new Room(202, RoomCategory.DELUXE, 180.0, true));
        rooms.add(new Room(203, RoomCategory.DELUXE, 200.0, true));
        rooms.add(new Room(301, RoomCategory.SUITE, 320.0, true));
        rooms.add(new Room(302, RoomCategory.SUITE, 350.0, true));
    }
}

