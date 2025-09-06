import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
class InvalidTurfOrSlotException extends Exception {
    public InvalidTurfOrSlotException(String msg) { super(msg); }
}
class DuplicateBookingException extends Exception {
    public DuplicateBookingException(String msg) { super(msg); }
}
class MaintenanceException extends Exception {
    public MaintenanceException(String msg) { super(msg); }
}
class AuthenticationException extends Exception {
    public AuthenticationException(String msg) { super(msg); }
}
interface ActionHandler {
    void performActions();
}
abstract class User implements ActionHandler {
    protected String email;
    protected String password;
    protected String name;

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public boolean login(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }
}
class Turf {
    private String id;
    private String name;
    private String location;
    private String status; // Available / Maintenance

    public Turf(String id, String name, String location, String status) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.status = status;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public String getStatus() { return status; }
    public void setStatus(String s) { status = s; }

    @Override
    public String toString() {
        return id + " | " + name + " | " + location + " | " + status;
    }
}
class Slot {
    private String id;
    private String turfId;
    private Date startTime;
    private Date endTime;
    private String status; // Available / Booked

    public Slot(String id, String turfId, Date startTime, Date endTime, String status) {
        this.id = id;
        this.turfId = turfId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public String getId() { return id; }
    public String getTurfId() { return turfId; }
    public Date getStartTime() { return startTime; }
    public Date getEndTime() { return endTime; }
    public String getStatus() { return status; }
    public void setStatus(String s) { status = s; }

    public boolean isAvailable() {
        return "Available".equals(status);
    }

    @Override
    public String toString() {
        return id + " | Turf:" + turfId + " | " + startTime + " - " + endTime + " | " + status;
    }
}
class Booking {
    private String bookingId;
    private Slot slot;
    private Player player;
    private Date bookingDate;

    public Booking(String bookingId, Slot slot, Player player) {
        this.bookingId = bookingId;
        this.slot = slot;
        this.player = player;
        this.bookingDate = new Date();
    }

    public String getBookingId() { return bookingId; }
    public Slot getSlot() { return slot; }
    public Player getPlayer() { return player; }

    @Override
    public String toString() {
        return bookingId + " | Slot: " + slot.getId() + " | Player: " + player.getName();
    }
}


class Player extends User {
    private ArrayList<Booking> myBookings = new ArrayList<>();

    public Player(String email, String password, String name) {
        super(email, password, name);
    }
    public String getName() { return name; }

