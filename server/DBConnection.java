package server;

import java.sql.*;
import java.util.*;

public class DBConnection {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/bytecode_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root@123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public static boolean registerUser(String username, String email, String password) {
        try {
            Connection conn = getConnection();
            String query = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);

            ps.executeUpdate();
            ps.close();
            conn.close();
            return true;

        } catch (SQLException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    public static int getUserId(String username, String password) {
        try {
            Connection conn = getConnection();
            String query = "SELECT id FROM users WHERE username = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(query);

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                rs.close();
                ps.close();
                conn.close();
                return id;
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }

        return -1;
    }

    public static int createScan(int userId, String fileName) {
        try {
            Connection conn = getConnection();
            String query = "INSERT INTO scans (user_id, file_name) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            ps.setInt(1, userId);
            ps.setString(2, fileName);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            int scanId = 0;

            if (rs.next()) {
                scanId = rs.getInt(1);
            }

            rs.close();
            ps.close();
            conn.close();

            return scanId;

        } catch (SQLException e) {
            System.err.println("Scan creation error: " + e.getMessage());
            return -1;
        }
    }

    public static String getUsernameById(int userId) {
        try {
            Connection conn = getConnection();
            String query = "SELECT username FROM users WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String username = rs.getString("username");
                rs.close();
                ps.close();
                conn.close();
                return username;
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "User";
    }

    public static List<ScanRecord> getUserScans(int userId) {
        List<ScanRecord> scans = new ArrayList<>();
        try {
            Connection conn = getConnection();
            String query = "SELECT s.id, s.file_name, s.created_at, COUNT(m.id) as method_count, " +
                          "SUM(CASE WHEN m.status='Dangerous' THEN 1 ELSE 0 END) as dangerous_count " +
                          "FROM scans s " +
                          "LEFT JOIN classes c ON s.id = c.scan_id " +
                          "LEFT JOIN methods m ON c.id = m.class_id " +
                          "WHERE s.user_id = ? " +
                          "GROUP BY s.id " +
                          "ORDER BY s.created_at DESC";
            
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                ScanRecord record = new ScanRecord();
                record.scanId = rs.getInt("id");
                record.fileName = rs.getString("file_name");
                record.createdAt = rs.getString("created_at");
                record.methodCount = rs.getInt("method_count");
                record.dangerousCount = rs.getInt("dangerous_count");
                scans.add(record);
            }
            
            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scans;
    }

    public static int getTotalScans(int userId) {
        try {
            Connection conn = getConnection();
            String query = "SELECT COUNT(*) as count FROM scans WHERE user_id = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("count");
                rs.close();
                ps.close();
                conn.close();
                return count;
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int getTotalDangerousMethods(int userId) {
        try {
            Connection conn = getConnection();
            String query = "SELECT COUNT(m.id) as count FROM methods m " +
                          "JOIN classes c ON m.class_id = c.id " +
                          "JOIN scans s ON c.scan_id = s.id " +
                          "WHERE s.user_id = ? AND (m.status='Dangerous' OR m.status='Suspicious')";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("count");
                rs.close();
                ps.close();
                conn.close();
                return count;
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void deleteScan(int scanId) {
        try {
            Connection conn = getConnection();
            
            // Delete violations
            String violationQuery = "DELETE FROM violations WHERE method_id IN " +
                                   "(SELECT id FROM methods WHERE class_id IN " +
                                   "(SELECT id FROM classes WHERE scan_id = ?))";
            PreparedStatement violationPs = conn.prepareStatement(violationQuery);
            violationPs.setInt(1, scanId);
            violationPs.executeUpdate();
            violationPs.close();
            
            // Delete methods
            String methodQuery = "DELETE FROM methods WHERE class_id IN " +
                                "(SELECT id FROM classes WHERE scan_id = ?)";
            PreparedStatement methodPs = conn.prepareStatement(methodQuery);
            methodPs.setInt(1, scanId);
            methodPs.executeUpdate();
            methodPs.close();
            
            // Delete classes
            String classQuery = "DELETE FROM classes WHERE scan_id = ?";
            PreparedStatement classPs = conn.prepareStatement(classQuery);
            classPs.setInt(1, scanId);
            classPs.executeUpdate();
            classPs.close();
            
            // Delete scan
            String scanQuery = "DELETE FROM scans WHERE id = ?";
            PreparedStatement scanPs = conn.prepareStatement(scanQuery);
            scanPs.setInt(1, scanId);
            scanPs.executeUpdate();
            scanPs.close();
            
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inner class for scan records
    public static class ScanRecord {
        public int scanId;
        public String fileName;
        public String createdAt;
        public int methodCount;
        public int dangerousCount;
    }
}