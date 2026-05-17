import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class SimpleRestAPI {
    private static HttpServer server;
    private static final int PORT = 5000;

    public static void main(String[] args) throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        server.setExecutor(null);
        
        // ========== ROUTE REGISTRATION ==========
        server.createContext("/api/login", exchange -> handleLogin(exchange));
        server.createContext("/api/content", exchange -> handleContent(exchange));
        server.createContext("/api/users", exchange -> handleUsers(exchange));
        server.createContext("/api/stats", exchange -> handleStats(exchange));
        server.createContext("/api/content/get", exchange -> handleGetContent(exchange));
        server.createContext("/api/content/update", exchange -> handleUpdateContent(exchange));
        server.createContext("/api/users/profile", exchange -> handleUserProfile(exchange));
        server.createContext("/api/users/password", exchange -> handleChangePassword(exchange));
        server.createContext("/api/users/delete", exchange -> handleDeleteUser(exchange));
        server.createContext("/api/activity", exchange -> handleActivityLogs(exchange));
        
        server.start();
        System.out.println("\n✅ REST API Server started on http://localhost:" + PORT);
        System.out.println("🔗 Frontend URL: http://localhost:8000");
        System.out.println("📊 Activity API: http://localhost:5000/api/activity\n");
    }
    
    // ========== LOGIN HANDLER ==========
    private static void handleLogin(HttpExchange exchange) throws IOException {
        addCORSHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String body = readBody(exchange);
                String username = extractValue(body, "username");
                String password = extractValue(body, "password");
                
                Connection conn = DBConnection.getConnection();
                if (conn == null) {
                    sendResponse(exchange, "{\"success\":false,\"message\":\"Database connection failed\"}", 500);
                    return;
                }
                
                String query = "SELECT user_id, username, email, role FROM users WHERE username=? AND password=? AND status='active'";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, username);
                pst.setString(2, password);
                ResultSet rs = pst.executeQuery();
                
                String response;
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    String role = rs.getString("role");
                    
                    ActivityLogger.logActivity(userId, "LOGIN", "User logged in", 0);
                    
                    response = "{\"success\":true,\"message\":\"Login successful\",\"user_id\":" + userId 
                             + ",\"username\":\"" + username + "\",\"role\":\"" + role + "\"}";
                } else {
                    response = "{\"success\":false,\"message\":\"Invalid credentials\"}";
                }
                
                sendResponse(exchange, response, 200);
                DBConnection.closeConnection(conn, pst, rs);
                
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
    }
    
    // ========== CONTENT HANDLER ==========
    private static void handleContent(HttpExchange exchange) throws IOException {
        addCORSHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                List<Map<String, String>> contents = ContentManager.getAllContent(1, 100);
                StringBuilder json = new StringBuilder("[");
                
                for (int i = 0; i < contents.size(); i++) {
                    Map<String, String> c = contents.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"content_id\":").append(c.get("content_id")).append(",")
                        .append("\"title\":\"").append(escapeJson(c.get("title"))).append("\",")
                        .append("\"description\":\"").append(escapeJson(c.get("description"))).append("\",")
                        .append("\"date_created\":\"").append(c.get("date_created")).append("\",")
                        .append("\"username\":\"").append(c.get("username")).append("\",")
                        .append("\"created_by\":").append(c.get("created_by"))
                        .append("}");
                }
                json.append("]");
                
                sendResponse(exchange, "{\"success\":true,\"data\":" + json.toString() + "}", 200);
                
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        } 
        else if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String body = readBody(exchange);
                String title = extractValue(body, "title");
                String description = extractValue(body, "description");
                int createdBy = Integer.parseInt(extractValue(body, "created_by"));
                
                boolean success = ContentManager.addContent(title, description, createdBy);
                
                if (success) {
                    ActivityLogger.logActivity(createdBy, "CREATE", "Created content: " + title, 0);
                    sendResponse(exchange, "{\"success\":true,\"message\":\"Content created\"}", 201);
                } else {
                    sendResponse(exchange, "{\"success\":false,\"message\":\"Failed to create content\"}", 400);
                }
                
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
        else if ("DELETE".equals(exchange.getRequestMethod())) {
            try {
                String body = readBody(exchange);
                int contentId = Integer.parseInt(extractValue(body, "content_id"));
                int deletedBy = Integer.parseInt(extractValue(body, "deleted_by"));
                
                boolean success = ContentManager.deleteContent(contentId, deletedBy);
                
                if (success) {
                    ActivityLogger.logActivity(deletedBy, "DELETE", "Deleted content ID: " + contentId, contentId);
                    sendResponse(exchange, "{\"success\":true,\"message\":\"Content deleted\"}", 200);
                } else {
                    sendResponse(exchange, "{\"success\":false,\"message\":\"Failed to delete content\"}", 400);
                }
                
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
    }
    
    // ========== GET CONTENT BY ID ==========
    private static void handleGetContent(HttpExchange exchange) throws IOException {
        addCORSHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                String query = exchange.getRequestURI().getQuery();
                int contentId = Integer.parseInt(extractQueryParam(query, "id"));
                
                Map<String, String> content = ContentManager.getContentById(contentId);
                
                if (content != null) {
                    String json = "{\"success\":true,\"data\":{" +
                        "\"content_id\":" + content.get("content_id") + "," +
                        "\"title\":\"" + escapeJson(content.get("title")) + "\"," +
                        "\"description\":\"" + escapeJson(content.get("description")) + "\"," +
                        "\"date_created\":\"" + content.get("date_created") + "\"," +
                        "\"username\":\"" + content.get("username") + "\"," +
                        "\"created_by\":" + content.get("created_by") +
                        "}}";
                    sendResponse(exchange, json, 200);
                } else {
                    sendResponse(exchange, "{\"success\":false,\"message\":\"Content not found\"}", 404);
                }
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
    }
    
    // ========== UPDATE CONTENT ==========
    private static void handleUpdateContent(HttpExchange exchange) throws IOException {
        addCORSHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String body = readBody(exchange);
                int contentId = Integer.parseInt(extractValue(body, "content_id"));
                String title = extractValue(body, "title");
                String description = extractValue(body, "description");
                int userId = Integer.parseInt(extractValue(body, "user_id"));
                
                boolean success = ContentManager.updateContent(contentId, title, description);
                
                if (success) {
                    ActivityLogger.logActivity(userId, "EDIT", "Updated content ID: " + contentId, contentId);
                    sendResponse(exchange, "{\"success\":true,\"message\":\"Content updated\"}", 200);
                } else {
                    sendResponse(exchange, "{\"success\":false,\"message\":\"Failed to update content\"}", 400);
                }
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
    }
    
    // ========== USERS HANDLER ==========
    private static void handleUsers(HttpExchange exchange) throws IOException {
        addCORSHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                List<Map<String, String>> users = UserManager.getAllUsers();
                StringBuilder json = new StringBuilder("[");
                
                for (int i = 0; i < users.size(); i++) {
                    Map<String, String> u = users.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"user_id\":").append(u.get("user_id")).append(",")
                        .append("\"username\":\"").append(u.get("username")).append("\",")
                        .append("\"email\":\"").append(u.get("email")).append("\",")
                        .append("\"role\":\"").append(u.get("role")).append("\",")
                        .append("\"status\":\"").append(u.get("status")).append("\"")
                        .append("}");
                }
                json.append("]");
                
                sendResponse(exchange, "{\"success\":true,\"data\":" + json.toString() + "}", 200);
                
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
        else if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String body = readBody(exchange);
                String username = extractValue(body, "username");
                String password = extractValue(body, "password");
                String email = extractValue(body, "email");
                String role = extractValue(body, "role");
                
                boolean success = UserManager.createUser(username, password, email, role);
                
                if (success) {
                    sendResponse(exchange, "{\"success\":true,\"message\":\"User created\"}", 201);
                } else {
                    sendResponse(exchange, "{\"success\":false,\"message\":\"Failed to create user\"}", 400);
                }
                
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
    }
    
    // ========== USER PROFILE ==========
    private static void handleUserProfile(HttpExchange exchange) throws IOException {
        addCORSHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                String query = exchange.getRequestURI().getQuery();
                int userId = Integer.parseInt(extractQueryParam(query, "id"));
                
                Map<String, String> user = UserManager.getUserById(userId);
                
                if (user != null) {
                    String json = "{\"success\":true,\"data\":{" +
                        "\"user_id\":" + user.get("user_id") + "," +
                        "\"username\":\"" + user.get("username") + "\"," +
                        "\"email\":\"" + user.get("email") + "\"," +
                        "\"role\":\"" + user.get("role") + "\"," +
                        "\"status\":\"" + user.get("status") + "\"" +
                        "}}";
                    sendResponse(exchange, json, 200);
                } else {
                    sendResponse(exchange, "{\"success\":false,\"message\":\"User not found\"}", 404);
                }
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
        else if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String body = readBody(exchange);
                int userId = Integer.parseInt(extractValue(body, "user_id"));
                String email = extractValue(body, "email");
                
                boolean success = UserManager.updateUserProfile(userId, email);
                
                if (success) {
                    ActivityLogger.logActivity(userId, "UPDATE_PROFILE", "Updated profile", 0);
                    sendResponse(exchange, "{\"success\":true,\"message\":\"Profile updated\"}", 200);
                } else {
                    sendResponse(exchange, "{\"success\":false,\"message\":\"Failed to update profile\"}", 400);
                }
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
    }
    
    // ========== CHANGE PASSWORD ==========
    private static void handleChangePassword(HttpExchange exchange) throws IOException {
        addCORSHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String body = readBody(exchange);
                int userId = Integer.parseInt(extractValue(body, "user_id"));
                String oldPassword = extractValue(body, "old_password");
                String newPassword = extractValue(body, "new_password");
                
                boolean success = UserManager.changePassword(userId, oldPassword, newPassword);
                
                if (success) {
                    ActivityLogger.logActivity(userId, "CHANGE_PASSWORD", "Changed password", 0);
                    sendResponse(exchange, "{\"success\":true,\"message\":\"Password changed\"}", 200);
                } else {
                    sendResponse(exchange, "{\"success\":false,\"message\":\"Invalid old password\"}", 400);
                }
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
    }
    
    // ========== DELETE USER ==========
    private static void handleDeleteUser(HttpExchange exchange) throws IOException {
        addCORSHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("POST".equals(exchange.getRequestMethod())) {
            try {
                String body = readBody(exchange);
                int userId = Integer.parseInt(extractValue(body, "user_id"));
                
                boolean success = UserManager.deleteUserAccount(userId);
                
                if (success) {
                    sendResponse(exchange, "{\"success\":true,\"message\":\"Account deleted\"}", 200);
                } else {
                    sendResponse(exchange, "{\"success\":false,\"message\":\"Failed to delete account\"}", 400);
                }
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
    }
    
    // ========== ACTIVITY LOGS HANDLER ==========
    private static void handleActivityLogs(HttpExchange exchange) throws IOException {
        addCORSHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                String query = exchange.getRequestURI().getQuery();
                String userIdParam = extractQueryParam(query, "user_id");
                
                List<Map<String, String>> logs;
                if (userIdParam != null && !userIdParam.isEmpty()) {
                    int userId = Integer.parseInt(userIdParam);
                    logs = ActivityLogger.getUserActivityLogs(userId);
                } else {
                    logs = ActivityLogger.getAllActivityLogs();
                }
                
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < logs.size(); i++) {
                    Map<String, String> log = logs.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"log_id\":").append(log.get("log_id")).append(",")
                        .append("\"username\":\"").append(log.get("username")).append("\",")
                        .append("\"action\":\"").append(log.get("action")).append("\",")
                        .append("\"description\":\"").append(escapeJson(log.get("description"))).append("\",")
                        .append("\"timestamp\":\"").append(log.get("timestamp")).append("\"")
                        .append("}");
                }
                json.append("]");
                
                System.out.println("📊 Activity Logs Response: " + json.toString());
                sendResponse(exchange, "{\"success\":true,\"data\":" + json.toString() + "}", 200);
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
    }
    
    // ========== STATS HANDLER ==========
    private static void handleStats(HttpExchange exchange) throws IOException {
        addCORSHeaders(exchange);
        
        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }
        
        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                Map<String, String> stats = ActivityLogger.getDashboardStats();
                
                String json = "{\"success\":true,\"data\":{" +
                    "\"total_users\":" + stats.get("total_users") + "," +
                    "\"total_content\":" + stats.get("total_content") + "," +
                    "\"today_activities\":" + stats.get("today_activities") +
                    "}}";
                
                sendResponse(exchange, json, 200);
                
            } catch (Exception e) {
                sendResponse(exchange, "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}", 500);
            }
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private static void addCORSHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }
    
    private static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
    
    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    
    private static String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) {
            pattern = "\"" + key + "\":";
            start = json.indexOf(pattern);
            if (start == -1) return "";
            start += pattern.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
        start += pattern.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
    
    private static String extractQueryParam(String query, String param) {
        if (query == null) return null;
        String[] params = query.split("&");
        for (String p : params) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(param)) {
                return kv[1];
            }
        }
        return null;
    }
    
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r");
    }
}