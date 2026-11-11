public class Reservation {
    private final String reservationId;
    private final String guestName;
    private final String guestPhone;
    private final int roomNumber;
    private final RoomCategory category;
    private final String checkInDate;
    private final int nights;
    private final double totalAmount;
    private final String paymentMethod;
    private final String transactionId;
    private String status;

    public Reservation(
            String reservationId,
            String guestName,
            String guestPhone,
            int roomNumber,
            RoomCategory category,
            String checkInDate,
            int nights,
            double totalAmount,
            String paymentMethod,
            String transactionId,
            String status
    ) {
        this.reservationId = reservationId;
        this.guestName = sanitize(guestName);
        this.guestPhone = sanitize(guestPhone);
        this.roomNumber = roomNumber;
        this.category = category;
        this.checkInDate = sanitize(checkInDate);
        this.nights = nights;
        this.totalAmount = totalAmount;
        this.paymentMethod = sanitize(paymentMethod);
        this.transactionId = transactionId;
        this.status = status;
    }

    private String sanitize(String text) {
        return text == null ? "" : text.replace("|", " ").trim();
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getGuestPhone() {
        return guestPhone;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public int getNights() {
        return nights;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBookingDetailsText() {
        StringBuilder sb = new StringBuilder();
        sb.append("================================================\n");
        sb.append("                BOOKING DETAILS                 \n");
        sb.append("================================================\n");
        sb.append("Reservation ID : ").append(reservationId).append("\n");
        sb.append("Status         : ").append(status).append("\n");
        sb.append("Guest Name     : ").append(guestName).append("\n");
        sb.append("Guest Contact  : ").append(guestPhone).append("\n");
        sb.append("------------------------------------------------\n");
        sb.append("Room Number    : ").append(roomNumber).append("\n");
        sb.append("Room Category  : ").append(category.getDisplayName()).append("\n");
        sb.append("Amenities      : ").append(category.getDescription()).append("\n");
        sb.append("Check-In Date  : ").append(checkInDate).append("\n");
        sb.append("Nights         : ").append(nights).append("\n");
        sb.append("------------------------------------------------\n");
        sb.append(String.format("Total Amount   : $%.2f%n", totalAmount));
        sb.append("Payment Method : ").append(paymentMethod).append("\n");
        sb.append("Transaction ID : ").append(transactionId).append("\n");
        sb.append("================================================\n");
        return sb.toString();
    }

    public String toFileString() {
        return String.join("|",
                reservationId,
                guestName,
                guestPhone,
                String.valueOf(roomNumber),
                category.name(),
                checkInDate,
                String.valueOf(nights),
                String.valueOf(totalAmount),
                paymentMethod,
                transactionId,
                status
        );
    }

    public static Reservation fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 11) {
            return null;
        }
        try {
            return new Reservation(
                    parts[0].trim(),
                    parts[1].trim(),
                    parts[2].trim(),
                    Integer.parseInt(parts[3].trim()),
                    RoomCategory.valueOf(parts[4].trim().toUpperCase()),
                    parts[5].trim(),
                    Integer.parseInt(parts[6].trim()),
                    Double.parseDouble(parts[7].trim()),
                    parts[8].trim(),
                    parts[9].trim(),
                    parts[10].trim()
            );
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

