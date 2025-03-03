import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class Library{
    private static final String URL = "jdbc:mysql://localhost:3306/library";
    private static final String USER = "root";
    private static final String PASSWORD = "Juli@123";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n===== Welcome to the Library Management System=====");
            System.out.println("1. Admin Login\n2. User Login");
            System.out.print("Enter your choice: ");

            try {
                int choice = scanner.nextInt();  // Attempt to read the integer input
                scanner.nextLine();  // Clear the newline character from the input buffer

                switch (choice) {
                    case 1:
                        adminLogin(scanner);
                        break;
                    case 2:
                        userLogin(scanner);
                        break;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (InputMismatchException e) {

                System.out.println("Invalid choice. Please enter a valid number.");
                scanner.nextLine();
            }
        }
    }
    private static void newRegistration(Scanner scanner) {
        System.out.println("\n===== User Registration =====");
        System.out.print("Enter your name: ");
        String name =scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        if (!isValidPassword(password)) {
            System.out.println("❌❌ Password must meet the following criteria: ");
            System.out.println("- At least 8 characters long");
            System.out.println("- Contains at least one uppercase letter");
            System.out.println("- Contains at least one lowercase letter");
            System.out.println("- Contains at least one numeric digit");
            System.out.println("- Contains at least one special character (e.g., !@#$%^&*)");
            return;
        }
        if (!isValidEmail(email)) {
            System.out.println("❌❌ Email must meet the following criteria: ");
            System.out.println("- Ensures the email contains valid characters (a-z, A-Z, 0-9, ., _, %, +, -).");
            System.out.println("- Must have \"@\" and a domain like gmail.com.");
            System.out.println("- Ends with a valid TLD (.com, .org, .net, etc.)");
            return;
        }

        try (Connection conn = DriverManager.getConnection(URL,USER,PASSWORD)) {
            String query = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();

            System.out.println("Registration successful! You can now log in.");

        } catch (SQLException e) {
            System.out.println("Enter Valid Value only the integers 1");
        }
    }




    public static boolean isValidEmail(String email) {
        String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }



    private static boolean isValidPassword(String password) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }
    private static void adminLogin(Scanner scanner) {
        System.out.print("Enter Admin Username: ");
        String username = scanner.nextLine();
        System.out.print("Enter Admin Password: ");
        String password = scanner.nextLine();

        if (validateAdmin(username, password)) {
            System.out.println("Admin Login Successful.");
            adminMenu(scanner);
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    private static boolean validateAdmin(String username, String password) {
        return username.equals("prabha") && password.equals("prabha@123");
    }

    private static void adminMenu(Scanner scanner) {
        while (true) {
            System.out.println("1. Add Book\n2. Remove Book\n3. View Books \n4. View Borrow history \n5. Logout");
            System.out.println("Enter your choice");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    addBook(scanner);
                    break;
                case 2:
                    removeBook(scanner);
                    break;
                case 3:
                    viewBooks();
                    break;
                case 4:
                    viewBorrowHistory();
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    private static void viewBorrowHistory() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT u.name, b.title, bb.borrow_date, bb.return_date, " +
                    "CASE WHEN bb.return_date IS NULL AND EXISTS " +
                    "(SELECT 1 FROM borrowed_books WHERE book_id = b.id AND user_id = u.id AND return_date IS NULL) " +
                    "THEN 'Currently Borrowed' ELSE bb.return_date END as status " +
                    "FROM borrowed_books bb " +
                    "JOIN users u ON bb.user_id = u.id " +
                    "JOIN books b ON bb.book_id = b.id " +
                    "ORDER BY bb.borrow_date DESC";

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\n===== Borrow History =====");
            System.out.printf("%-20s %-30s %-15s %-20s%n",
                    "User Name", "Book Title", "Borrow Date", "Status/Return Date");
            System.out.println("--------------------------------------------------------------------------------");

            while (rs.next()) {
                String userName = rs.getString("name");
                String bookTitle = rs.getString("title");
                Date borrowDate = rs.getDate("borrow_date");
                String status = rs.getString("status");

                if (status == null) {
                    status = "Currently Borrowed";
                }

                System.out.printf("%-20s %-30s %-15s %-20s%n",
                        userName,
                        bookTitle.length() > 28 ? bookTitle.substring(0, 25) + "..." : bookTitle,
                        borrowDate.toString(),
                        status);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving borrow history: " + e.getMessage());
        }
    }
    private static void userLogin(Scanner scanner) {
        System.out.println("\n===== User Login =====");
        while (true) {
            System.out.println("1. New Registration");
            System.out.println("2. Existing User");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the leftover newline

            switch (choice) {
                case 1:
                    newRegistration(scanner);
                    break;
                case 2:
                    existingUser(scanner);
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1,2");
            }
        }
    }
    private static void existingUser(Scanner scanner)
    {
        System.out.println("\n===== existing User =====");
        System.out.print("Enter User Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        if (validateUser(email, password)) {
            System.out.println("User Login Successful!");
            userMenu(scanner, email);
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    private static boolean validateUser(String email, String password) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT * FROM users WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    private static void userMenu(Scanner scanner, String email) {
        while (true) {
            System.out.println("1. View Books\n2. Borrow Book\n3. Return Book\n4. Logout");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    viewBooks();
                    break;
                case 2:
                    borrowBook(scanner, email);
                    break;
                case 3:
                    returnBook(scanner, email);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    private static void addBook(Scanner scanner) {
        System.out.print("Enter Book Title: ");
        String title = scanner.nextLine();
        System.out.print("Enter Author: ");
        String author = scanner.nextLine();
        System.out.print("Enter Quantity: ");
        int quantity = scanner.nextInt();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO books (title, author, quantity) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
            System.out.println("Book added successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewBooks() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT * FROM books";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Title: " + rs.getString("title") + ", Author: " + rs.getString("author") + ", Quantity: " + rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void removeBook(Scanner scanner) {
        System.out.print("Enter Book ID to remove: ");
        String bookId = scanner.nextLine();
        System.out.print("Enter quantity to remove: ");
        String quantity= scanner.nextLine();

        try {
            int bookIdstr = Integer.parseInt(bookId);
            int quantityToRemove = Integer.parseInt(quantity);

            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                // Fetch the current quantity
                String checkQuery = "SELECT quantity FROM books WHERE id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                checkStmt.setInt(1, bookIdstr);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int currentQuantity = rs.getInt("quantity");

                    // Check if requested quantity is valid
                    if (quantityToRemove > currentQuantity || quantityToRemove <= 0) {
                        System.out.println("Enter a valid quantity. Available: " + currentQuantity);
                    } else {
                        // Update the quantity
                        String updateQuery = "UPDATE books SET quantity = quantity - ? WHERE id = ?";
                        PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                        updateStmt.setInt(1, quantityToRemove);
                        updateStmt.setInt(2, bookIdstr);
                        updateStmt.executeUpdate();
                        System.out.println(quantityToRemove + " books removed successfully.");
                    }
                } else {
                    System.out.println("Book ID not found.");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter numeric values for Book ID and Quantity.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    private static void borrowBook(Scanner scanner, String email) {
        System.out.print("Enter Book ID to borrow: ");
        int bookId = scanner.nextInt();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Get user ID from email
            String getUserQuery = "SELECT id FROM users WHERE email = ?";
            PreparedStatement userStmt = conn.prepareStatement(getUserQuery);
            userStmt.setString(1, email);
            ResultSet userRs = userStmt.executeQuery();

            if (userRs.next()) {
                int userId = userRs.getInt("id");

                // Check if book is available
                String checkBookQuery = "SELECT quantity FROM books WHERE id = ?";
                PreparedStatement bookStmt = conn.prepareStatement(checkBookQuery);
                bookStmt.setInt(1, bookId);
                ResultSet bookRs = bookStmt.executeQuery();

                if (bookRs.next() && bookRs.getInt("quantity") > 0) {
                    // Reduce book quantity
                    String updateBookQuery = "UPDATE books SET quantity = quantity - 1 WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateBookQuery);
                    updateStmt.setInt(1, bookId);
                    updateStmt.executeUpdate();

                    // Add entry to borrowed_books table
                    String borrowQuery = "INSERT INTO borrowed_books (user_id, book_id, borrow_date) VALUES (?, ?, CURDATE())";
                    PreparedStatement borrowStmt = conn.prepareStatement(borrowQuery);
                    borrowStmt.setInt(1, userId);
                    borrowStmt.setInt(2, bookId);
                    borrowStmt.executeUpdate();

                    System.out.println("Book borrowed successfully!");
                } else {
                    System.out.println("Book not available.");
                }
            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private static void returnBook(Scanner scanner, String email) {
        System.out.print("Enter Book ID to return: ");
        int bookId = scanner.nextInt();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Get user ID from email
            String getUserQuery = "SELECT id FROM users WHERE email = ?";
            PreparedStatement userStmt = conn.prepareStatement(getUserQuery);
            userStmt.setString(1, email);
            ResultSet userRs = userStmt.executeQuery();

            if (userRs.next()) {
                int userId = userRs.getInt("id");

                // Check if the user has borrowed this book
                String checkBorrowQuery = "SELECT id FROM borrowed_books WHERE user_id = ? AND book_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkBorrowQuery);
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, bookId);
                ResultSet checkRs = checkStmt.executeQuery();

                if (checkRs.next()) {
                    int borrowId = checkRs.getInt("id");

                    // Remove the entry from borrowed_books
                    String deleteBorrowQuery = "DELETE FROM borrowed_books WHERE id = ?";
                    PreparedStatement deleteStmt = conn.prepareStatement(deleteBorrowQuery);
                    deleteStmt.setInt(1, borrowId);
                    deleteStmt.executeUpdate();

                    // Increase book quantity
                    String updateBookQuery = "UPDATE books SET quantity = quantity + 1 WHERE id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateBookQuery);
                    updateStmt.setInt(1, bookId);
                    updateStmt.executeUpdate();

                    System.out.println("Book returned successfully!");
                } else {
                    System.out.println("You have not borrowed this book.");
                }
            } else {
                System.out.println("User not found.");
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
