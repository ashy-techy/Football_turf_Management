public class FootballTurfManagement {
    public static void main(String[] args) {
        SessionManager session = SessionManager.getInstance();
        System.out.println("=== Football Turf Management System ===");

        while (true) {
            System.out.print("\nEmail: ");
            String email = session.scanner.nextLine().trim();
            System.out.print("Password: ");
            String password = session.scanner.nextLine().trim();

            try {
                User currentUser = session.authenticate(email, password);
                System.out.println("Welcome " + currentUser.name + "!");

                boolean loggedIn = true;
                while (loggedIn) {
                    System.out.println("\n===== Main Menu =====");
                    System.out.println("1. View Available Turfs");
                    System.out.println("2. Book a Slot (Player Only)");
                    System.out.println("3. Schedule Practice (Coach Only)");
                    System.out.println("4. Manage Turfs (Manager Only)");
                    System.out.println("5. Logout");
                    System.out.print("Enter choice: ");
                    String choice = session.scanner.nextLine().trim();

                    switch (choice) {
                        case "1":
                            session.viewAllTurfsAndSlots();
                            break;
                        case "2":
                            if (currentUser instanceof Player) {
                                currentUser.performActions();
                            } else {
                                System.out.println("Access Denied: Only Players can book slots.");
                            }
                            break;
                        case "3":
                            if (currentUser instanceof Coach) {
                                currentUser.performActions();
                            } else {
                                System.out.println("Access Denied: Only Coaches can schedule practice.");
                            }
                            break;
                        case "4":
                            if (currentUser instanceof TurfAdmin) {
                                currentUser.performActions();
                            } else {
                                System.out.println("Access Denied: Only Managers can manage turfs.");
                            }
                            break;
                        case "5":
                            System.out.println("Logging out...");
                            loggedIn = false;
                            break;
                        default:
                            System.out.println("Invalid choice. Try again.");
                    }
                }

            } catch (AuthenticationException e) {
                System.out.println("Login Failed: " + e.getMessage());
            }
        }
    }
}
