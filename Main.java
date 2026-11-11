import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        HotelManager manager = new HotelManager();

        boolean useConsole = GraphicsEnvironment.isHeadless();
        for (String arg : args) {
            if ("--console".equalsIgnoreCase(arg)) {
                useConsole = true;
            }
        }

        if (useConsole) {
            ConsoleUI consoleUI = new ConsoleUI(manager);
            consoleUI.start();
        } else {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            SwingUtilities.invokeLater(() -> {
                HotelGUI gui = new HotelGUI(manager);
                gui.setVisible(true);
            });
        }
    }
}

