import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

// ─────────────────────────────────────────
//  Contact Model Class
// ─────────────────────────────────────────
class Contact implements Comparable<Contact> {
    private String id;
    private String name;
    private String phone;
    private String email;
    private LocalDate birthday;
    private String category;
    private LocalDateTime addedOn;
    
    // Static counter for IDs
    private static int idCounter = 1;
    
    public Contact(String name, String phone, 
                   String email, LocalDate birthday, 
                   String category) {
        this.id = "C" + String.format("%04d", idCounter++);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.birthday = birthday;
        this.category = category;
        this.addedOn = LocalDateTime.now();
    }
    
    // Constructor for loading from file
    public Contact(String id, String name, String phone,
                   String email, LocalDate birthday,
                   String category, LocalDateTime addedOn) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.birthday = birthday;
        this.category = category;
        this.addedOn = addedOn;
    }
    
    // Getters
    public String getId()        { return id; }
    public String getName()      { return name; }
    public String getPhone()     { return phone; }
    public String getEmail()     { return email; }
    public LocalDate getBirthday() { return birthday; }
    public String getCategory()  { return category; }
    public LocalDateTime getAddedOn() { return addedOn; }
    
    // Setters
    public void setName(String name)     { this.name = name; }
    public void setPhone(String phone)   { this.phone = phone; }
    public void setEmail(String email)   { this.email = email; }
    public void setBirthday(LocalDate b) { this.birthday = b; }
    public void setCategory(String cat)  { this.category = cat; }
    
    // Calculate age
    public int getAge() {
        if (birthday == null) return -1;
        return Period.between(birthday, LocalDate.now()).getYears();
    }
    
    // Days until next birthday
    public long daysUntilBirthday() {
        if (birthday == null) return -1;
        
        LocalDate today = LocalDate.now();
        LocalDate nextBirthday = birthday.withYear(today.getYear());
        
        if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            nextBirthday = nextBirthday.plusYears(1);
        }
        
        return today.until(nextBirthday, java.time.temporal.ChronoUnit.DAYS);
    }
    
    // Convert to CSV for saving
    public String toCSV() {
        DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter dtFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return String.join("|",
            id,
            name,
            phone,
            email == null ? "" : email,
            birthday == null ? "" : birthday.format(dateFmt),
            category,
            addedOn.format(dtFmt)
        );
    }
    
    // Parse from CSV
    public static Contact fromCSV(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 7) return null;
        
        try {
            DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
            DateTimeFormatter dtFmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            
            LocalDate bday = parts[4].isEmpty() ? null :
                             LocalDate.parse(parts[4], dateFmt);
            LocalDateTime added = LocalDateTime.parse(parts[6], dtFmt);
            
            return new Contact(
                parts[0], parts[1], parts[2],
                parts[3].isEmpty() ? null : parts[3],
                bday, parts[5], added
            );
        } catch (Exception e) {
            System.out.println("⚠️  Skipping malformed line: " + line);
            return null;
        }
    }
    
    // Natural sort by name
    @Override
    public int compareTo(Contact other) {
        return this.name.compareToIgnoreCase(other.name);
    }
    
    // Display full contact card
    public void displayCard() {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter dtFmt  = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.printf("║  👤 %-40s║%n", name);
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.printf("║  ID:       %-34s║%n", id);
        System.out.printf("║  📞 Phone:  %-34s║%n", phone);
        System.out.printf("║  📧 Email:  %-34s║%n",
            email != null ? email : "Not provided");
        System.out.printf("║  🎂 Birth:  %-34s║%n",
            birthday != null ? birthday.format(dateFmt) +
            " (Age: " + getAge() + ")" : "Not provided");
        System.out.printf("║  📁 Cat:    %-34s║%n", category);
        System.out.printf("║  ➕ Added:  %-34s║%n", addedOn.format(dtFmt));
        
        if (birthday != null) {
            long days = daysUntilBirthday();
            String bMsg = days == 0 ? "🎉 Birthday is TODAY!" :
                          days == 1 ? "🎁 Birthday TOMORROW!" :
                          "🎂 Birthday in " + days + " days";
            System.out.printf("║  %-44s║%n", bMsg);
        }
        System.out.println("╚══════════════════════════════════════════════╝");
    }
    
    // Display compact row
    public void displayRow(int index) {
        System.out.printf("║ %-3d │ %-18s │ %-14s │ %-12s │ %-8s ║%n",
            index,
            truncate(name, 18),
            phone,
            truncate(email != null ? email : "-", 12),
            category
        );
    }
    
    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 2) + ".." : s;
    }
}

