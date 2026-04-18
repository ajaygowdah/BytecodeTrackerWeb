import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Login {

    public static String loginUser() {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Username: ");
        String username = sc.nextLine();

        System.out.print("Enter Password: ");
        String password = sc.nextLine();

        String role = null;

        try {

            Connection conn = DBConnection.getConnection();

            String query = "SELECT role FROM users WHERE username=? AND password=?";

            PreparedStatement pst = conn.prepareStatement(query);

            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if(rs.next()) {

                role = rs.getString("role");
                System.out.println("Login Successful!");
                System.out.println("Role: " + role);

            } 
            else {

                System.out.println("Invalid Username or Password!");

            }

        } 
        catch(Exception e) {

            e.printStackTrace();

        }

        return role;
    }
}