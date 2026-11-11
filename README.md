# Hotel Reservation System

A clean, object-oriented Java application to search, book, and manage hotel rooms with room categorization, payment simulation, booking details viewing, and persistent File I/O storage.

## Project Structure

- `Main.java` — Application entry point (launches Swing GUI by default, or Console CLI with `--console`).
- `RoomCategory.java` — Enum defining `STANDARD`, `DELUXE`, and `SUITE` room tiers with pricing and amenities.
- `Room.java` — Represents a hotel room (`roomNumber`, `category`, `pricePerNight`, `available`) and File I/O serialization.
- `Reservation.java` — Represents a guest reservation with payment receipt details and booking summary formatting.
- `PaymentSimulator.java` — Simulates payment validation and generates transaction IDs (`TXN-...`).
- `HotelManager.java` — Core OOP manager handling room search, reservations, cancellations, and File I/O (`rooms.txt`, `reservations.txt`).
- `HotelGUI.java` — Java Swing graphical interface with dedicated tabs for searching rooms, making reservations & simulating payments, and managing bookings.
- `ConsoleUI.java` — Optional terminal-based menu interface.
- `README.md` — Project documentation and run instructions.

## Features

- **Room Categorization**: `Standard` ($100/night), `Deluxe` ($180/night), and `Suite` ($320/night).
- **Search & Filter**: Filter rooms by category and availability (`Available Only`).
- **Make Reservations & Payment Simulation**: Book available rooms with guest details, check-in date, number of nights, payment method (`Credit Card`, `Debit Card`, `UPI / Digital Wallet`, `Cash at Reception`), and simulated transaction processing.
- **Cancel Reservations**: Cancel existing bookings to automatically restore room availability and simulate refunds.
- **Booking Details View**: View complete booking receipts including Reservation ID, Transaction ID, guest details, room info, and status (`CONFIRMED` / `CANCELLED`).
- **File I/O Persistence**: Automatically reads and writes room availability and bookings to `rooms.txt` and `reservations.txt`.

## How to Run

Navigate to the `Hotel Reservation System` folder in a terminal and run:

### Launch GUI (Default)

```bash
java Main.java
```

### Launch Console Mode

```bash
java Main.java --console
```

### Compile & Run Separately

```bash
javac *.java
java Main
```

