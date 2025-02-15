import java.io.*;
import java.util.*;
import java.util.regex.*;

class Vehicle implements Serializable {
    String vehicleNumber;
    String vehicleType;
    int entryTime;

    public Vehicle(String vehicleNumber, String vehicleType, int entryTime) {
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.entryTime = entryTime;
    }

    public String getVehicleNumber() { return vehicleNumber; }
    public String getVehicleType() { return vehicleType; }
    public int getEntryTime() { return entryTime; }
}

class Ticket implements Serializable {
    int slotNumber;
    Vehicle vehicle;

    public Ticket(int slotNumber, Vehicle vehicle) {
        this.slotNumber = slotNumber;
        this.vehicle = vehicle;
    }

    public int getSlotNumber() { return slotNumber; }
    public Vehicle getVehicle() { return vehicle; }
}

public class ParkingLot implements Serializable {
    private final int capacity;
    private final Map<Integer, String> vehicleTypes;
    private final Map<String, PriorityQueue<Integer>> availableSlots;
    private final Map<String, Map<String, Ticket>> occupiedSlots;
    private final String parkingDataFile = "parking_data.dat";
    private final String revenueFile = "revenue_data.dat";
    private final String adminPassword = "admin123";
    private final double hourlyRate = 20.0;
    private final Pattern vehiclePattern = Pattern.compile("^[A-Z]{2}\\d{2}[A-Z]{0,2}\\d{4}$");
    private final Map<String, Double> dailyRevenue = new HashMap<>();

    public ParkingLot(int twoWheelerCapacity, int threeWheelerCapacity, int fourWheelerCapacity) {
        this.capacity = twoWheelerCapacity + threeWheelerCapacity + fourWheelerCapacity;
        this.vehicleTypes = new HashMap<>();
        this.availableSlots = new HashMap<>();
        this.occupiedSlots = new HashMap<>();

        vehicleTypes.put(1, "Two-Wheeler");
        vehicleTypes.put(2, "Three-Wheeler");
        vehicleTypes.put(3, "Four-Wheeler");

        for (String type : vehicleTypes.values()) {
            availableSlots.put(type, new PriorityQueue<>());
            occupiedSlots.put(type, new HashMap<>());
        }

        initializeSlots("Two-Wheeler", twoWheelerCapacity);
        initializeSlots("Three-Wheeler", threeWheelerCapacity);
        initializeSlots("Four-Wheeler", fourWheelerCapacity);

        loadFromFile();
        loadRevenue();
    }

    private void initializeSlots(String type, int count) {
        for (int i = 1; i <= count; i++) {
            availableSlots.get(type).offer(i);
        }
    }
    
    private boolean isValidVehicleNumber(String vehicleNumber) {
        return vehiclePattern.matcher(vehicleNumber).matches();
    }

    public String parkVehicle(String vehicleNumber, int vehicleTypeOption) {
        if (!isValidVehicleNumber(vehicleNumber)) {
            return "Invalid Vehicle Number Format!";
        }

        String vehicleType = vehicleTypes.get(vehicleTypeOption);
        if (vehicleType == null) {
            return "Invalid Vehicle Type Selected!";
        }

        if (occupiedSlots.get(vehicleType).containsKey(vehicleNumber)) {
            return "Vehicle already parked!";
        }

        if (availableSlots.get(vehicleType).isEmpty()) {
            return "No Available Slots for " + vehicleType;
        }

        int slot = availableSlots.get(vehicleType).poll();
        int entryTime = (int) (System.currentTimeMillis() / 3600000);
        Vehicle vehicle = new Vehicle(vehicleNumber, vehicleType, entryTime);
        Ticket ticket = new Ticket(slot, vehicle);
        occupiedSlots.get(vehicleType).put(vehicleNumber, ticket);

        saveToFile();
        return "Vehicle parked successfully!\nSlot: " + slot + 
               "\nEntry Time: " + new Date(entryTime * 3600000L);
    }

    public String unparkVehicle(String vehicleNumber) {
        for (String type : occupiedSlots.keySet()) {
            if (occupiedSlots.get(type).containsKey(vehicleNumber)) {
                Ticket removedTicket = occupiedSlots.get(type).remove(vehicleNumber);
                availableSlots.get(type).offer(removedTicket.slotNumber);
                int currentTime = (int) (System.currentTimeMillis() / 3600000);
                int duration = currentTime - removedTicket.vehicle.entryTime;
                double charge = Math.max(duration, 1) * hourlyRate;

                String date = new Date().toString().substring(0, 10);
                dailyRevenue.put(date, dailyRevenue.getOrDefault(date, 0.0) + charge);

                saveToFile();
                saveRevenue();
                
                return String.format("Vehicle unparked successfully!\nSlot: %d\nDuration: %d hours\nTotal Charge: Rs. %.2f",
                                   removedTicket.slotNumber, duration, charge);
            }
        }
        return "Vehicle not found!";
    }

    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = new ArrayList<>();
        for (String type : occupiedSlots.keySet()) {
            tickets.addAll(occupiedSlots.get(type).values());
        }
        return tickets;
    }

    public Map<String, Double> getDailyRevenue() { return new HashMap<>(dailyRevenue); }
    public String getAdminPassword() { return adminPassword; }
    public int getAvailableSlots(String type) { return availableSlots.get(type).size(); }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(parkingDataFile))) {
            oos.writeObject(occupiedSlots);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(parkingDataFile))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                occupiedSlots.putAll((Map) obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No previous parking data found");
        }
    }

    private void saveRevenue() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(revenueFile))) {
            oos.writeObject(dailyRevenue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRevenue() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(revenueFile))) {
            Object obj = ois.readObject();
            if (obj instanceof Map) {
                dailyRevenue.putAll((Map) obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("No previous revenue data found");
        }
    }
}