// ─────────────────────────────────────────
//  Validator Class
// ─────────────────────────────────────────
class Validator {
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) return false;
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\+]", "");
        return cleaned.matches("\\d{10,12}");
    }
    
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) return true; // Optional
        return email.matches("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}");
    }
    
    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.length() >= 2;
    }
    
    public static String formatPhone(String phone) {
        String cleaned = phone.replaceAll("[\\s\\-\\(\\)\\+]", "");
        if (cleaned.length() == 10) {
            return cleaned.substring(0, 5) + "-" + cleaned.substring(5);
        }
        return cleaned;
    }
}

// ─────────────────────────────────────────
//  ContactManager Class
// ─────────────────────────────────────────
class ContactManager {
    private ArrayList<Contact> contacts;
    private static final String DATA_FILE = "contacts.dat";
    
    public ContactManager() {
        contacts = new ArrayList<>();
        loadFromFile();
    }
    
    // ── CRUD Operations ──────────────────
    
    public boolean addContact(Contact contact) {
        // Check duplicate phone
        for (Contact c : contacts) {
            if (c.getPhone().equals(contact.getPhone())) {
                System.out.println("❌ Phone number already exists!");
                return false;
            }
        }
        contacts.add(contact);
        saveToFile();
        return true;
    }
    
    public boolean deleteContact(String id) {
        Contact toRemove = findById(id);
        if (toRemove != null) {
            contacts.remove(toRemove);
            saveToFile();
            return true;
        }
        return false;
    }
    
    public boolean updateContact(String id, String field, String value) {
        Contact contact = findById(id);
        if (contact == null) return false;
        
        switch (field.toLowerCase()) {
            case "name":
                if (!Validator.isValidName(value)) {
                    System.out.println("❌ Invalid name!");
                    return false;
                }
                contact.setName(value);
                break;
            case "phone":
                if (!Validator.isValidPhone(value)) {
                    System.out.println("❌ Invalid phone!");
                    return false;
                }
                contact.setPhone(Validator.formatPhone(value));
                break;
            case "email":
                if (!Validator.isValidEmail(value)) {
                    System.out.println("❌ Invalid email!");
                    return false;
                }
                contact.setEmail(value.isBlank() ? null : value);
                break;
            case "category":
                contact.setCategory(value);
                break;
            default:
                return false;
        }
        
        saveToFile();
        return true;
    }
    
    // ── Search Operations ────────────────
    
    public ArrayList<Contact> searchByName(String query) {
        ArrayList<Contact> results = new ArrayList<>();
        String q = query.toLowerCase().trim();
        for (Contact c : contacts) {
            if (c.getName().toLowerCase().contains(q)) {
                results.add(c);
            }
        }
        return results;
    }
    
    public ArrayList<Contact> searchByPhone(String query) {
        ArrayList<Contact> results = new ArrayList<>();
        for (Contact c : contacts) {
            if (c.getPhone().contains(query.trim())) {
                results.add(c);
            }
        }
        return results;
    }
    
    public ArrayList<Contact> searchByEmail(String query) {
        ArrayList<Contact> results = new ArrayList<>();
        String q = query.toLowerCase().trim();
        for (Contact c : contacts) {
            if (c.getEmail() != null && 
                c.getEmail().toLowerCase().contains(q)) {
                results.add(c);
            }
        }
        return results;
    }
    
    public ArrayList<Contact> searchAll(String query) {
        // Search across name, phone, email
        HashSet<String> seen = new HashSet<>();
        ArrayList<Contact> results = new ArrayList<>();
        
        for (Contact c : contacts) {
            String q = query.toLowerCase().trim();
            if (!seen.contains(c.getId()) && (
                c.getName().toLowerCase().contains(q) ||
                c.getPhone().contains(q) ||
                (c.getEmail() != null && 
                 c.getEmail().toLowerCase().contains(q)))) {
                results.add(c);
                seen.add(c.getId());
            }
        }
        return results;
    }
    
