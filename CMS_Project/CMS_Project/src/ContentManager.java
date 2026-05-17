import java.sql.*;
import java.util.*;

public class ContentManager {
    
    // Add new content
    public static boolean addContent(String title, String description, int createdBy) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "INSERT INTO content (title, description, date_created, created_by) VALUES (?, ?, CURDATE(), ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setInt(3, createdBy);
            
            if (pstmt.executeUpdate() > 0) {
                // Log activity
                ActivityLogger.logActivity(createdBy, "CREATE", "Created content: " + title, 0);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, null);
        }
        return false;
    }
    
    // Get all content with pagination
    public static List<Map<String, String>> getAllContent(int pageNumber, int pageSize) {
        List<Map<String, String>> contentList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            int offset = (pageNumber - 1) * pageSize;
            String sql = "SELECT c.content_id, c.title, c.description, c.date_created, u.username, c.created_by " +
                        "FROM content c JOIN users u ON c.created_by = u.user_id " +
                        "ORDER BY c.date_created DESC LIMIT ? OFFSET ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, pageSize);
            pstmt.setInt(2, offset);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> content = new HashMap<>();
                content.put("content_id", String.valueOf(rs.getInt("content_id")));
                content.put("title", rs.getString("title"));
                content.put("description", rs.getString("description"));
                content.put("date_created", rs.getString("date_created"));
                content.put("username", rs.getString("username"));
                content.put("created_by", String.valueOf(rs.getInt("created_by")));
                contentList.add(content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return contentList;
    }
    
    // Get total content count
    public static int getTotalContentCount() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT COUNT(*) as count FROM content";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return 0;
    }
    
    // Search content by title
    public static List<Map<String, String>> searchContent(String searchTerm) {
        List<Map<String, String>> contentList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT c.content_id, c.title, c.description, c.date_created, u.username, c.created_by " +
                        "FROM content c JOIN users u ON c.created_by = u.user_id " +
                        "WHERE c.title LIKE ? OR c.description LIKE ? " +
                        "ORDER BY c.date_created DESC";
            pstmt = conn.prepareStatement(sql);
            String searchPattern = "%" + searchTerm + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> content = new HashMap<>();
                content.put("content_id", String.valueOf(rs.getInt("content_id")));
                content.put("title", rs.getString("title"));
                content.put("description", rs.getString("description"));
                content.put("date_created", rs.getString("date_created"));
                content.put("username", rs.getString("username"));
                content.put("created_by", String.valueOf(rs.getInt("created_by")));
                contentList.add(content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return contentList;
    }
    
    // Get single content by ID
    public static Map<String, String> getContentById(int contentId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT c.content_id, c.title, c.description, c.date_created, u.username, c.created_by " +
                        "FROM content c JOIN users u ON c.created_by = u.user_id WHERE c.content_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, contentId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Map<String, String> content = new HashMap<>();
                content.put("content_id", String.valueOf(rs.getInt("content_id")));
                content.put("title", rs.getString("title"));
                content.put("description", rs.getString("description"));
                content.put("date_created", rs.getString("date_created"));
                content.put("username", rs.getString("username"));
                content.put("created_by", String.valueOf(rs.getInt("created_by")));
                return content;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return null;
    }
    
    // Edit content
    public static boolean editContent(int contentId, String title, String description, int editedBy) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE content SET title = ?, description = ?, last_edited_by = ?, last_edited_date = CURDATE() WHERE content_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setInt(3, editedBy);
            pstmt.setInt(4, contentId);
            
            if (pstmt.executeUpdate() > 0) {
                ActivityLogger.logActivity(editedBy, "EDIT", "Edited content ID: " + contentId, contentId);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, null);
        }
        return false;
    }
    
    // Delete content
    public static boolean deleteContent(int contentId, int deletedBy) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            // First, delete related activity logs
            String deleteLogs = "DELETE FROM activity_log WHERE content_id = ?";
            PreparedStatement logStmt = conn.prepareStatement(deleteLogs);
            logStmt.setInt(1, contentId);
            logStmt.executeUpdate();
            logStmt.close();
            
            // Then delete the content
            String sql = "DELETE FROM content WHERE content_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, contentId);
            
            if (pstmt.executeUpdate() > 0) {
                ActivityLogger.logActivity(deletedBy, "DELETE", "Deleted content ID: " + contentId, contentId);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, null);
        }
        return false;
    }
    
    // Get user's content
    public static List<Map<String, String>> getUserContent(int userId) {
        List<Map<String, String>> contentList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "SELECT content_id, title, description, date_created FROM content WHERE created_by = ? ORDER BY date_created DESC";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, String> content = new HashMap<>();
                content.put("content_id", String.valueOf(rs.getInt("content_id")));
                content.put("title", rs.getString("title"));
                content.put("description", rs.getString("description"));
                content.put("date_created", rs.getString("date_created"));
                contentList.add(content);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, rs);
        }
        return contentList;
    }
    
    // Console version - Add Content (no parameters)
    public static void addContent() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Content Title: ");
        String title = sc.nextLine();
        System.out.print("Enter Content Description: ");
        String description = sc.nextLine();
        System.out.print("Enter User ID: ");
        int userId = sc.nextInt();
        
        if (addContent(title, description, userId)) {
            System.out.println("✅ Content added successfully!");
        } else {
            System.out.println("❌ Failed to add content!");
        }
    }
    
    // Console version - View Content (no parameters)
    public static void viewContent() {
        List<Map<String, String>> contents = getAllContent(1, 100);
        
        if (contents.isEmpty()) {
            System.out.println("❌ No content found!");
            return;
        }
        
        System.out.println("\n===== ALL CONTENT =====");
        for (Map<String, String> content : contents) {
            System.out.println("\nID: " + content.get("content_id"));
            System.out.println("Title: " + content.get("title"));
            System.out.println("Description: " + content.get("description"));
            System.out.println("Created by: " + content.get("username"));
            System.out.println("Date: " + content.get("date_created"));
        }
        System.out.println("\n======================\n");
    }
    
    // Console version - Delete Content (no parameters)
    public static void deleteContent() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Content ID to delete: ");
        int contentId = sc.nextInt();
        System.out.print("Enter User ID (for logging): ");
        int userId = sc.nextInt();
        
        if (deleteContent(contentId, userId)) {
            System.out.println("✅ Content deleted successfully!");
        } else {
            System.out.println("❌ Failed to delete content!");
        }
    }
    
    // Update content (update title and description)
    public static boolean updateContent(int contentId, String title, String description) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBConnection.getConnection();
            String sql = "UPDATE content SET title = ?, description = ? WHERE content_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, title);
            pstmt.setString(2, description);
            pstmt.setInt(3, contentId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn, pstmt, null);
        }
        return false;
    }
}