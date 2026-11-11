public class Room {
    private final int roomNumber;
    private RoomCategory category;
    private double pricePerNight;
    private boolean available;

    public Room(int roomNumber, RoomCategory category, double pricePerNight, boolean available) {
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.available = available;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public void setCategory(RoomCategory category) {
        this.category = category;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String toFileString() {
        return roomNumber + "|" + category.name() + "|" + pricePerNight + "|" + available;
    }

    public static Room fromFileString(String line) {
        String[] parts = line.split("\\|");
        if (parts.length != 4) {
            return null;
        }
        try {
            int number = Integer.parseInt(parts[0].trim());
            RoomCategory cat = RoomCategory.valueOf(parts[1].trim().toUpperCase());
            double price = Double.parseDouble(parts[2].trim());
            boolean avail = Boolean.parseBoolean(parts[3].trim());
            return new Room(number, cat, price, avail);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("Room %d (%s) - $%.2f/night - %s",
                roomNumber, category.getDisplayName(), pricePerNight, available ? "Available" : "Booked");
    }
}

