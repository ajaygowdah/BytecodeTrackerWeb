package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.net.InetSocketAddress;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.net.URLDecoder;
import java.util.*;

public class WebServer {

    private static Map<String, Integer> sessions = new HashMap<>();

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        /* ---------- LOGIN PAGE ---------- */

        server.createContext("/", exchange -> {
            String sessionId = getSessionId(exchange);
            int userId = sessions.getOrDefault(sessionId, -1);
            
            if (userId > 0) {
                exchange.getResponseHeaders().add("Location", "/dashboard");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }

            File file = new File("resources/login.html");

            if (!file.exists()) {
                String msg = "login.html not found!";
                exchange.sendResponseHeaders(500, msg.length());
                OutputStream os = exchange.getResponseBody();
                os.write(msg.getBytes());
                os.close();
                return;
            }

            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                exchange.getResponseHeaders().add("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, bytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        /* ---------- REGISTER PAGE ---------- */

        server.createContext("/register", exchange -> {
            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                File file = new File("resources/register.html");

                try {
                    byte[] bytes = Files.readAllBytes(file.toPath());
                    exchange.getResponseHeaders().add("Content-Type", "text/html");
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                try {
                    String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    
                    Map<String, String> params = parseFormData(form);
                    
                    String username = params.getOrDefault("username", "").trim();
                    String email = params.getOrDefault("email", "").trim();
                    String password = params.getOrDefault("password", "").trim();

                    if (username.isEmpty() || password.isEmpty()) {
                        sendResponse(exchange, getErrorPage("Username and Password are required", "/register"), 400);
                        return;
                    }

                    boolean success = DBConnection.registerUser(username, email, password);

                    if (success) {
                        String msg = getSuccessPage("Registration Successful!", "Your account has been created successfully. Redirecting to login...", "/");
                        sendResponse(exchange, msg, 200);
                    } else {
                        sendResponse(exchange, getErrorPage("Username or email already exists", "/register"), 400);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, getErrorPage("Registration failed: " + e.getMessage(), "/register"), 500);
                }
            }
        });

        /* ---------- LOGIN PROCESS ---------- */

        server.createContext("/login", exchange -> {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                try {
                    String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    
                    Map<String, String> params = parseFormData(form);
                    
                    String username = params.getOrDefault("username", "").trim();
                    String password = params.getOrDefault("password", "").trim();

                    if (username.isEmpty() || password.isEmpty()) {
                        sendResponse(exchange, getErrorPage("Username and Password are required", "/"), 400);
                        return;
                    }

                    int userId = DBConnection.getUserId(username, password);

                    if (userId > 0) {
                        String sessionId = UUID.randomUUID().toString();
                        sessions.put(sessionId, userId);
                        
                        exchange.getResponseHeaders().add("Set-Cookie", "sessionId=" + sessionId + "; Path=/");

                        exchange.getResponseHeaders().add("Location", "/dashboard");
                        exchange.sendResponseHeaders(302, -1);
                        exchange.close();

                    } else {
                        sendResponse(exchange, getErrorPage("Invalid username or password", "/"), 401);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, getErrorPage("Login failed: " + e.getMessage(), "/"), 500);
                }
            }
        });

        /* ---------- FILE UPLOAD ---------- */

        server.createContext("/upload", exchange -> {
            System.out.println("[UPLOAD] Request received");
            
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            Thread uploadThread = new Thread(() -> {
                try {
                    String sessionId = getSessionId(exchange);
                    int userId = sessions.getOrDefault(sessionId, -1);

                    System.out.println("[UPLOAD] SessionId: " + sessionId + ", UserId: " + userId);

                    if (userId <= 0) {
                        sendResponse(exchange, getErrorPage("You must be logged in to upload", "/"), 401);
                        return;
                    }

                    System.out.println("[UPLOAD] Reading request body...");
                    InputStream is = exchange.getRequestBody();
                    byte[] body = is.readAllBytes();
                    
                    System.out.println("[UPLOAD] Body size: " + body.length + " bytes");

                    String raw = new String(body, "ISO-8859-1");

                    int fileStart = raw.indexOf("\r\n\r\n") + 4;
                    int fileEnd = raw.lastIndexOf("\r\n--");

                    System.out.println("[UPLOAD] FileStart: " + fileStart + ", FileEnd: " + fileEnd);

                    if (fileStart < 4 || fileEnd < 0 || fileEnd <= fileStart) {
                        System.out.println("[UPLOAD] Invalid file boundaries");
                        sendResponse(exchange, getErrorPage("Invalid file upload - corrupted data", "/dashboard"), 400);
                        return;
                    }

                    byte[] fileBytes = new byte[fileEnd - fileStart];
                    System.arraycopy(body, fileStart, fileBytes, 0, fileBytes.length);

                    System.out.println("[UPLOAD] Extracted " + fileBytes.length + " bytes");

                    File uploadDir = new File("uploads");
                    if (!uploadDir.exists()) uploadDir.mkdirs();

                    String fileName = "uploaded_" + System.currentTimeMillis() + ".class";
                    File uploadedFile = new File(uploadDir, fileName);
                    
                    System.out.println("[UPLOAD] Writing file to: " + uploadedFile.getAbsolutePath());
                    Files.write(uploadedFile.toPath(), fileBytes);

                    System.out.println("[UPLOAD] File written successfully");

                    System.out.println("[UPLOAD] Creating scan entry...");
                    int scanId = DBConnection.createScan(userId, fileName);
                    System.out.println("[UPLOAD] Scan ID: " + scanId);

                    System.out.println("[UPLOAD] Starting analysis...");
                    String result = BytecodeAnalyzer.analyzeUploadedClass(uploadedFile.getAbsolutePath(), scanId);
                    System.out.println("[UPLOAD] Analysis complete");

                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, result.getBytes(StandardCharsets.UTF_8).length);

                    OutputStream os = exchange.getResponseBody();
                    os.write(result.getBytes(StandardCharsets.UTF_8));
                    os.close();
                    
                    System.out.println("[UPLOAD] Response sent successfully");

                } catch (Exception e) {
                    System.err.println("[UPLOAD] Error: " + e.getMessage());
                    e.printStackTrace();
                    try {
                        sendResponse(exchange, getErrorPage("Upload failed: " + e.getMessage(), "/dashboard"), 500);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            
            uploadThread.setName("UploadThread");
            uploadThread.start();
        });

        /* ---------- DASHBOARD PAGE ---------- */

        server.createContext("/dashboard", exchange -> {
            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                try {
                    String sessionId = getSessionId(exchange);
                    int userId = sessions.getOrDefault(sessionId, -1);

                    if (userId <= 0) {
                        exchange.getResponseHeaders().add("Location", "/");
                        exchange.sendResponseHeaders(302, -1);
                        exchange.close();
                        return;
                    }

                    String username = DBConnection.getUsernameById(userId);
                    String html = UserDashboard.generateDashboardHTML(userId, username);

                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, html.getBytes(StandardCharsets.UTF_8).length);

                    OutputStream os = exchange.getResponseBody();
                    os.write(html.getBytes(StandardCharsets.UTF_8));
                    os.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /* ---------- SCAN HISTORY PAGE ---------- */

        server.createContext("/history", exchange -> {
            if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                try {
                    String sessionId = getSessionId(exchange);
                    int userId = sessions.getOrDefault(sessionId, -1);

                    if (userId <= 0) {
                        exchange.getResponseHeaders().add("Location", "/");
                        exchange.sendResponseHeaders(302, -1);
                        exchange.close();
                        return;
                    }

                    String username = DBConnection.getUsernameById(userId);
                    String html = ScanHistory.generateHistoryHTML(userId, username);

                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, html.getBytes(StandardCharsets.UTF_8).length);

                    OutputStream os = exchange.getResponseBody();
                    os.write(html.getBytes(StandardCharsets.UTF_8));
                    os.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /* ---------- DELETE SCAN ---------- */

        server.createContext("/delete-scan", exchange -> {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                try {
                    String sessionId = getSessionId(exchange);
                    int userId = sessions.getOrDefault(sessionId, -1);

                    if (userId <= 0) {
                        exchange.getResponseHeaders().add("Location", "/");
                        exchange.sendResponseHeaders(302, -1);
                        exchange.close();
                        return;
                    }

                    String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    Map<String, String> params = parseFormData(form);
                    
                    int scanId = Integer.parseInt(params.getOrDefault("scanId", "0"));

                    DBConnection.deleteScan(scanId);

                    exchange.getResponseHeaders().add("Location", "/history");
                    exchange.sendResponseHeaders(302, -1);
                    exchange.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /* ---------- LOGOUT ---------- */

        server.createContext("/logout", exchange -> {
            try {
                String sessionId = getSessionId(exchange);
                sessions.remove(sessionId);
                
                exchange.getResponseHeaders().add("Set-Cookie", "sessionId=; Path=/; Max-Age=0");
                exchange.getResponseHeaders().add("Location", "/");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        server.setExecutor(null);
        server.start();

        System.out.println("Server started at http://localhost:8080");
    }

    /* ---------- HELPER METHODS ---------- */

    private static Map<String, String> parseFormData(String form) {
        Map<String, String> params = new HashMap<>();
        
        if (form == null || form.isEmpty()) {
            return params;
        }

        String[] pairs = form.split("&");
        for (String pair : pairs) {
            try {
                int idx = pair.indexOf("=");
                if (idx > 0) {
                    String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                    params.put(key, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return params;
    }

    private static String getSessionId(HttpExchange exchange) {
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String cookie : cookies) {
                if (cookie.contains("sessionId=")) {
                    return cookie.split("sessionId=")[1].split(";")[0];
                }
            }
        }
        return "";
    }

    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private static String getErrorPage(String message, String backLink) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Error</title><style>" +
            "body{font-family:Arial,sans-serif;background:#0f172a;color:white;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}" +
            ".error-container{background:#1e293b;padding:40px;border-radius:10px;box-shadow:0 0 20px rgba(0,0,0,0.5);text-align:center;max-width:500px;border-left:5px solid #ef4444;}" +
            "h1{color:#ef4444;margin:0 0 20px 0;}" +
            ".error-icon{font-size:48px;margin-bottom:20px;}" +
            "p{margin:20px 0;font-size:16px;color:#cbd5e1;}" +
            "a{display:inline-block;margin-top:20px;padding:12px 30px;background:#38bdf8;color:white;text-decoration:none;border-radius:5px;}" +
            "a:hover{background:#0ea5e9;}" +
            "</style></head><body>" +
            "<div class='error-container'>" +
            "<div class='error-icon'>⚠️</div>" +
            "<h1>Error</h1>" +
            "<p>" + message + "</p>" +
            "<a href='" + backLink + "'>Go Back</a>" +
            "</div>" +
            "</body></html>";
    }

    private static String getSuccessPage(String title, String message, String redirectLink) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Success</title><style>" +
            "body{font-family:Arial,sans-serif;background:#0f172a;color:white;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;}" +
            ".success-container{background:#1e293b;padding:40px;border-radius:10px;box-shadow:0 0 20px rgba(0,0,0,0.5);text-align:center;max-width:500px;border-left:5px solid #22c55e;}" +
            "h1{color:#22c55e;margin:0 0 20px 0;}" +
            ".success-icon{font-size:48px;margin-bottom:20px;}" +
            "p{margin:20px 0;font-size:16px;color:#cbd5e1;}" +
            "</style><script>setTimeout(() => window.location='" + redirectLink + "', 3000);</script></head><body>" +
            "<div class='success-container'>" +
            "<div class='success-icon'>✅</div>" +
            "<h1>" + title + "</h1>" +
            "<p>" + message + "</p>" +
            "<p>Redirecting in 3 seconds...</p>" +
            "</div>" +
            "</body></html>";
    }
}