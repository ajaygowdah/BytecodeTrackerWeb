package server;

import java.lang.reflect.Method;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class BytecodeAnalyzer {

    public static String analyzeUploadedClass(String filePath, int scanId) {

        StringBuilder result = new StringBuilder();
        long startTime = System.currentTimeMillis();

        try {

            CustomClassLoader loader = new CustomClassLoader();
            Class<?> cls = loader.loadClassFromFile(filePath);

            Connection conn = DBConnection.getConnection();

            try {
                String classQuery = "INSERT INTO classes (scan_id, class_name) VALUES (?, ?)";
                PreparedStatement classPS = conn.prepareStatement(classQuery, Statement.RETURN_GENERATED_KEYS);

                classPS.setInt(1, scanId);
                classPS.setString(2, cls.getName());

                classPS.executeUpdate();

                ResultSet classRS = classPS.getGeneratedKeys();
                int classId = 0;

                if (classRS.next()) {
                    classId = classRS.getInt(1);
                }

                classPS.close();

                Method[] methods = cls.getDeclaredMethods();
                int dangerousCount = 0;
                int safeCount = 0;
                List<MethodInfo> methodList = new ArrayList<>();

                System.out.println("Analyzing " + methods.length + " methods...");

                for (Method m : methods) {

                    String methodName = m.getName();
                    String status = "Safe";
                    String riskReason = "";

                    // ADVANCED DETECTION - Check method name for dangerous patterns
                    if (methodName.toLowerCase().contains("exec")) {
                        status = "Dangerous";
                        riskReason = "Possible code execution via exec()";
                    } else if (methodName.toLowerCase().contains("delete")) {
                        status = "Dangerous";
                        riskReason = "File deletion operation detected";
                    } else if (methodName.toLowerCase().contains("system")) {
                        status = "Dangerous";
                        riskReason = "System command execution detected";
                    } else if (methodName.toLowerCase().contains("danger")) {
                        status = "Dangerous";
                        riskReason = "Method marked as dangerous";
                    } else if (methodName.toLowerCase().contains("exit")) {
                        status = "Dangerous";
                        riskReason = "Process termination via System.exit()";
                    } else if (methodName.toLowerCase().contains("runtime")) {
                        status = "Dangerous";
                        riskReason = "Runtime command execution detected";
                    }

                    // Check for reflection API usage
                    if (methodName.toLowerCase().contains("reflect") || 
                        methodName.toLowerCase().contains("invoke") ||
                        methodName.toLowerCase().contains("getmethod")) {
                        status = "Dangerous";
                        riskReason = "Reflection API usage detected - potential security risk";
                    }

                    // Check for file I/O operations
                    if (methodName.toLowerCase().contains("file") || 
                        methodName.toLowerCase().contains("write") ||
                        methodName.toLowerCase().contains("read") ||
                        methodName.toLowerCase().contains("stream")) {
                        if (!status.equals("Dangerous")) {
                            status = "Suspicious";
                            riskReason = "File I/O operation detected - review for validation";
                        }
                    }

                    // Check for network operations
                    if (methodName.toLowerCase().contains("socket") || 
                        methodName.toLowerCase().contains("connect") ||
                        methodName.toLowerCase().contains("http") ||
                        methodName.toLowerCase().contains("url")) {
                        if (!status.equals("Dangerous")) {
                            status = "Suspicious";
                            riskReason = "Network operation detected - ensure proper validation";
                        }
                    }

                    // Check for native method calls (JNI)
                    if (methodName.toLowerCase().contains("native") || 
                        methodName.toLowerCase().contains("jni")) {
                        status = "Dangerous";
                        riskReason = "Native code execution (JNI) detected - high security risk";
                    }

                    // Check parameter count as indicator of complexity
                    Class<?>[] paramTypes = m.getParameterTypes();
                    if (paramTypes.length > 5 && status.equals("Safe")) {
                        status = "Suspicious";
                        riskReason = "High parameter count - complex method logic";
                    }

                    // Assign risk level
                    String riskLevel = getRiskLevel(status);

                    if (status.equals("Dangerous")) {
                        dangerousCount++;
                    } else if (status.equals("Suspicious")) {
                        dangerousCount++; // Count suspicious as risky too
                    } else {
                        safeCount++;
                    }

                    methodList.add(new MethodInfo(methodName, status, riskReason, riskLevel));

                    // Store method in DB
                    String methodQuery = "INSERT INTO methods (class_id, method_name, status) VALUES (?, ?, ?)";
                    PreparedStatement methodPS = conn.prepareStatement(methodQuery, Statement.RETURN_GENERATED_KEYS);

                    methodPS.setInt(1, classId);
                    methodPS.setString(2, methodName);
                    methodPS.setString(3, status);

                    methodPS.executeUpdate();

                    ResultSet methodRS = methodPS.getGeneratedKeys();
                    int methodId = 0;

                    if (methodRS.next()) {
                        methodId = methodRS.getInt(1);
                    }

                    methodPS.close();
                    methodRS.close();

                    // Store violation if dangerous or suspicious
                    if (!status.equals("Safe")) {
                        String violationQuery = "INSERT INTO violations (method_id, description) VALUES (?, ?)";
                        PreparedStatement violationPS = conn.prepareStatement(violationQuery);

                        violationPS.setInt(1, methodId);
                        violationPS.setString(2, riskReason.isEmpty() ? "Security concern detected" : riskReason);

                        violationPS.executeUpdate();
                        violationPS.close();
                    }
                }

                long analysisTime = System.currentTimeMillis() - startTime;
                System.out.println("Analysis completed in " + analysisTime + "ms");

                // Build beautiful HTML response
                result.append(getAnalysisHTML(cls.getName(), methodList, dangerousCount, safeCount, analysisTime));

            } finally {
                if (conn != null) {
                    conn.close();
                }
            }

        } catch (Exception e) {
            System.err.println("Error during analysis: " + e.getMessage());
            e.printStackTrace();
            result.append(getErrorAnalysisHTML(e.getMessage()));
        }

        return result.toString();
    }

    private static String getRiskLevel(String status) {
        if (status.equals("Dangerous")) return "HIGH";
        if (status.equals("Suspicious")) return "MEDIUM";
        return "LOW";
    }

    private static String getAnalysisHTML(String className, List<MethodInfo> methods, int dangerous, int safe, long analysisTime) {
        StringBuilder html = new StringBuilder();
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Analysis Results - BytecodeAnalyzer</title>\n");
        html.append("<style>\n");
        html.append(getCSSStyles());
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("<div class='container'>\n");
        
        // Header
        html.append("<div class='header'>\n");
        html.append("<h1>📊 Bytecode Security Analysis Report</h1>\n");
        html.append("<p class='timestamp'>Generated on ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())).append(" | Analysis Time: ").append(analysisTime).append("ms</p>\n");
        html.append("</div>\n");
        
        // Class Info Card
        html.append("<div class='card class-card'>\n");
        html.append("<div class='card-header'>📦 Class Information</div>\n");
        html.append("<div class='card-body'>\n");
        html.append("<div class='class-name'>").append(escapeHtml(className)).append("</div>\n");
        html.append("<div class='class-meta'>\n");
        html.append("<span class='badge badge-info'>Total Methods: ").append(methods.size()).append("</span>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        
        // Summary Stats
        html.append("<div class='stats-grid'>\n");
        html.append("<div class='stat-card stat-safe'>\n");
        html.append("<div class='stat-number'>").append(safe).append("</div>\n");
        html.append("<div class='stat-label'>Safe Methods</div>\n");
        html.append("</div>\n");
        html.append("<div class='stat-card stat-danger'>\n");
        html.append("<div class='stat-number'>").append(dangerous).append("</div>\n");
        html.append("<div class='stat-label'>High Risk Methods</div>\n");
        html.append("</div>\n");
        html.append("<div class='stat-card stat-total'>\n");
        html.append("<div class='stat-number'>").append(methods.size()).append("</div>\n");
        html.append("<div class='stat-label'>Total Methods</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        
        // Risk Level Assessment
        int highRisk = (int) methods.stream().filter(m -> m.status.equals("Dangerous")).count();
        int mediumRisk = (int) methods.stream().filter(m -> m.status.equals("Suspicious")).count();
        int lowRisk = methods.size() - highRisk - mediumRisk;
        
        String overallRisk = highRisk > 0 ? "HIGH" : (mediumRisk > 0 ? "MEDIUM" : "LOW");
        String riskColor = highRisk > 0 ? "#ef4444" : (mediumRisk > 0 ? "#f59e0b" : "#22c55e");
        
        html.append("<div class='risk-card' style='border-left: 5px solid ").append(riskColor).append(";'>\n");
        html.append("<div class='risk-left'>\n");
        html.append("<div class='risk-label'>Overall Security Risk Level</div>\n");
        html.append("<div class='risk-value' style='color: ").append(riskColor).append(";'>").append(overallRisk).append("</div>\n");
        html.append("</div>\n");
        html.append("<div class='risk-breakdown'>\n");
        html.append("<div class='risk-item'><span class='risk-dot' style='background:#ef4444;'></span> High Risk: ").append(highRisk).append("</div>\n");
        html.append("<div class='risk-item'><span class='risk-dot' style='background:#f59e0b;'></span> Medium Risk: ").append(mediumRisk).append("</div>\n");
        html.append("<div class='risk-item'><span class='risk-dot' style='background:#22c55e;'></span> Low Risk: ").append(lowRisk).append("</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        
        // Methods Table
        html.append("<div class='card'>\n");
        html.append("<div class='card-header'>🔍 Detailed Method Analysis</div>\n");
        html.append("<div class='table-container'>\n");
        html.append("<table class='methods-table'>\n");
        html.append("<thead>\n");
        html.append("<tr>\n");
        html.append("<th class='col-method'>Method Name</th>\n");
        html.append("<th class='col-status'>Risk Level</th>\n");
        html.append("<th class='col-reason'>Risk Reason</th>\n");
        html.append("</tr>\n");
        html.append("</thead>\n");
        html.append("<tbody>\n");
        
        for (MethodInfo method : methods) {
            String statusClass = method.status.equals("Dangerous") ? "status-danger" : 
                                (method.status.equals("Suspicious") ? "status-suspicious" : "status-safe");
            String statusIcon = method.status.equals("Dangerous") ? "🔴" : 
                               (method.status.equals("Suspicious") ? "🟡" : "🟢");
            
            html.append("<tr class='").append(statusClass).append("'>\n");
            html.append("<td class='method-name'><code>").append(escapeHtml(method.name)).append("()</code></td>\n");
            html.append("<td class='status-cell'><span class='risk-badge risk-").append(method.riskLevel.toLowerCase()).append("'>").append(statusIcon).append(" ").append(method.riskLevel).append("</span></td>\n");
            html.append("<td class='reason-cell'>").append(escapeHtml(method.reason)).append("</td>\n");
            html.append("</tr>\n");
        }
        
        html.append("</tbody>\n");
        html.append("</table>\n");
        html.append("</div>\n");
        html.append("</div>\n");

        // Security Recommendations
        html.append("<div class='card recommendations'>\n");
        html.append("<div class='card-header'>💡 Security Recommendations</div>\n");
        html.append("<div class='card-body'>\n");
        if (highRisk > 0) {
            html.append("<div class='recommendation'><strong>⚠️ High Risk Found:</strong> Review and fix ").append(highRisk).append(" method(s) immediately. These may execute external code or system commands.</div>\n");
        }
        if (mediumRisk > 0) {
            html.append("<div class='recommendation'><strong>⚡ Medium Risk Found:</strong> Review ").append(mediumRisk).append(" method(s) for potential security issues. Ensure proper input validation.</div>\n");
        }
        if (highRisk == 0 && mediumRisk == 0) {
            html.append("<div class='recommendation'><strong>✅ Safe:</strong> No critical security issues detected. Continue with standard security practices.</div>\n");
        }
        html.append("</div>\n");
        html.append("</div>\n");
        
        // Footer
        html.append("<div class='footer'>\n");
        html.append("<a href='/'>← Back to Upload</a>\n");
        html.append("</div>\n");
        
        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
    }

    private static String getErrorAnalysisHTML(String errorMessage) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Analysis Error</title><style>" +
            "body{font-family:Arial,sans-serif;background:#0f172a;color:white;padding:20px;}" +
            ".container{max-width:800px;margin:0 auto;background:#1e293b;padding:40px;border-radius:10px;}" +
            ".error-card{border-left:5px solid #ef4444;background:#1f2937;padding:20px;border-radius:5px;}" +
            "h1{color:#ef4444;}" +
            ".error-message{color:#cbd5e1;margin:20px 0;padding:15px;background:#0f172a;border-radius:5px;border-left:3px solid #ef4444;word-break:break-all;}" +
            "a{display:inline-block;margin-top:20px;padding:10px 20px;background:#38bdf8;color:white;text-decoration:none;border-radius:5px;}" +
            "a:hover{background:#0ea5e9;}" +
            "</style></head><body>" +
            "<div class='container'>" +
            "<div class='error-card'>" +
            "<h1>⚠️ Analysis Failed</h1>" +
            "<div class='error-message'>" + escapeHtml(errorMessage) + "</div>" +
            "<a href='/'>← Back to Upload</a>" +
            "</div>" +
            "</div>" +
            "</body></html>";
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    private static String getCSSStyles() {
        return "* { margin: 0; padding: 0; box-sizing: border-box; }\n" +
            "body {\n" +
            "  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
            "  background: linear-gradient(135deg, #0f172a 0%, #1a202c 100%);\n" +
            "  color: #e2e8f0;\n" +
            "  line-height: 1.6;\n" +
            "}\n" +
            ".container {\n" +
            "  max-width: 1200px;\n" +
            "  margin: 0 auto;\n" +
            "  padding: 20px;\n" +
            "}\n" +
            ".header {\n" +
            "  text-align: center;\n" +
            "  margin-bottom: 40px;\n" +
            "  padding: 30px 0;\n" +
            "  border-bottom: 2px solid #334155;\n" +
            "}\n" +
            ".header h1 {\n" +
            "  font-size: 2.5em;\n" +
            "  color: #38bdf8;\n" +
            "  margin-bottom: 10px;\n" +
            "  text-shadow: 0 2px 10px rgba(56, 189, 248, 0.2);\n" +
            "}\n" +
            ".timestamp {\n" +
            "  color: #94a3b8;\n" +
            "  font-size: 0.9em;\n" +
            "}\n" +
            ".card {\n" +
            "  background: #1e293b;\n" +
            "  border-radius: 10px;\n" +
            "  overflow: hidden;\n" +
            "  margin-bottom: 25px;\n" +
            "  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);\n" +
            "  border: 1px solid #334155;\n" +
            "}\n" +
            ".card-header {\n" +
            "  background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%);\n" +
            "  color: white;\n" +
            "  padding: 15px 20px;\n" +
            "  font-weight: bold;\n" +
            "  font-size: 1.1em;\n" +
            "}\n" +
            ".card-body {\n" +
            "  padding: 25px;\n" +
            "}\n" +
            ".class-card .card-body {\n" +
            "  text-align: center;\n" +
            "}\n" +
            ".class-name {\n" +
            "  font-size: 1.3em;\n" +
            "  font-weight: bold;\n" +
            "  color: #38bdf8;\n" +
            "  word-break: break-all;\n" +
            "  margin-bottom: 15px;\n" +
            "  font-family: 'Courier New', monospace;\n" +
            "}\n" +
            ".class-meta {\n" +
            "  display: flex;\n" +
            "  gap: 10px;\n" +
            "  justify-content: center;\n" +
            "  flex-wrap: wrap;\n" +
            "}\n" +
            ".badge {\n" +
            "  display: inline-block;\n" +
            "  padding: 8px 15px;\n" +
            "  border-radius: 20px;\n" +
            "  font-size: 0.85em;\n" +
            "  font-weight: bold;\n" +
            "}\n" +
            ".badge-info {\n" +
            "  background: #0ea5e9;\n" +
            "  color: white;\n" +
            "}\n" +
            ".stats-grid {\n" +
            "  display: grid;\n" +
            "  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));\n" +
            "  gap: 20px;\n" +
            "  margin-bottom: 25px;\n" +
            "}\n" +
            ".stat-card {\n" +
            "  background: #1e293b;\n" +
            "  border-radius: 10px;\n" +
            "  padding: 25px;\n" +
            "  text-align: center;\n" +
            "  border-left: 4px solid;\n" +
            "  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);\n" +
            "}\n" +
            ".stat-safe {\n" +
            "  border-left-color: #22c55e;\n" +
            "}\n" +
            ".stat-danger {\n" +
            "  border-left-color: #ef4444;\n" +
            "}\n" +
            ".stat-total {\n" +
            "  border-left-color: #38bdf8;\n" +
            "}\n" +
            ".stat-number {\n" +
            "  font-size: 2.5em;\n" +
            "  font-weight: bold;\n" +
            "  margin-bottom: 10px;\n" +
            "}\n" +
            ".stat-safe .stat-number { color: #22c55e; }\n" +
            ".stat-danger .stat-number { color: #ef4444; }\n" +
            ".stat-total .stat-number { color: #38bdf8; }\n" +
            ".stat-label {\n" +
            "  color: #94a3b8;\n" +
            "  font-size: 0.95em;\n" +
            "  font-weight: 500;\n" +
            "}\n" +
            ".risk-card {\n" +
            "  background: #1e293b;\n" +
            "  border-radius: 10px;\n" +
            "  padding: 25px;\n" +
            "  margin-bottom: 25px;\n" +
            "  display: flex;\n" +
            "  justify-content: space-between;\n" +
            "  align-items: center;\n" +
            "  gap: 30px;\n" +
            "}\n" +
            ".risk-left {\n" +
            "  flex: 1;\n" +
            "}\n" +
            ".risk-label {\n" +
            "  font-size: 1.1em;\n" +
            "  font-weight: bold;\n" +
            "  margin-bottom: 10px;\n" +
            "}\n" +
            ".risk-value {\n" +
            "  font-size: 2em;\n" +
            "  font-weight: bold;\n" +
            "}\n" +
            ".risk-breakdown {\n" +
            "  flex: 1;\n" +
            "  display: flex;\n" +
            "  flex-direction: column;\n" +
            "  gap: 10px;\n" +
            "}\n" +
            ".risk-item {\n" +
            "  display: flex;\n" +
            "  align-items: center;\n" +
            "  gap: 8px;\n" +
            "}\n" +
            ".risk-dot {\n" +
            "  width: 12px;\n" +
            "  height: 12px;\n" +
            "  border-radius: 50%;\n" +
            "}\n" +
            ".table-container {\n" +
            "  overflow-x: auto;\n" +
            "}\n" +
            ".methods-table {\n" +
            "  width: 100%;\n" +
            "  border-collapse: collapse;\n" +
            "}\n" +
            ".methods-table thead {\n" +
            "  background: #0f172a;\n" +
            "  border-bottom: 2px solid #334155;\n" +
            "}\n" +
            ".methods-table th {\n" +
            "  padding: 15px;\n" +
            "  text-align: left;\n" +
            "  font-weight: bold;\n" +
            "  color: #94a3b8;\n" +
            "  font-size: 0.95em;\n" +
            "  text-transform: uppercase;\n" +
            "  letter-spacing: 0.5px;\n" +
            "}\n" +
            ".col-method { width: 40%; }\n" +
            ".col-status { width: 20%; }\n" +
            ".col-reason { width: 40%; }\n" +
            ".methods-table tbody tr {\n" +
            "  border-bottom: 1px solid #334155;\n" +
            "  transition: background 0.3s ease;\n" +
            "}\n" +
            ".methods-table tbody tr:hover {\n" +
            "  background: #0f172a;\n" +
            "}\n" +
            ".status-safe {\n" +
            "  background: rgba(34, 197, 94, 0.05);\n" +
            "}\n" +
            ".status-suspicious {\n" +
            "  background: rgba(245, 158, 11, 0.05);\n" +
            "}\n" +
            ".status-danger {\n" +
            "  background: rgba(239, 68, 68, 0.05);\n" +
            "}\n" +
            ".methods-table td {\n" +
            "  padding: 15px;\n" +
            "}\n" +
            ".method-name {\n" +
            "  font-family: 'Courier New', monospace;\n" +
            "  color: #38bdf8;\n" +
            "  font-weight: 500;\n" +
            "}\n" +
            ".method-name code {\n" +
            "  background: #0f172a;\n" +
            "  padding: 2px 6px;\n" +
            "  border-radius: 3px;\n" +
            "}\n" +
            ".status-cell {\n" +
            "  text-align: center;\n" +
            "}\n" +
            ".reason-cell {\n" +
            "  color: #cbd5e1;\n" +
            "  font-size: 0.9em;\n" +
            "}\n" +
            ".risk-badge {\n" +
            "  display: inline-block;\n" +
            "  padding: 6px 12px;\n" +
            "  border-radius: 20px;\n" +
            "  font-size: 0.85em;\n" +
            "  font-weight: bold;\n" +
            "}\n" +
            ".risk-high {\n" +
            "  background: #ef4444;\n" +
            "  color: white;\n" +
            "}\n" +
            ".risk-medium {\n" +
            "  background: #f59e0b;\n" +
            "  color: white;\n" +
            "}\n" +
            ".risk-low {\n" +
            "  background: #22c55e;\n" +
            "  color: white;\n" +
            "}\n" +
            ".recommendations {\n" +
            "  background: #1f2937;\n" +
            "}\n" +
            ".recommendations .card-header {\n" +
            "  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);\n" +
            "}\n" +
            ".recommendation {\n" +
            "  background: #0f172a;\n" +
            "  border-left: 4px solid #f59e0b;\n" +
            "  padding: 15px;\n" +
            "  margin-bottom: 12px;\n" +
            "  border-radius: 5px;\n" +
            "  color: #e2e8f0;\n" +
            "}\n" +
            ".footer {\n" +
            "  text-align: center;\n" +
            "  margin-top: 40px;\n" +
            "  padding-top: 20px;\n" +
            "  border-top: 1px solid #334155;\n" +
            "}\n" +
            ".footer a {\n" +
            "  display: inline-block;\n" +
            "  padding: 12px 30px;\n" +
            "  background: linear-gradient(135deg, #38bdf8 0%, #0ea5e9 100%);\n" +
            "  color: white;\n" +
            "  text-decoration: none;\n" +
            "  border-radius: 5px;\n" +
            "  font-weight: bold;\n" +
            "  transition: transform 0.3s ease, box-shadow 0.3s ease;\n" +
            "}\n" +
            ".footer a:hover {\n" +
            "  transform: translateY(-2px);\n" +
            "  box-shadow: 0 5px 15px rgba(56, 189, 248, 0.4);\n" +
            "}\n" +
            "@media (max-width: 768px) {\n" +
            "  .risk-card { flex-direction: column; align-items: flex-start; }\n" +
            "  .col-method { width: 50%; }\n" +
            "  .col-status { width: 25%; }\n" +
            "  .col-reason { width: 25%; }\n" +
            "}\n";
    }

    // Inner class to hold method info
    static class MethodInfo {
        String name;
        String status;
        String reason;
        String riskLevel;

        MethodInfo(String name, String status, String reason, String riskLevel) {
            this.name = name;
            this.status = status;
            this.reason = reason;
            this.riskLevel = riskLevel;
        }
    }
}