
import java.util.*;

public class Main {

    static Scanner sc = new Scanner(System.in);
    static MovieManager manager = new MovieManager();

    public static void main(String[] args) {

        manager.addMovie(new Movie(1, "Interstellar", 5, 8));
        manager.addMovie(new Movie(2, "Inception", 5, 8));
        manager.addMovie(new Movie(3, "3 Idiots", 5, 8));

        while (true) {

            System.out.println("\n===== MOVIE TICKET BOOKING =====");
            System.out.println("1. Display Movies");
            System.out.println("2. Sort Movies");
            System.out.println("3. Show Seats");
            System.out.println("4. Book Ticket");
            System.out.println("5. Process Booking");
            System.out.println("6. View Bookings");
            System.out.println("7. Cancel Booking");
            System.out.println("0. Exit");

            System.out.print("Enter choice: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    manager.displayMovies();
                    break;

                case 2:
                    manager.sortMovies();
                    break;

                case 3: {
                    Movie movie = manager.chooseMovie();
                    if (movie != null)
                        movie.displaySeats();
                    break;
                }

                case 4:
                    bookTicket();
                    break;

                case 5:
                    manager.processBooking();
                    break;

                case 6:
                    manager.viewBookings();
                    break;

                case 7:
                    System.out.print("Enter Ticket ID: ");
                    String id = sc.nextLine();
                    manager.cancelBooking(id);
                    break;

                case 0:
                    System.out.println("Thank you!");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    static void bookTicket() {

        Movie movie = manager.chooseMovie();

        if (movie == null)
            return;

        System.out.print("Enter Customer Name: ");
        String name = sc.nextLine();

        System.out.print("Enter Row Number: ");
        int row = sc.nextInt() - 1;

        System.out.print("Enter Seat Number: ");
        int seat = sc.nextInt();
        sc.nextLine();
        seat--;

        System.out.print("Booking Type (VIP/NORMAL): ");
        String type = sc.nextLine();

        boolean vip = type.equalsIgnoreCase("VIP");

        manager.addBooking(
            new BookingRequest(name, movie, row, seat, vip)
        );
    }
}

// ================= MOVIE CLASS =================

class Movie {

    int id;
    String name;

    // 2D ARRAY
    boolean[][] seats;

    Movie(int id, String name, int rows, int columns) {
        this.id = id;
        this.name = name;
        seats = new boolean[rows][columns];
    }

    boolean bookSeat(int row, int seat) {

        if (row < 0 || row >= seats.length ||
            seat < 0 || seat >= seats[0].length) {
            return false;
        }

        if (seats[row][seat])
            return false;

        seats[row][seat] = true;
        return true;
    }

    void releaseSeat(int row, int seat) {
        seats[row][seat] = false;
    }

    void displaySeats() {

        System.out.println("\nSeat Layout: " + name);
        System.out.println("O = Available, X = Booked");

        for (int i = 0; i < seats.length; i++) {

            System.out.print("Row " + (i + 1) + ": ");

            for (int j = 0; j < seats[i].length; j++) {

                if (seats[i][j])
                    System.out.print(" X ");
                else
                    System.out.print(" O ");
            }

            System.out.println();
        }
    }
}

// ================= BOOKING REQUEST =================

class BookingRequest {

    String customerName;
    Movie movie;
    int row, seat;
    boolean vip;

    static long counter = 0;
    long sequence;

    BookingRequest(String name, Movie movie,
                   int row, int seat, boolean vip) {

        this.customerName = name;
        this.movie = movie;
        this.row = row;
        this.seat = seat;
        this.vip = vip;
        this.sequence = counter++;
    }
}

// ================= BOOKING CLASS =================

class Booking {

    String ticketId;
    String customerName;
    Movie movie;
    int row, seat;
    boolean vip;

    Booking(String id, BookingRequest request) {

        ticketId = id;
        customerName = request.customerName;
        movie = request.movie;
        row = request.row;
        seat = request.seat;
        vip = request.vip;
    }

    public String toString() {

        return "Ticket ID: " + ticketId +
               " | Customer: " + customerName +
               " | Movie: " + movie.name +
               " | Seat: " + (row + 1) + "-" + (seat + 1) +
               " | Type: " + (vip ? "VIP" : "NORMAL");
    }
}

// ================= MOVIE MANAGER =================

class MovieManager {

    ArrayList<Movie> movies = new ArrayList<>();

    // PRIORITY QUEUE
    PriorityQueue<BookingRequest> queue =
        new PriorityQueue<>((a, b) -> {

            if (a.vip != b.vip)
                return a.vip ? -1 : 1;

            return Long.compare(a.sequence, b.sequence);
        });

    // HASHMAP
    HashMap<String, Booking> bookings = new HashMap<>();

    int ticketCounter = 1001;

    void addMovie(Movie movie) {
        movies.add(movie);
    }

    void displayMovies() {

        System.out.println("\nAvailable Movies:");

        for (Movie m : movies) {
            System.out.println(m.id + ". " + m.name);
        }
    }

    // SORTING
    void sortMovies() {

        movies.sort(
            Comparator.comparing(m -> m.name.toLowerCase())
        );

        System.out.println("Movies sorted successfully!");
        displayMovies();
    }

    Movie chooseMovie() {

        displayMovies();

        System.out.print("Enter Movie ID: ");
        int id = Main.sc.nextInt();
        Main.sc.nextLine();

        for (Movie m : movies) {

            if (m.id == id)
                return m;
        }

        System.out.println("Movie not found!");
        return null;
    }

    void addBooking(BookingRequest request) {

        if (request.row < 0 ||
            request.row >= request.movie.seats.length ||
            request.seat < 0 ||
            request.seat >= request.movie.seats[0].length) {

            System.out.println("Invalid seat!");
            return;
        }

        queue.add(request);

        System.out.println("Booking request added!");
    }

    // PROCESS PRIORITY QUEUE
    void processBooking() {

        if (queue.isEmpty()) {
            System.out.println("No pending bookings!");
            return;
        }

        BookingRequest request = queue.poll();

        boolean success = request.movie.bookSeat(
            request.row, request.seat
        );

        if (!success) {
            System.out.println("Seat already booked!");
            return;
        }

        String id = "T" + ticketCounter++;

        Booking booking = new Booking(id, request);

        bookings.put(id, booking);

        System.out.println("\nBooking Successful!");
        System.out.println(booking);
    }

    // DISPLAY HASHMAP DATA
    void viewBookings() {

        if (bookings.isEmpty()) {
            System.out.println("No bookings found!");
            return;
        }

        System.out.println("\nConfirmed Bookings:");

        for (Booking b : bookings.values()) {
            System.out.println(b);
        }
    }

    // CANCEL BOOKING
    void cancelBooking(String id) {

        Booking booking = bookings.remove(id);

        if (booking == null) {
            System.out.println("Ticket not found!");
            return;
        }

        booking.movie.releaseSeat(
            booking.row, booking.seat
        );

        System.out.println("Booking cancelled successfully!");
    }
}