    public Contact findById(String id) {
        for (Contact c : contacts) {
            if (c.getId().equalsIgnoreCase(id)) return c;
        }
        return null;
    }
    
    public ArrayList<Contact> filterByCategory(String category) {
        ArrayList<Contact> results = new ArrayList<>();
        for (Contact c : contacts) {
            if (c.getCategory().equalsIgnoreCase(category)) {
                results.add(c);
            }
        }
        return results;
    }
    
    // ── Birthday Operations ──────────────
    
    public ArrayList<Contact> getUpcomingBirthdays(int days) {
        ArrayList<Contact> results = new ArrayList<>();
        for (Contact c : contacts) {
            if (c.getBirthday() != null) {
                long daysUntil = c.daysUntilBirthday();
                if (daysUntil <= days) {
                    results.add(c);
                }
            }
        }
        results.sort(Comparator.comparingLong(Contact::daysUntilBirthday));
        return results;
    }
    
    // ── Display Operations ───────────────
    
    public void displayAll(String sortBy) {
        if (contacts.isEmpty()) {
            System.out.println("\n📭 No contacts found!");
            return;
        }
        
        ArrayList<Contact> sorted = new ArrayList<>(contacts);
        
        switch (sortBy.toLowerCase()) {
            case "name":
                Collections.sort(sorted);
                break;
            case "category":
                sorted.sort(Comparator.comparing(Contact::getCategory));
                break;
            case "recent":
                sorted.sort(Comparator.comparing(
                    Contact::getAddedOn).reversed());
                break;
        }
        
        System.out.println("\n╔═════╤════════════════════╤");
        System.out.println("║ No. │ Name               │");
        System.out.println("║     │                    │");
        System.out.printf("║ %-3s │ %-18s │ %-14s │ %-12s │ %-8s ║%n",
            "#", "Name", "Phone", "Email", "Category");
        System.out.println("╠═════╪════════════════════╪" +
            "════════════════╪══════════════╪══════════╣");
        
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).displayRow(i + 1);
        }
        
        System.out.println("╠═════╧════════════════════╧" +
            "════════════════╧══════════════╧══════════╣");
        System.out.printf("║  Total Contacts: %-38d║%n", contacts.size());
        System.out.println("╚═══════════════════════════" +
            "════════════════════════════════════════╝");
    }
    
    public void displayResults(ArrayList<Contact> results, String title) {
        System.out.println("\n=== " + title + " (" + results.size() + " found) ===");
        if (results.isEmpty()) {
            System.out.println("No results found");
            return;
        }
        for (int i = 0; i < results.size(); i++) {
            System.out.printf("%d. %s | %s | %s%n",
                i + 1,
                results.get(i).getName(),
                results.get(i).getPhone(),
                results.get(i).getCategory()
            );
        }
    }
    
    // ── Statistics ───────────────────────
    
    public void displayStatistics() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║           CONTACT STATISTICS              ║");
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.printf("║  Total Contacts:    %-23d║%n", contacts.size());
        
        // Count by category
        HashMap<String, Integer> catCount = new HashMap<>();
        int withEmail = 0;
        int withBirthday = 0;
        
        for (Contact c : contacts) {
            catCount.put(c.getCategory(),
                catCount.getOrDefault(c.getCategory(), 0) + 1);
            if (c.getEmail() != null) withEmail++;
            if (c.getBirthday() != null) withBirthday++;
        }
        
        System.out.printf("║  With Email:        %-23d║%n", withEmail);
        System.out.printf("║  With Birthday:     %-23d║%n", withBirthday);
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.println("║  By Category:                             ║");
        
        for (Map.Entry<String, Integer> entry : catCount.entrySet()) {
            System.out.printf("║    %-15s: %-23d║%n",
                entry.getKey(), entry.getValue());
        }
        
        // Upcoming birthdays
        ArrayList<Contact> upcoming = getUpcomingBirthdays(30);
        System.out.println("╠════════════════════════════════════════════╣");
        System.out.printf("║  Birthdays (next 30 days): %-15d║%n",
            upcoming.size());
        System.out.println("╚════════════════════════════════════════════╝");
    }
    
    // ── File Operations ──────────────────
    
    public void saveToFile() {
        try (PrintWriter writer = new PrintWriter(
                new FileWriter(DATA_FILE))) {
            for (Contact c : contacts) {
                writer.println(c.toCSV());
            }
        } catch (IOException e) {
            System.out.println("⚠️  Error saving: " + e.getMessage());
        }
    }
    
    public void loadFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        
        try (BufferedReader reader = new BufferedReader(
                new FileReader(DATA_FILE))) {
            String line;
            int loaded = 0;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    Contact c = Contact.fromCSV(line);
                    if (c != null) {
                        contacts.add(c);
                        loaded++;
                    }
                }
            }
            if (loaded > 0) {
                System.out.println("✅ Loaded " + loaded + " contacts");
            }
        } catch (IOException e) {
            System.out.println("⚠️  Error loading: " + e.getMessage());
        }
    }
    
    public void exportToCSV(String filename) {
        try (PrintWriter writer = new PrintWriter(
                new FileWriter(filename))) {
            // Header
            writer.println("ID,Name,Phone,Email,Birthday,Category");
            
            ArrayList<Contact> sorted = new ArrayList<>(contacts);
            Collections.sort(sorted);
            
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Contact c : sorted) {
                writer.printf("%s,%s,%s,%s,%s,%s%n",
                    c.getId(),
                    c.getName(),
                    c.getPhone(),
                    c.getEmail() != null ? c.getEmail() : "",
                    c.getBirthday() != null ? 
                        c.getBirthday().format(fmt) : "",
                    c.getCategory()
                );
            }
            System.out.println("✅ Exported " + contacts.size() +
                               " contacts to " + filename);
        } catch (IOException e) {
            System.out.println("❌ Export failed: " + e.getMessage());
        }
    }
    
    public int getTotalContacts() {
        return contacts.size();
    }
}

