import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ParkingGUI extends JFrame {
    private final ParkingLot parkingLot;
    private JTextArea statusArea;
    private JTextField vehicleNumberField;
    private JComboBox<String> vehicleTypeCombo;
    private JTextField unparkField;

    public ParkingGUI() {
        parkingLot = new ParkingLot(5, 3, 4);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Parking Management System -- By Deepak Shukla");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Park Vehicle", createParkPanel());
        tabbedPane.addTab("Unpark Vehicle", createUnparkPanel());
        tabbedPane.addTab("Status", createStatusPanel());
        tabbedPane.addTab("Admin", createAdminPanel());

        add(tabbedPane);
    }

    private JPanel createParkPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        vehicleNumberField = new JTextField();
        vehicleTypeCombo = new JComboBox<>(new String[] { "Two-Wheeler", "Three-Wheeler", "Four-Wheeler" });
        JButton parkButton = new JButton("Park Vehicle");

        parkButton.addActionListener(e -> parkVehicle());

        panel.add(new JLabel("Vehicle Number:"));
        panel.add(vehicleNumberField);
        panel.add(new JLabel("Vehicle Type:"));
        panel.add(vehicleTypeCombo);
        panel.add(new JLabel());
        panel.add(parkButton);

        return panel;
    }

    private JPanel createUnparkPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        unparkField = new JTextField();
        JButton unparkButton = new JButton("Unpark Vehicle");

        unparkButton.addActionListener(e -> unparkVehicle());

        inputPanel.add(new JLabel("Enter Vehicle Number:"), BorderLayout.NORTH);
        inputPanel.add(unparkField, BorderLayout.CENTER);
        inputPanel.add(unparkButton, BorderLayout.SOUTH);

        panel.add(inputPanel, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        statusArea = new JTextArea();
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(statusArea);
        JButton refreshButton = new JButton("Refresh Status");

        refreshButton.addActionListener(e -> updateStatus());

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(refreshButton, BorderLayout.SOUTH);
        updateStatus();

        return panel;
    }

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton revenueButton = new JButton("View Revenue");
        JButton clearRevenueButton = new JButton("Clear Revenue History");
        JButton exitButton = new JButton("Exit System");

        revenueButton.addActionListener(e -> showRevenue());
        clearRevenueButton.addActionListener(e -> clearRevenue());
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(revenueButton);
        panel.add(clearRevenueButton);
        panel.add(exitButton);

        return panel;
    }

    private void parkVehicle() {
        String vehicleNumber = vehicleNumberField.getText().trim().toUpperCase();
        int vehicleType = vehicleTypeCombo.getSelectedIndex() + 1;

        if (vehicleNumber.isEmpty()) {
            showError("Please enter a vehicle number");
            return;
        }

        String result = parkingLot.parkVehicle(vehicleNumber, vehicleType);
        showMessage(result);
        vehicleNumberField.setText("");
        updateStatus();
    }

    private void unparkVehicle() {
        String vehicleNumber = unparkField.getText().trim().toUpperCase();

        if (vehicleNumber.isEmpty()) {
            showError("Please enter a vehicle number");
            return;
        }

        String result = parkingLot.unparkVehicle(vehicleNumber);
        showMessage(result);
        unparkField.setText("");
        updateStatus();
    }

    private void updateStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Current Parking Status\n\n");
        status.append(String.format("%-5s %-15s %-12s %-20s\n",
                "Slot", "Vehicle No.", "Type", "Entry Time"));
        status.append("------------------------------------------------------------\n");

        List<Ticket> tickets = parkingLot.getAllTickets();
        if (tickets.isEmpty()) {
            status.append("No vehicles currently parked\n");
        } else {
            for (Ticket ticket : tickets) {
                Vehicle v = ticket.getVehicle();
                status.append(String.format("%-5d %-15s %-12s %-20s\n",
                        ticket.getSlotNumber(),
                        v.getVehicleNumber(),
                        v.getVehicleType(),
                        new Date(v.getEntryTime() * 3600000L)));
            }
        }

        status.append("\nAvailable Slots:\n");
        status.append("Two-Wheeler: ").append(parkingLot.getAvailableSlots("Two-Wheeler")).append("\n");
        status.append("Three-Wheeler: ").append(parkingLot.getAvailableSlots("Three-Wheeler")).append("\n");
        status.append("Four-Wheeler: ").append(parkingLot.getAvailableSlots("Four-Wheeler"));

        statusArea.setText(status.toString());
    }

    private void showRevenue() {
        String password = JOptionPane.showInputDialog(this, "Enter Admin Password:");
        if (password == null)
            return;

        if (!password.equals(parkingLot.getAdminPassword())) {
            showError("Invalid Password!");
            return;
        }

        StringBuilder revenueInfo = new StringBuilder("Daily Revenue Report\n\n");
        Map<String, Double> revenue = parkingLot.getDailyRevenue();

        if (revenue.isEmpty()) {
            revenueInfo.append("No revenue data available");
        } else {
            revenue.forEach((date, amount) -> revenueInfo.append(date).append(": Rs. ")
                    .append(String.format("%.2f", amount)).append("\n"));
        }

        JOptionPane.showMessageDialog(this, revenueInfo.toString(), "Revenue Report", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearRevenue() {
        String password = JOptionPane.showInputDialog(this, "Enter Admin Password:");
        if (password == null)
            return;

        if (!password.equals(parkingLot.getAdminPassword())) {
            showError("Invalid Password!");
            return;
        }

        parkingLot.getDailyRevenue().clear();
        JOptionPane.showMessageDialog(this, "Revenue history cleared successfully!");
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Parking System", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ParkingGUI gui = new ParkingGUI();
            gui.setVisible(true);
        });
    }
}