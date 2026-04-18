package server;

import java.util.*;

public class ScanHistory {

    public static String generateHistoryHTML(int userId, String username) {
        StringBuilder html = new StringBuilder();
        
        List<DBConnection.ScanRecord> scans = DBConnection.getUserScans(userId);
        int totalScans = scans.size();
        int totalMethods = 0;
        int totalDangerous = 0;
        
        for (DBConnection.ScanRecord scan : scans) {
            totalMethods += scan.methodCount;
            totalDangerous += scan.dangerousCount;
        }
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("<title>Scan History - BytecodeAnalyzer</title>\n");
        html.append("<style>\n");
        html.append(getCSSStyles());
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        html.append("<div class='container'>\n");
        
        // Header
        html.append("<div class='header'>\n");
        html.append("<div class='header-content'>\n");
        html.append("<h1>📜 Scan History</h1>\n");
        html.append("<p class='welcome'>User: <strong>").append(escapeHtml(username)).append("</strong></p>\n");
        html.append("</div>\n");
        html.append("<div class='header-actions'>\n");
        html.append("<a href='/dashboard'>← Back to Dashboard</a>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        
        // Statistics Cards
        html.append("<div class='stats-grid'>\n");
        html.append("<div class='stat-card'>\n");
        html.append("<div class='stat-icon'>📁</div>\n");
        html.append("<div class='stat-number'>").append(totalScans).append("</div>\n");
        html.append("<div class='stat-label'>Total Scans</div>\n");
        html.append("</div>\n");
        
        html.append("<div class='stat-card'>\n");
        html.append("<div class='stat-icon'>⚙️</div>\n");
        html.append("<div class='stat-number'>").append(totalMethods).append("</div>\n");
        html.append("<div class='stat-label'>Total Methods Detected</div>\n");
        html.append("</div>\n");
        
        html.append("<div class='stat-card'>\n");
        html.append("<div class='stat-icon'>🔴</div>\n");
        html.append("<div class='stat-number'>").append(totalDangerous).append("</div>\n");
        html.append("<div class='stat-label'>High Risk Methods</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        
        // History Table
        html.append("<div class='card'>\n");
        html.append("<div class='card-header'>📋 All Uploaded Files</div>\n");
        
        if (scans.isEmpty()) {
            html.append("<div class='empty-state'>\n");
            html.append("<div class='empty-icon'>📭</div>\n");
            html.append("<p>No files uploaded yet.</p>\n");
            html.append("<p><a href='/dashboard'>Go to Dashboard</a> to upload a file.</p>\n");
            html.append("</div>\n");
        } else {
            html.append("<div class='table-container'>\n");
            html.append("<table class='history-table'>\n");
            html.append("<thead>\n");
            html.append("<tr>\n");
            html.append("<th>#</th>\n");
            html.append("<th>File Name</th>\n");
            html.append("<th>Methods Detected</th>\n");
            html.append("<th>High Risk</th>\n");
            html.append("<th>Safe Methods</th>\n");
            html.append("<th>Scan Date</th>\n");
            html.append("<th>Actions</th>\n");
            html.append("</tr>\n");
            html.append("</thead>\n");
            html.append("<tbody>\n");
            
            int rowNum = 1;
            for (DBConnection.ScanRecord scan : scans) {
                String riskLevel = scan.dangerousCount > 0 ? "🔴 HIGH" : "🟢 SAFE";
                String riskClass = scan.dangerousCount > 0 ? "risk-high" : "risk-safe";
                int safeMethods = scan.methodCount - scan.dangerousCount;
                
                html.append("<tr>\n");
                html.append("<td class='row-number'>").append(rowNum).append("</td>\n");
                html.append("<td class='file-name'>📄 ").append(escapeHtml(scan.fileName)).append("</td>\n");
                html.append("<td class='methods-count'><strong>").append(scan.methodCount).append("</strong></td>\n");
                html.append("<td class='high-risk'>🔴 ").append(scan.dangerousCount).append("</td>\n");
                html.append("<td class='safe-methods'>🟢 ").append(safeMethods).append("</td>\n");
                html.append("<td class='date'>📅 ").append(scan.createdAt).append("</td>\n");
                html.append("<td class='actions'>\n");
                html.append("<a href='/scan-detail?id=").append(scan.scanId).append("' class='btn-view'>View</a>\n");
                html.append("<form method='POST' action='/delete-scan' style='display:inline;' onsubmit='return confirm(\"Delete this scan?\")'>\n");
                html.append("<input type='hidden' name='scanId' value='").append(scan.scanId).append("'>\n");
                html.append("<button type='submit' class='btn-delete'>Delete</button>\n");
                html.append("</form>\n");
                html.append("</td>\n");
                html.append("</tr>\n");
                rowNum++;
            }
            
            html.append("</tbody>\n");
            html.append("</table>\n");
            html.append("</div>\n");
        }
        
        html.append("</div>\n");
        
        html.append("</div>\n");
        html.append("</body>\n");
        html.append("</html>\n");
        
        return html.toString();
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
            "  min-height: 100vh;\n" +
            "  padding: 20px;\n" +
            "}\n" +
            ".container {\n" +
            "  max-width: 1200px;\n" +
            "  margin: 0 auto;\n" +
            "}\n" +
            ".header {\n" +
            "  display: flex;\n" +
            "  justify-content: space-between;\n" +
            "  align-items: center;\n" +
            "  margin-bottom: 40px;\n" +
            "  padding: 30px;\n" +
            "  background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);\n" +
            "  border-radius: 10px;\n" +
            "  border: 1px solid #334155;\n" +
            "}\n" +
            ".header h1 {\n" +
            "  font-size: 2.5em;\n" +
            "  color: #38bdf8;\n" +
            "  margin-bottom: 10px;\n" +
            "}\n" +
            ".welcome {\n" +
            "  color: #94a3b8;\n" +
            "  font-size: 1em;\n" +
            "}\n" +
            ".header-actions a {\n" +
            "  padding: 10px 20px;\n" +
            "  background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%);\n" +
            "  color: white;\n" +
            "  text-decoration: none;\n" +
            "  border-radius: 5px;\n" +
            "  font-weight: bold;\n" +
            "  transition: transform 0.3s ease;\n" +
            "}\n" +
            ".header-actions a:hover {\n" +
            "  transform: translateY(-2px);\n" +
            "}\n" +
            ".stats-grid {\n" +
            "  display: grid;\n" +
            "  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));\n" +
            "  gap: 20px;\n" +
            "  margin-bottom: 30px;\n" +
            "}\n" +
            ".stat-card {\n" +
            "  background: #1e293b;\n" +
            "  border-radius: 10px;\n" +
            "  padding: 25px;\n" +
            "  text-align: center;\n" +
            "  border: 1px solid #334155;\n" +
            "  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);\n" +
            "  transition: transform 0.3s ease;\n" +
            "}\n" +
            ".stat-card:hover {\n" +
            "  transform: translateY(-5px);\n" +
            "}\n" +
            ".stat-icon {\n" +
            "  font-size: 2.5em;\n" +
            "  margin-bottom: 10px;\n" +
            "}\n" +
            ".stat-number {\n" +
            "  font-size: 2.5em;\n" +
            "  font-weight: bold;\n" +
            "  color: #38bdf8;\n" +
            "  margin-bottom: 10px;\n" +
            "}\n" +
            ".stat-label {\n" +
            "  color: #94a3b8;\n" +
            "  font-size: 1em;\n" +
            "}\n" +
            ".card {\n" +
            "  background: #1e293b;\n" +
            "  border-radius: 10px;\n" +
            "  overflow: hidden;\n" +
            "  border: 1px solid #334155;\n" +
            "  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);\n" +
            "  margin-bottom: 25px;\n" +
            "}\n" +
            ".card-header {\n" +
            "  background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%);\n" +
            "  color: white;\n" +
            "  padding: 20px;\n" +
            "  font-weight: bold;\n" +
            "  font-size: 1.2em;\n" +
            "}\n" +
            ".empty-state {\n" +
            "  padding: 60px 20px;\n" +
            "  text-align: center;\n" +
            "  color: #94a3b8;\n" +
            "}\n" +
            ".empty-icon {\n" +
            "  font-size: 4em;\n" +
            "  margin-bottom: 20px;\n" +
            "}\n" +
            ".empty-state p {\n" +
            "  margin: 10px 0;\n" +
            "  font-size: 1.1em;\n" +
            "}\n" +
            ".empty-state a {\n" +
            "  color: #38bdf8;\n" +
            "  text-decoration: none;\n" +
            "  font-weight: bold;\n" +
            "}\n" +
            ".empty-state a:hover {\n" +
            "  text-decoration: underline;\n" +
            "}\n" +
            ".table-container {\n" +
            "  overflow-x: auto;\n" +
            "}\n" +
            ".history-table {\n" +
            "  width: 100%;\n" +
            "  border-collapse: collapse;\n" +
            "}\n" +
            ".history-table thead {\n" +
            "  background: #0f172a;\n" +
            "  border-bottom: 2px solid #334155;\n" +
            "}\n" +
            ".history-table th {\n" +
            "  padding: 15px;\n" +
            "  text-align: left;\n" +
            "  font-weight: bold;\n" +
            "  color: #94a3b8;\n" +
            "  text-transform: uppercase;\n" +
            "  font-size: 0.9em;\n" +
            "  letter-spacing: 0.5px;\n" +
            "}\n" +
            ".history-table tbody tr {\n" +
            "  border-bottom: 1px solid #334155;\n" +
            "  transition: background 0.3s ease;\n" +
            "}\n" +
            ".history-table tbody tr:hover {\n" +
            "  background: #0f172a;\n" +
            "}\n" +
            ".history-table td {\n" +
            "  padding: 15px;\n" +
            "}\n" +
            ".row-number {\n" +
            "  text-align: center;\n" +
            "  font-weight: bold;\n" +
            "  color: #38bdf8;\n" +
            "  width: 50px;\n" +
            "}\n" +
            ".file-name {\n" +
            "  font-family: 'Courier New', monospace;\n" +
            "  color: #38bdf8;\n" +
            "  font-weight: 500;\n" +
            "}\n" +
            ".methods-count {\n" +
            "  text-align: center;\n" +
            "  font-size: 1.1em;\n" +
            "  color: #22c55e;\n" +
            "}\n" +
            ".high-risk {\n" +
            "  text-align: center;\n" +
            "  color: #fca5a5;\n" +
            "  font-weight: bold;\n" +
            "}\n" +
            ".safe-methods {\n" +
            "  text-align: center;\n" +
            "  color: #86efac;\n" +
            "  font-weight: bold;\n" +
            "}\n" +
            ".date {\n" +
            "  color: #cbd5e1;\n" +
            "  font-size: 0.9em;\n" +
            "  text-align: center;\n" +
            "}\n" +
            ".actions {\n" +
            "  display: flex;\n" +
            "  gap: 10px;\n" +
            "  justify-content: center;\n" +
            "}\n" +
            ".btn-view, .btn-delete {\n" +
            "  padding: 8px 15px;\n" +
            "  border: none;\n" +
            "  border-radius: 5px;\n" +
            "  font-size: 0.85em;\n" +
            "  cursor: pointer;\n" +
            "  font-weight: bold;\n" +
            "  text-decoration: none;\n" +
            "  display: inline-block;\n" +
            "  transition: all 0.3s ease;\n" +
            "}\n" +
            ".btn-view {\n" +
            "  background: #0ea5e9;\n" +
            "  color: white;\n" +
            "}\n" +
            ".btn-view:hover {\n" +
            "  background: #06b6d4;\n" +
            "  transform: translateY(-2px);\n" +
            "}\n" +
            ".btn-delete {\n" +
            "  background: #ef4444;\n" +
            "  color: white;\n" +
            "}\n" +
            ".btn-delete:hover {\n" +
            "  background: #dc2626;\n" +
            "  transform: translateY(-2px);\n" +
            "}\n" +
            "@media (max-width: 768px) {\n" +
            "  .header { flex-direction: column; align-items: flex-start; gap: 20px; }\n" +
            "  .actions { flex-direction: column; }\n" +
            "  .history-table { font-size: 0.9em; }\n" +
            "  .history-table th, .history-table td { padding: 10px; }\n" +
            "}\n";
    }
} 