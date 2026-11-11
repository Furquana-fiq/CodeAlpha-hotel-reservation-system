import java.util.Random;

public class PaymentSimulator {
    private static final Random random = new Random();

    public static String[] getPaymentMethods() {
        return new String[]{
                "Credit Card",
                "Debit Card",
                "UPI / Digital Wallet",
                "Cash at Reception"
        };
    }

    public static String processPayment(String method, String referenceInput, double amount) {
        return processPayment(method, referenceInput, amount, false);
    }

    public static String processPayment(String method, String referenceInput, double amount, boolean simulateDecline) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }

        if ("Credit Card".equals(method) || "Debit Card".equals(method)) {
            String digits = referenceInput == null ? "" : referenceInput.replaceAll("[\\s-]", "");
            if (!digits.matches("\\d{12,19}")) {
                throw new IllegalArgumentException("Card number must contain 12 to 19 digits.");
            }
        } else if ("UPI / Digital Wallet".equals(method)) {
            String ref = referenceInput == null ? "" : referenceInput.trim();
            if (ref.isEmpty() || !ref.contains("@") || ref.startsWith("@") || ref.endsWith("@")) {
                throw new IllegalArgumentException("Please enter a valid UPI/Wallet ID (e.g. name@bank).");
            }
        }

        if (simulateDecline) {
            throw new IllegalArgumentException("Payment Declined (Simulated Gateway Failure). Please try again or switch simulation mode.");
        }

        int code = 100000 + random.nextInt(900000);
        return "TXN-" + code;
    }

    public static String maskReference(String method, String referenceInput) {
        if ("Cash at Reception".equals(method)) {
            return "Front Desk Cash";
        }
        if (referenceInput == null || referenceInput.trim().isEmpty()) {
            return "N/A";
        }
        String trimmed = referenceInput.trim();
        if ("Credit Card".equals(method) || "Debit Card".equals(method)) {
            String digits = trimmed.replaceAll("[\\s-]", "");
            if (digits.length() >= 4) {
                return "****-****-****-" + digits.substring(digits.length() - 4);
            }
        }
        return trimmed;
    }

    public static String formatPaymentReceipt(String transactionId, String method, double amount) {
        return String.format(
                "Payment Successful!\nAmount Paid: $%.2f\nMethod: %s\nTransaction ID: %s",
                amount, method, transactionId
        );
    }
}

