public enum RoomCategory {
    STANDARD("Standard", 100.0, "Cozy room with Wi-Fi and TV"),
    DELUXE("Deluxe", 180.0, "Spacious room with balcony and breakfast"),
    SUITE("Suite", 320.0, "Luxury suite with living area and ocean view");

    private final String displayName;
    private final double defaultPrice;
    private final String description;

    RoomCategory(String displayName, double defaultPrice, String description) {
        this.displayName = displayName;
        this.defaultPrice = defaultPrice;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDefaultPrice() {
        return defaultPrice;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

