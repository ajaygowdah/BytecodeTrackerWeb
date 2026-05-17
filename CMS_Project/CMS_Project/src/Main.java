import java.sql.Connection;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        System.out.println("=================================");
        System.out.println("   CONTENT MANAGEMENT SYSTEM");
        System.out.println("=================================");

        Connection conn = DBConnection.getConnection();

        if (conn != null) {

            String role = Login.loginUser();

            if (role == null) {

                System.out.println("Login Failed!");

            }

            else if (role.equals("admin")) {

                System.out.println("Welcome Admin!");

                int choice;

                do {

                    System.out.println("\n----- ADMIN MENU -----");
                    System.out.println("1. Add Content");
                    System.out.println("2. View Content");
                    System.out.println("3. Delete Content");
                    System.out.println("4. Add User");
                    System.out.println("5. View Users");
                    System.out.println("6. Exit");

                    System.out.print("Enter Choice: ");
                    choice = sc.nextInt();

                    switch (choice) {

                        case 1:
                            ContentManager.addContent();
                            break;

                        case 2:
                            ContentManager.viewContent();
                            break;

                        case 3:
                            ContentManager.deleteContent();
                            break;

                        case 4:
                            UserManager.addUser();
                            break;

                        case 5:
                            UserManager.viewUsers();
                            break;

                        case 6:
                            System.out.println("Exiting...");
                            break;

                        default:
                            System.out.println("Invalid Choice!");

                    }

                } while (choice != 6);

            }

            else if (role.equals("user")) {

                System.out.println("Welcome User!");

                int choice;

                do {

                    System.out.println("\n----- USER MENU -----");
                    System.out.println("1. Add Content");
                    System.out.println("2. View Content");
                    System.out.println("3. Exit");

                    System.out.print("Enter Choice: ");
                    choice = sc.nextInt();

                    switch (choice) {

                        case 1:
                            ContentManager.addContent();
                            break;

                        case 2:
                            ContentManager.viewContent();
                            break;

                        case 3:
                            System.out.println("Exiting...");
                            break;

                        default:
                            System.out.println("Invalid Choice!");

                    }

                } while (choice != 3);

            }

        }

        else {

            System.out.println("CMS Failed to Start.");

        }

    }

}