// ─────────────────────────────────────────
//  Main Application
// ─────────────────────────────────────────
public class ContactApp {
    
    private static ContactManager manager = new ContactManager();
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        // Add sample data if empty
        if (manager.getTotalContacts() == 0) {
            loadSampleData();
        }
        
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║        CONTACT MANAGEMENT APP               ║");
        System.out.println("║              Version 1.0                    ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        
        while (true) {
            showMainMenu();
            int choice = getIntInput("Enter choice: ");
            
            switch (choice) {
                case 1 -> handleAddContact();
                case 2 -> handleViewContacts();
                case 3 -> handleSearchContacts();
                case 4 -> handleUpdateContact();
                case 5 -> handleDeleteContact();
                case 6 -> handleBirthdays();
                case 7 -> handleExport();
                case 8 -> manager.displayStatistics();
                case 9 -> {
                    System.out.println("\n👋 Goodbye! Contacts saved.");
                    scanner.close();
                    return;
                }
                default -> System.out.println("❌ Invalid choice!");
            }
            
            System.out.print("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }
    
    // ── Menu ─────────────────────────────
    
    static void showMainMenu() {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║                 MAIN MENU                   ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  1. ➕ Add Contact                          ║");
        System.out.println("║  2. 📋 View All Contacts                    ║");
        System.out.println("║  3. 🔍 Search Contacts                      ║");
        System.out.println("║  4. ✏️  Update Contact                      ║");
        System.out.println("║  5. 🗑️  Delete Contact                      ║");
        System.out.println("║  6. 🎂 Birthday Reminders                   ║");
        System.out.println("║  7. 📤 Export Contacts                      ║");
        System.out.println("║  8. 📊 Statistics                           ║");
        System.out.println("║  9. 🚪 Exit                                 ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }
    
    // ── Handlers ─────────────────────────
    
    static void handleAddContact() {
        System.out.println("\n=== ADD NEW CONTACT ===");
        
        // Name
        String name;
        do {
            name = getStringInput("Full Name: ");
            if (!Validator.isValidName(name)) {
                System.out.println("❌ Name must be at least 2 characters");
            }
        } while (!Validator.isValidName(name));
        
        // Phone
        String phone;
        do {
            phone = getStringInput("Phone Number: ");
            if (!Validator.isValidPhone(phone)) {
                System.out.println("❌ Invalid phone (10-12 digits)");
            }
        } while (!Validator.isValidPhone(phone));
        phone = Validator.formatPhone(phone);
        
        // Email (optional)
        String email;
        do {
            email = getStringInput("Email (optional, press Enter to skip): ");
            if (!email.isBlank() && !Validator.isValidEmail(email)) {
                System.out.println("❌ Invalid email format");
            }
        } while (!email.isBlank() && !Validator.isValidEmail(email));
        
        // Birthday (optional)
        LocalDate birthday = null;
        String bdInput = getStringInput(
            "Birthday (dd/MM/yyyy, optional, Enter to skip): ");
        if (!bdInput.isBlank()) {
            try {
                birthday = LocalDate.parse(bdInput,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception e) {
                System.out.println("⚠️  Invalid date, skipping birthday");
            }
        }
        
        // Category
        System.out.println("Categories: Family | Friend | Work | Other");
        String category = getStringInput("Category: ");
        if (category.isBlank()) category = "Other";
        
        Contact contact = new Contact(
            name, phone,
            email.isBlank() ? null : email,
            birthday, category
        );
        
        if (manager.addContact(contact)) {
            System.out.println("✅ Contact added successfully!");
            contact.displayCard();
        }
    }
    
    static void handleViewContacts() {
        System.out.println("\n=== VIEW CONTACTS ===");
        System.out.println("Sort by: 1.Name  2.Category  3.Recently Added");
        int sort = getIntInput("Choose sort: ");
        
        String sortBy = switch (sort) {
            case 2 -> "category";
            case 3 -> "recent";
            default -> "name";
        };
        
        manager.displayAll(sortBy);
        
        // Option to view details
        String id = getStringInput(
            "\nEnter contact ID to view details (or Enter to skip): ");
        if (!id.isBlank()) {
            Contact c = manager.findById(id.toUpperCase());
            if (c != null) {
                c.displayCard();
            } else {
                System.out.println("❌ Contact not found");
            }
        }
    }
    
    static void handleSearchContacts() {
        System.out.println("\n=== SEARCH CONTACTS ===");
        System.out.println("1. Search by Name");
        System.out.println("2. Search by Phone");
        System.out.println("3. Search by Email");
        System.out.println("4. Search All Fields");
        System.out.println("5. Filter by Category");
        
        int choice = getIntInput("Choose: ");
        String query = getStringInput("Enter search term: ");
        
        ArrayList<Contact> results = switch (choice) {
            case 1 -> manager.searchByName(query);
            case 2 -> manager.searchByPhone(query);
            case 3 -> manager.searchByEmail(query);
            case 5 -> manager.filterByCategory(query);
            default -> manager.searchAll(query);
        };
        
        manager.displayResults(results, "Search Results");
        
        if (!results.isEmpty()) {
            String id = getStringInput(
                "\nEnter ID for full details (or Enter to skip): ");
            if (!id.isBlank()) {
                Contact c = manager.findById(id.toUpperCase());
                if (c != null) c.displayCard();
            }
        }
    }
    
    static void handleUpdateContact() {
        System.out.println("\n=== UPDATE CONTACT ===");
        
        String id = getStringInput("Enter Contact ID: ");
        Contact contact = manager.findById(id.toUpperCase());
        
        if (contact == null) {
            System.out.println("❌ Contact not found");
            return;
        }
        
        contact.displayCard();
        
        System.out.println("\nWhat to update?");
        System.out.println("1. Name   2. Phone   3. Email   4. Category");
        
        int field = getIntInput("Choose field: ");
        String fieldName = switch (field) {
            case 1 -> "name";
            case 2 -> "phone";
            case 3 -> "email";
            case 4 -> "category";
            default -> "";
        };
        
        if (fieldName.isEmpty()) {
            System.out.println("❌ Invalid field");
            return;
        }
        
        String newValue = getStringInput("Enter new " + fieldName + ": ");
        
        if (manager.updateContact(id.toUpperCase(), fieldName, newValue)) {
            System.out.println("✅ Contact updated!");
            manager.findById(id.toUpperCase()).displayCard();
        }
    }
    
    static void handleDeleteContact() {
        System.out.println("\n=== DELETE CONTACT ===");
        
        String id = getStringInput("Enter Contact ID to delete: ");
        Contact contact = manager.findById(id.toUpperCase());
        
        if (contact == null) {
            System.out.println("❌ Contact not found");
            return;
        }
        
        contact.displayCard();
        
        String confirm = getStringInput(
            "⚠️  Delete " + contact.getName() + "? (yes/no): ");
        
        if (confirm.equalsIgnoreCase("yes")) {
            if (manager.deleteContact(id.toUpperCase())) {
                System.out.println("✅ Contact deleted successfully");
            }
        } else {
            System.out.println("❌ Deletion cancelled");
        }
    }
    
    static void handleBirthdays() {
        System.out.println("\n=== BIRTHDAY REMINDERS ===");
        System.out.println("1. Upcoming (next 7 days)");
        System.out.println("2. Upcoming (next 30 days)");
        System.out.println("3. This month");
        
        int choice = getIntInput("Choose: ");
        int days = switch (choice) {
            case 1 -> 7;
            case 3 -> LocalDate.now()
                .lengthOfMonth() - LocalDate.now().getDayOfMonth();
            default -> 30;
        };
        
        ArrayList<Contact> upcoming = manager.getUpcomingBirthdays(days);
        
        System.out.println("\n🎂 Upcoming Birthdays (next " + days + " days):");
        
        if (upcoming.isEmpty()) {
            System.out.println("No upcoming birthdays!");
            return;
        }
        
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");
        
        for (Contact c : upcoming) {
            long daysLeft = c.daysUntilBirthday();
            String msg = daysLeft == 0 ? "🎉 TODAY!" :
                         daysLeft == 1 ? "🎁 Tomorrow" :
                         "In " + daysLeft + " days";
            
            System.out.printf("  %-20s │ %s │ Age: %d │ %s%n",
                c.getName(),
                c.getBirthday().format(fmt),
                c.getAge() + 1,  // Age they'll turn
                msg
            );
        }
    }
    
    static void handleExport() {
        System.out.println("\n=== EXPORT CONTACTS ===");
        String filename = getStringInput(
            "Enter filename (e.g. contacts.csv): ");
        if (filename.isBlank()) filename = "contacts_export.csv";
        manager.exportToCSV(filename);
    }
    
    // ── Load Sample Data ─────────────────
    
    static void loadSampleData() {
        System.out.println("📥 Loading sample contacts...");
        
        manager.addContact(new Contact(
            "Alice Johnson", "98765-43210",
            "alice@example.com",
            LocalDate.of(1995, 3, 15), "Friend"));
        
        manager.addContact(new Contact(
            "Bob Smith", "87654-32109",
            "bob@work.com",
            LocalDate.of(1988, LocalDate.now().getMonthValue(),
                Math.min(LocalDate.now().getDayOfMonth() + 3,
                    LocalDate.now().lengthOfMonth())),
            "Work"));
        
        manager.addContact(new Contact(
            "Charlie Brown", "76543-21098",
            null,
            LocalDate.of(2000, 12, 25), "Family"));
        
        manager.addContact(new Contact(
            "Diana Prince", "65432-10987",
            "diana@email.com",
            LocalDate.of(LocalDate.now().getYear() - 28,
                LocalDate.now().getMonthValue(),
                LocalDate.now().getDayOfMonth()),
            "Friend"));
        
        System.out.println("✅ Sample data loaded!");
    }
    
    // ── Helper Methods ───────────────────
    
    static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
    
    static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                int val = Integer.parseInt(scanner.nextLine().trim());
                return val;
            } catch (NumberFormatException e) {
                System.out.println("❌ Please enter a number");
            }
        }
    }
}