    private void bookSlot(SessionManager session) {
        System.out.println("\n--- Available Slots ---");
        for (Slot s : session.getSlotList()) if (s.isAvailable()) System.out.println(s);

        System.out.print("Enter Slot ID to book: ");
        String slotId = session.scanner.nextLine().trim();

        try {
            Slot slot = session.findSlotById(slotId);
            if (slot == null) throw new InvalidTurfOrSlotException("Invalid Slot ID.");
            if (!slot.isAvailable()) throw new MaintenanceException("Slot is not available.");

            // Prevent duplicate or overlapping bookings
            for (Booking b : myBookings) {
                Slot booked = b.getSlot();
                if (booked.getId().equals(slotId))
                    throw new DuplicateBookingException("You already booked this slot.");
                if (timesOverlap(booked.getStartTime(), booked.getEndTime(),
                        slot.getStartTime(), slot.getEndTime()))
                    throw new DuplicateBookingException("Overlaps with an existing booking.");
            }

            slot.setStatus("Booked");
            Booking booking = new Booking("B" + (session.getBookingList().size() + 1), slot, this);
            myBookings.add(booking);
            session.getBookingList().add(booking);
            System.out.println("Booking successful: " + booking);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void cancelBooking(SessionManager session) {
        System.out.print("Enter Slot ID to cancel: ");
        String slotId = session.scanner.nextLine().trim();
        Booking toCancel = null;

        for (Booking b : myBookings) {
            if (b.getSlot().getId().equals(slotId)) {
                long now = new Date().getTime();
                long diff = b.getSlot().getStartTime().getTime() - now;
                if (diff > 24 * 60 * 60 * 1000 || diff <= 0) {
                    System.out.println("Error: Can cancel only within 24 hours of start time.");
                    return;
                }
                toCancel = b;
                break;
            }
        }
        if (toCancel == null) {
            System.out.println("Error: No booking found.");
            return;
        }
        toCancel.getSlot().setStatus("Available");
        myBookings.remove(toCancel);
        session.getBookingList().remove(toCancel);
        System.out.println("Booking cancelled: " + toCancel.getBookingId());
    }

    private void viewBookings() {
        if (myBookings.isEmpty()) System.out.println("No bookings.");
        else myBookings.forEach(System.out::println);
    }

    @Override
    public void performActions() {
        SessionManager session = SessionManager.getInstance();
        while (true) {
            System.out.println("\n===== Player Menu =====");
            System.out.println("1. Book Slot");
            System.out.println("2. Cancel Booking");
            System.out.println("3. View My Bookings");
            System.out.println("4. Back");
            String c = session.scanner.nextLine().trim();
            switch (c) {
                case "1": bookSlot(session); break;
                case "2": cancelBooking(session); break;
                case "3": viewBookings(); break;
                case "4": return;
                default: System.out.println("Invalid.");
            }
        }
    }

    private boolean timesOverlap(Date aStart, Date aEnd, Date bStart, Date bEnd) {
        return aStart.before(bEnd) && bStart.before(aEnd);
    }
}
class Coach extends User {
    public Coach(String email, String password, String name) { super(email, password, name); }

    @Override
    public void performActions() {
        SessionManager session = SessionManager.getInstance();
        while (true) {
            System.out.println("\n===== Coach Menu =====");
            System.out.println("1. View All Turfs & Slots");
            System.out.println("2. Schedule Practice");
            System.out.println("3. Back");
            String c = session.scanner.nextLine().trim();
            switch (c) {
                case "1": session.viewAllTurfsAndSlots(); break;
                case "2":
                    System.out.print("Enter Slot ID to schedule practice: ");
                    String slotId = session.scanner.nextLine().trim();
                    try {
                        Slot slot = session.findSlotById(slotId);
                        if (slot == null) throw new InvalidTurfOrSlotException("Invalid Slot ID.");
                        if ("Maintenance".equals(slot.getStatus()))
                            throw new MaintenanceException("Turf under maintenance.");
                        System.out.println("Practice scheduled on Slot " + slot.getId());
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                    break;
                case "3": return;
                default: System.out.println("Invalid.");
            }
        }
    }
}
class TurfAdmin extends User {
    public TurfAdmin(String email, String password, String name) {
        super(email, password, name);
    }

    @Override
    public void performActions() {
        SessionManager session = SessionManager.getInstance();

        while (true) {
            System.out.println("\n===== Turf Admin Menu =====");
            System.out.println("1. Add New Turf Slot (Add/Remove Turf)");
            System.out.println("2. Mark Slot as Under Maintenance");
            System.out.println("3. Back");
            System.out.print("Enter choice: ");

            String choice = session.scanner.nextLine().trim();

            if (choice.equals("1")) {
                manageTurfs(session);
            }
            else if (choice.equals("2")) {
                markTurfMaintenance(session);
            }
            else if (choice.equals("3")) {
                return;
            }
            else {
                System.out.println("Invalid choice. Try again.");
            }
        }
    }
    private void manageTurfs(SessionManager session) {
        System.out.println("\n--- Manage Turfs ---");
        System.out.println("1. Add Turf");
        System.out.println("2. Remove Turf");
        System.out.print("Enter choice: ");
        String option = session.scanner.nextLine().trim();

        if (option.equals("1")) {
            // Add new turf
            System.out.print("Enter Turf Name: ");
            String name = session.scanner.nextLine().trim();
            System.out.print("Enter Location: ");
            String location = session.scanner.nextLine().trim();

            String turfId = "T" + (session.getTurfList().size() + 1); // auto ID
            Turf newTurf = new Turf(turfId, name, location, "Available");
            session.getTurfList().add(newTurf);

            System.out.println("New Turf added: " + newTurf);
        } else if (option.equals("2")) {
            // Remove an existing turf
            session.viewAllTurfs();
            System.out.print("Enter Turf ID to Remove: ");
            String id = session.scanner.nextLine().trim();

            Turf toRemove = session.findTurfById(id);
            if (toRemove != null) {
                session.getTurfList().remove(toRemove);
                System.out.println("Turf removed successfully.");
            } else {
                System.out.println("Error: Invalid Turf ID.");
            }
        } else {
            System.out.println("Invalid choice. Going back.");
        }
    }
    private void markTurfMaintenance(SessionManager session) {
        session.viewAllTurfs();
        System.out.print("Enter Turf ID: ");
        String turfId = session.scanner.nextLine().trim();

        Turf t = session.findTurfById(turfId);
        if (t == null) {
            System.out.println(" Error: Invalid Turf ID.");
            return;
        }

        t.setStatus("Maintenance");
        System.out.println("Turf marked under maintenance: " + t);
    }
}

class SessionManager {
    private static SessionManager instance;
    private ArrayList<User> userList = new ArrayList<>();
    private ArrayList<Turf> turfList = new ArrayList<>();
    private ArrayList<Slot> slotList = new ArrayList<>();
    private ArrayList<Booking> bookingList = new ArrayList<>();
    public Scanner scanner = new Scanner(System.in);

    private SessionManager() { initializeData(); }
    public static synchronized SessionManager getInstance() {
        if (instance==null) instance=new SessionManager(); return instance;
    }

    private void initializeData() {
        userList.add(new Player("player1@turfsys.com","pass","Kinga"));
        userList.add(new Coach("coach1@turfsys.com","pass","Tabassum"));
        userList.add(new TurfAdmin("manager1@turfsys.com","pass","Messi"));

        turfList.add(new Turf("T1","Main Field","Stadium A","Available"));
        turfList.add(new Turf("T2","Training Field","Stadium B","Maintenance"));

        Date now = new Date();
        slotList.add(new Slot("S1","T1",new Date(now.getTime()+7200000), new Date(now.getTime()+10800000),"Available"));
        slotList.add(new Slot("S2","T2",new Date(now.getTime()+14400000), new Date(now.getTime()+18000000),"Booked"));
    }

    public ArrayList<User> getUserList(){return userList;}
    public ArrayList<Turf> getTurfList(){return turfList;}
    public ArrayList<Slot> getSlotList(){return slotList;}
    public ArrayList<Booking> getBookingList(){return bookingList;}

    public User authenticate(String e,String p) throws AuthenticationException {
        for (User u:userList) if (u.login(e,p)) return u;
        throw new AuthenticationException("Invalid email or password.");
    }

    public Slot findSlotById(String id){
        for(Slot s:slotList) if(s.getId().equals(id)) return s;
        return null;
    }
    public Turf findTurfById(String id){
        for(Turf t:turfList) if(t.getId().equals(id)) return t;
        return null;
    }

    public void viewAllTurfs(){
        System.out.println("\n--- All Turfs ---");
        for(Turf t:turfList) System.out.println(t);
    }

    public void viewAllTurfsAndSlots(){
        System.out.println("\n--- All Turfs & Slots ---");
        for(Turf t:turfList){
            System.out.println(t);
            for(Slot s:slotList) {
                if(s.getTurfId().equals(t.getId())) System.out.println("   "+s);
            }
        }
    }
}


