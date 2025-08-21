import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCounter = 1;

    private int id;
    private String title;
    private String author;
    private boolean isAvailable;

    public Book(String title, String author) {
        this.id = idCounter++;
        this.title = title;
        this.author = author;
        this.isAvailable = true;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public static void updateIdCounter(int count) {
        idCounter = count;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Title: '" + title + "', Author: '" + author + "', Available: " + (isAvailable ? "Yes" : "No");
    }
}

class LibraryManager {
    private List<Book> books;
    private static final String FILE_NAME = "library_data.ser";

    public LibraryManager() {
        this.books = new ArrayList<>();
        loadBooksFromFile();
    }

    public void addBook(Book book) {
        books.add(book);
        saveBooksToFile();
        System.out.println("Book added: " + book.getTitle());
    }

    public void viewAllBooks() {
        if (books.isEmpty()) {
            System.out.println("The library is empty.");
            return;
        }
        System.out.println("\n--- All Books in Library ---");
        books.forEach(System.out::println);
    }

    public void searchBook(String title) {
        Optional<Book> foundBook = books.stream()
                .filter(book -> book.getTitle().equalsIgnoreCase(title))
                .findFirst();

        if (foundBook.isPresent()) {
            System.out.println("Book found: " + foundBook.get());
        } else {
            System.out.println("No book found with the title: " + title);
        }
    }

    public void issueBook(int bookId) {
        findBookById(bookId).ifPresentOrElse(book -> {
            if (book.isAvailable()) {
                book.setAvailable(false);
                saveBooksToFile();
                System.out.println("Successfully issued: " + book.getTitle());
            } else {
                System.out.println("This book is currently unavailable.");
            }
        }, () -> System.out.println("Book with ID " + bookId + " not found."));
    }

    public void returnBook(int bookId) {
        findBookById(bookId).ifPresentOrElse(book -> {
            if (!book.isAvailable()) {
                book.setAvailable(true);
                saveBooksToFile();
                System.out.println("Successfully returned: " + book.getTitle());
            } else {
                System.out.println("This book was not issued.");
            }
        }, () -> System.out.println("Book with ID " + bookId + " not found."));
    }

    private Optional<Book> findBookById(int bookId) {
        return books.stream().filter(book -> book.getId() == bookId).findFirst();
    }

    private void saveBooksToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(books);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadBooksFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            books = (List<Book>) ois.readObject();
            if (!books.isEmpty()) {
                int maxId = books.stream().mapToInt(Book::getId).max().orElse(0);
                Book.updateIdCounter(maxId + 1);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
}

public class Library {
    public static void main(String[] args) {
        LibraryManager manager = new LibraryManager();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Library Management System ---");
            System.out.println("1. Add Book");
            System.out.println("2. View All Books");
            System.out.println("3. Search for a Book");
            System.out.println("4. Issue a Book");
            System.out.println("5. Return a Book");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            String input = scanner.nextLine();
            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter Title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter Author: ");
                    String author = scanner.nextLine();
                    manager.addBook(new Book(title, author));
                    break;
                case 2:
                    manager.viewAllBooks();
                    break;
                case 3:
                    System.out.print("Enter title to search: ");
                    String searchTitle = scanner.nextLine();
                    manager.searchBook(searchTitle);
                    break;
                case 4:
                    System.out.print("Enter Book ID to issue: ");
                    try {
                        int issueId = Integer.parseInt(scanner.nextLine());
                        manager.issueBook(issueId);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format.");
                    }
                    break;
                case 5:
                    System.out.print("Enter Book ID to return: ");
                    try {
                        int returnId = Integer.parseInt(scanner.nextLine());
                        manager.returnBook(returnId);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid ID format.");
                    }
                    break;
                case 6:
                    System.out.println("Exiting system. Goodbye!");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
