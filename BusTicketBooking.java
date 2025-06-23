import java.sql.*;
import java.util.Scanner;

public class BusTicketBooking {

    static final String URL = "jdbc:mysql://localhost:3306/bus_booking";
    static final String USER = "root";
    static final String PASS = "chitra@30";

    static Connection con;

    public static void main(String[] args) throws Exception {
        con = DriverManager.getConnection(URL, USER, PASS);
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Bus Ticket Booking ---");
            System.out.println("1. View Buses");
            System.out.println("2. Book Ticket");
            System.out.println("3. View Booked Tickets");
            System.out.println("4. Cancel Ticket");
            System.out.println("5. Exit");
            System.out.print("Choose: ");
            int choice = sc.nextInt();

            switch (choice) {
                case 1 -> viewBuses();
                case 2 -> bookTicket(sc);
                case 3 -> viewTickets();
                case 4 -> cancelTicket(sc);
                case 5 -> {
                    con.close();
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    public static void viewBuses() throws SQLException {
        String query = "SELECT * FROM buses";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        System.out.println("\nAvailable Buses:");
        while (rs.next()) {
            System.out.printf("Bus ID: %d | From: %s | To: %s | Seats: %d\n",
                    rs.getInt("bus_id"),
                    rs.getString("source"),
                    rs.getString("destination"),
                    rs.getInt("seats_available"));
        }
    }

    public static void bookTicket(Scanner sc) throws SQLException {
        System.out.print("Enter Bus ID: ");
        int busId = sc.nextInt();
        sc.nextLine();  // consume newline
        System.out.print("Enter Passenger Name: ");
        String name = sc.nextLine();

        String seatCheck = "SELECT seats_available FROM buses WHERE bus_id = ?";
        PreparedStatement pst = con.prepareStatement(seatCheck);
        pst.setInt(1, busId);
        ResultSet rs = pst.executeQuery();

        if (rs.next() && rs.getInt("seats_available") > 0) {
            con.setAutoCommit(false);

            String insert = "INSERT INTO tickets (passenger_name, bus_id) VALUES (?, ?)";
            PreparedStatement insertPst = con.prepareStatement(insert);
            insertPst.setString(1, name);
            insertPst.setInt(2, busId);
            insertPst.executeUpdate();

            String updateSeats = "UPDATE buses SET seats_available = seats_available - 1 WHERE bus_id = ?";
            PreparedStatement updatePst = con.prepareStatement(updateSeats);
            updatePst.setInt(1, busId);
            updatePst.executeUpdate();

            con.commit();
            System.out.println("✅ Ticket booked successfully!");
        } else {
            System.out.println("❌ No seats available!");
        }
    }

    public static void viewTickets() throws SQLException {
        String query = "SELECT t.ticket_id, t.passenger_name, b.source, b.destination FROM tickets t JOIN buses b ON t.bus_id = b.bus_id";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        System.out.println("\nBooked Tickets:");
        while (rs.next()) {
            System.out.printf("Ticket ID: %d | Name: %s | From: %s | To: %s\n",
                    rs.getInt("ticket_id"),
                    rs.getString("passenger_name"),
                    rs.getString("source"),
                    rs.getString("destination"));
        }
    }

    public static void cancelTicket(Scanner sc) throws SQLException {
        System.out.print("Enter Ticket ID to Cancel: ");
        int ticketId = sc.nextInt();

        String getBusId = "SELECT bus_id FROM tickets WHERE ticket_id = ?";
        PreparedStatement pst = con.prepareStatement(getBusId);
        pst.setInt(1, ticketId);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            int busId = rs.getInt("bus_id");

            con.setAutoCommit(false);

            String delete = "DELETE FROM tickets WHERE ticket_id = ?";
            PreparedStatement deletePst = con.prepareStatement(delete);
            deletePst.setInt(1, ticketId);
            deletePst.executeUpdate();

            String updateSeats = "UPDATE buses SET seats_available = seats_available + 1 WHERE bus_id = ?";
            PreparedStatement updatePst = con.prepareStatement(updateSeats);
            updatePst.setInt(1, busId);
            updatePst.executeUpdate();

            con.commit();
            System.out.println("✅ Ticket cancelled successfully.");
        } else {
            System.out.println("❌ Ticket ID not found.");
        }
    }
}

