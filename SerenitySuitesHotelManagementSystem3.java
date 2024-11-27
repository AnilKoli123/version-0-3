import java.sql.*;
import java.util.Scanner;
import java.util.Vector;

public class SerenitySuitesHotelManagementSystem3 {
    private Vector<Room> rooms = new Vector<>();
    private Vector<Booking> bookings = new Vector<>();
    private Scanner scanner = new Scanner(System.in);
    private boolean isFirstRun = true;

    public SerenitySuitesHotelManagementSystem() {
        loadRoomsFromDatabase();
    }

    private void loadRoomsFromDatabase() {
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM rooms")) {

            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("room_number"),
                        rs.getString("type"),
                        rs.getBoolean("available"),
                        rs.getDouble("price")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error loading rooms: " + e.getMessage());
        }
    }

    public void bookRoom() {
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();
        System.out.print("Enter contact: ");
        String contact = scanner.nextLine();
        System.out.print("Enter address: ");
        String address = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.println("Available rooms:");
        rooms.stream().filter(Room::isAvailable).forEach(System.out::println);

        Vector<Room> bookedRooms = new Vector<>();
        double totalAmount = 0;

        while (true) {
            System.out.print("Enter room number to book (or 0 to finish): ");
            int roomNumber = scanner.nextInt();
            if (roomNumber == 0) break;

            Room room = rooms.stream().filter(r -> r.getRoomNumber() == roomNumber && r.isAvailable()).findFirst().orElse(null);
            if (room != null) {
                bookedRooms.add(room);
                room.setAvailable(false);
                totalAmount += room.getPrice();
                System.out.println("Room " + roomNumber + " booked successfully.");
            } else {
                System.out.println("Room not available or invalid room number.");
            }
        }

        if (!bookedRooms.isEmpty()) {
            saveBookingToDatabase(name, contact, address, email, bookedRooms);
            System.out.println("\nBooking Complete!");
            System.out.println("Customer Details: " + name + ", Contact: " + contact + ", Address: " + address + ", Email: " + email);
            System.out.println("Booked Rooms: ");
            bookedRooms.forEach(room -> System.out.println("Room " + room.getRoomNumber() + " (" + room.getType() + ") - Price: " + room.getPrice()));
            System.out.println("Total MRP: " + totalAmount);
        } else {
            System.out.println("No rooms booked. Please book at least one room.");
        }
        scanner.nextLine();
    }

    private void saveBookingToDatabase(String name, String contact, String address, String email, Vector<Room> bookedRooms) {
        try (Connection conn = DBHelper.getConnection()) {
            for (Room room : bookedRooms) {
                String insertBooking = "INSERT INTO bookings (customer_name, contact, address, email, room_number) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement bookingStmt = conn.prepareStatement(insertBooking);
                bookingStmt.setString(1, name);
                bookingStmt.setString(2, contact);
                bookingStmt.setString(3, address);
                bookingStmt.setString(4, email);
                bookingStmt.setInt(5, room.getRoomNumber());
                bookingStmt.executeUpdate();

                String updateRoom = "UPDATE rooms SET available = FALSE WHERE room_number = ?";
                PreparedStatement roomStmt = conn.prepareStatement(updateRoom);
                roomStmt.setInt(1, room.getRoomNumber());
                roomStmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Error saving booking: " + e.getMessage());
        }
    }

    public void viewAvailableRooms() {
        System.out.println("Available rooms:");
        rooms.stream().filter(Room::isAvailable).forEach(System.out::println);
    }

    public static void main(String[] args) {
        SerenitySuitesHotelManagementSystem hms = new SerenitySuitesHotelManagementSystem();
        Scanner scanner = new Scanner(System.in);
        int choice;

        if (hms.isFirstRun) {
            System.out.println("Welcome to Serenity Suites Hotel");
            hms.isFirstRun = false;
        }

        do {
            System.out.println("\n1. Book Room\n2. View Available Rooms\n3. Exit");
            System.out.print("Choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1: hms.bookRoom(); break;
                case 2: hms.viewAvailableRooms(); break;
                case 3: 
                    System.out.println("Thank you for visiting Serenity Suites Hotel!"); 
                    break;
                default: System.out.println("Invalid choice.");
            }
        } while (choice != 3);
        scanner.close();
    }
}

class DBHelper {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/serenity_suites";
        String user = "root";  // Default XAMPP user
        String password = "";  // Default XAMPP password
        return DriverManager.getConnection(url, user, password);
    }
}

class Room {
    private int roomNumber;
    private String type;
    private boolean available;
    private double price;

    public Room(int roomNumber, String type, boolean available, double price) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.available = available;
        this.price = price;
    }

    public int getRoomNumber() { return roomNumber; }
    public String getType() { return type; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setType(String type) { this.type = type; }
    public double getPrice() { return price; }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + type + ") - Price: " + price + " Available: " + (available ? "Yes" : "No");
    }
}
