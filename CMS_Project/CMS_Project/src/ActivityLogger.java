import java.sql.*;
import java.util.*;

public class ActivityLogger {
    
    public static void logActivity(int userId, String action, String description, int contentId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO activity_log (user_id, action, description, content_id, timestamp) VALUES (?, ?, ?, ?, NOW())";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, action);
            pstmt.setString(3, description);
            pstmt.setInt(4, contentId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, null);
        }
    }
    
    public static List<Map<String, String>> getAllActivityLogs() {
        List<Map<String, String>> logs = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT al.log_id, u.username, al.action, al.description, al.timestamp FROM activity_log al JOIN users u ON al.user_id = u.user_id ORDER BY al.timestamp DESC LIMIT 100";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> log = new HashMap<>();
                log.put("log_id", String.valueOf(rs.getInt("log_id")));
                log.put("username", rs.getString("username"));
                log.put("action", rs.getString("action"));
                log.put("description", rs.getString("description"));
                log.put("timestamp", rs.getString("timestamp"));
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return logs;
    }
    
    public static List<Map<String, String>> getUserActivityLogs(int userId) {
        List<Map<String, String>> logs = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT al.log_id, u.username, al.action, al.description, al.timestamp FROM activity_log al JOIN users u ON al.user_id = u.user_id WHERE al.user_id = ? ORDER BY al.timestamp DESC LIMIT 50";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> log = new HashMap<>();
                log.put("log_id", String.valueOf(rs.getInt("log_id")));
                log.put("username", rs.getString("username"));
                log.put("action", rs.getString("action"));
                log.put("description", rs.getString("description"));
                log.put("timestamp", rs.getString("timestamp"));
                logs.add(log);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return logs;
    }
    
    public static Map<String, String> getDashboardStats() {
        Map<String, String> stats = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            
            String userQuery = "SELECT COUNT(*) as count FROM users";
            pstmt = conn.prepareStatement(userQuery);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("total_users", String.valueOf(rs.getInt("count")));
            }
            
            String contentQuery = "SELECT COUNT(*) as count FROM content";
            pstmt = conn.prepareStatement(contentQuery);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("total_content", String.valueOf(rs.getInt("count")));
            }
            
            String activityQuery = "SELECT COUNT(*) as count FROM activity_log WHERE DATE(timestamp) = CURDATE()";
            pstmt = conn.prepareStatement(activityQuery);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("today_activities", String.valueOf(rs.getInt("count")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return stats;
    }
}