import java.sql.*;
import java.util.*;

public class UserManager {
    
    // Check if user exists and password is correct
    public static boolean authenticateUser(String username, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'active'";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return false;
    }
    
    // Get user details
    public static String[] getUserDetails(String username) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT user_id, username, email, role FROM users WHERE username = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new String[]{
                    String.valueOf(rs.getInt("user_id")),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("role")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return null;
    }
    
    // Get all users (Admin only)
    public static List<Map<String, String>> getAllUsers() {
        List<Map<String, String>> users = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT user_id, username, email, role, status FROM users";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> user = new HashMap<>();
                user.put("user_id", String.valueOf(rs.getInt("user_id")));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("role", rs.getString("role"));
                user.put("status", rs.getString("status"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return users;
    }
    
    // Delete user (Admin only)
    public static boolean deleteUser(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "DELETE FROM users WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, null);
        }
        return false;
    }
    
    // Create new user (Admin only)
    public static boolean createUser(String username, String password, String email, String role) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO users (username, password, email, role, status) VALUES (?, ?, ?, ?, 'active')";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, email);
            pstmt.setString(4, role);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, null);
        }
        return false;
    }
    
    // Change user status
    public static boolean changeUserStatus(int userId, String status) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE users SET status = ? WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, null);
        }
        return false;
    }
    
    // Console version - Add User (no parameters)
    public static void addUser() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Username: ");
        String username = sc.nextLine();
        System.out.print("Enter Password: ");
        String password = sc.nextLine();
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Role (admin/user): ");
        String role = sc.nextLine();
        
        if (createUser(username, password, email, role)) {
            System.out.println("✅ User added successfully!");
        } else {
            System.out.println("❌ Failed to add user!");
        }
    }
    
    // Console version - View Users (no parameters)
    public static void viewUsers() {
        List<Map<String, String>> users = getAllUsers();
        
        if (users.isEmpty()) {
            System.out.println("❌ No users found!");
            return;
        }
        
        System.out.println("\n===== ALL USERS =====");
        for (Map<String, String> user : users) {
            System.out.println("\nID: " + user.get("user_id"));
            System.out.println("Username: " + user.get("username"));
            System.out.println("Email: " + user.get("email"));
            System.out.println("Role: " + user.get("role"));
            System.out.println("Status: " + user.get("status"));
        }
        System.out.println("\n====================\n");
    }
        // Get user by ID
    public static Map<String, String> getUserById(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT user_id, username, email, role, status FROM users WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> user = new HashMap<>();
                user.put("user_id", String.valueOf(rs.getInt("user_id")));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("role", rs.getString("role"));
                user.put("status", rs.getString("status"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return null;
    }
    
    // Change password
    public static boolean changePassword(int userId, String oldPassword, String newPassword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            // Verify old password
            String verifyQuery = "SELECT password FROM users WHERE user_id = ?";
            pstmt = conn.prepareStatement(verifyQuery);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String currentPassword = rs.getString("password");
                if (!currentPassword.equals(oldPassword)) {
                    return false; // Old password doesn't match
                }
            } else {
                return false; // User not found
            }
            
            // Update password
            String updateQuery = "UPDATE users SET password = ? WHERE user_id = ?";
            pstmt = conn.prepareStatement(updateQuery);
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return false;
    }
    
    // Delete user account
    public static boolean deleteUserAccount(int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "DELETE FROM users WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, null);
        }
        return false;
    }
    
    // Update user profile
    public static boolean updateUserProfile(int userId, String email) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE users SET email = ? WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setInt(2, userId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, null);
        }
        return false;
    }
}   