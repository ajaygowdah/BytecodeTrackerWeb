package server;

import java.util.*;

public class UserDashboard {

    public static String generateDashboardHTML(int userId, String username) {
        StringBuilder html = new StringBuilder();
        
        // Get user statistics
        List<DBConnection.ScanRecord> scans = DBConnection.getUserScans(userId);
        int totalScans = DBConnection.getTotalScans(userId);
        int totalDangerous = DBConnection.getTotalDangerousMethods(userId);
        int totalMethods = 0;
        
        for (DBConnection.ScanRecord scan : scans) {
            totalMethods += scan.methodCount;
        }
        
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Dashboard - BytecodeAnalyzer</title>\n");
        html.append("<style>\n");
        html.append(getCSSStyles());
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        html.append("<div class='container'>\n");
        
        // Header with username
        html.append("<div class='header'>\n");
        html.append("<div class='header-content'>\n");
        html.append("<h1>📊 Dashboard</h1>\n");
        html.append("<p class='welcome'>Welcome, <strong>").append(escapeHtml(username)).append("</strong></p>\n");
        html.append("</div>\n");
        html.append("<div class='header-actions'>\n");
        html.append("<a href='/history' class='history-btn'>📜 View History</a>\n");
        html.append("<a href='/logout' class='logout'>Logout</a>\n");
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
        html.append("<div class='stat-label'>Methods Analyzed</div>\n");
        html.append("</div>\n");
        
        html.append("<div class='stat-card'>\n");
        html.append("<div class='stat-icon'>🔴</div>\n");
        html.append("<div class='stat-number'>").append(totalDangerous).append("</div>\n");
        html.append("<div class='stat-label'>High Risk Methods</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");

        // Upload Section
        html.append("<div class='card upload-card'>\n");
        html.append("<div class='card-header'>📤 Upload New File</div>\n");
        html.append("<div class='card-body'>\n");
        html.append("<form action='/upload' method='post' enctype='multipart/form-data' id='uploadForm'>\n");
        html.append("<div class='upload-section'>\n");
        html.append("<div class='upload-icon'>📁</div>\n");
        html.append("<label class='upload-label'>Select a .class file</label>\n");
        html.append("<div class='file-input-wrapper'>\n");
        html.append("<input type='file' name='file' id='file' accept='.class' required onchange='updateFileName()'>\n");
        html.append("<label for='file' class='file-input-label'>Choose File</label>\n");
        html.append("</div>\n");
        html.append("<div class='file-name-display' id='fileDisplay'>No file selected</div>\n");
        html.append("<button type='submit' class='submit-btn'>🔍 Scan Class</button>\n");
        html.append("</div>\n");
        html.append("</form>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        
        // Scan History
        html.append("<div class='card'>\n");
        html.append("<div class='card-header'>📜 Scan History</div>\n");
        
        if (scans.isEmpty()) {
            html.append("<div class='empty-state'>\n");
            html.append("<div class='empty-icon'>📭</div>\n");
            html.append("<p>No scans yet. Upload a file above to get started!</p>\n");
            html.append("</div>\n");
        } else {
            html.append("<div class='table-container'>\n");
            html.append("<table class='scans-table'>\n");
            html.append("<thead>\n");
            html.append("<tr>\n");
            html.append("<th>File Name</th>\n");
            html.append("<th>Scanned Date</th>\n");
            html.append("<th>Methods</th>\n");
            html.append("<th>High Risk</th>\n");
            html.append("<th>Actions</th>\n");
            html.append("</tr>\n");
            html.append("</thead>\n");
            html.append("<tbody>\n");
            
            for (DBConnection.ScanRecord scan : scans) {
                String riskLevel = scan.dangerousCount > 0 ? "🔴 High" : "🟢 Safe";
                String riskClass = scan.dangerousCount > 0 ? "risk-high" : "risk-safe";
                
                html.append("<tr>\n");
                html.append("<td class='file-name'>").append(escapeHtml(scan.fileName)).append("</td>\n");
                html.append("<td class='date'>").append(scan.createdAt).append("</td>\n");
                html.append("<td class='count'>").append(scan.methodCount).append("</td>\n");
                html.append("<td class='risk ").append(riskClass).append("'>").append(riskLevel).append(" (").append(scan.dangerousCount).append(")</td>\n");
                html.append("<td class='actions'>\n");
                html.append("<a href='/scan-detail?id=").append(scan.scanId).append("' class='btn-view'>View</a>\n");
                html.append("<form method='POST' action='/delete-scan' style='display:inline;' onsubmit='return confirm(\"Delete this scan?\")'>\n");
                html.append("<input type='hidden' name='scanId' value='").append(scan.scanId).append("'>\n");
                html.append("<button type='submit' class='btn-delete'>Delete</button>\n");
                html.append("</form>\n");
                html.append("</td>\n");
                html.append("</tr>\n");
            }
            
            html.append("</tbody>\n");
            html.append("</table>\n");
            html.append("</div>\n");
        }
        
        html.append("</div>\n");
        
        html.append("</div>\n");
        html.append("<script>\n");
        html.append("function updateFileName() {\n");
        html.append("  const fileInput = document.getElementById('file');\n");
        html.append("  const fileDisplay = document.getElementById('fileDisplay');\n");
        html.append("  if (fileInput.files && fileInput.files[0]) {\n");
        html.append("    fileDisplay.textContent = '✅ ' + fileInput.files[0].name;\n");
        html.append("    fileDisplay.classList.add('file-selected');\n");
        html.append("  } else {\n");
        html.append("    fileDisplay.textContent = 'No file selected';\n");
        html.append("    fileDisplay.classList.remove('file-selected');\n");
        html.append("  }\n");
        html.append("}\n");
        html.append("</script>\n");
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
            ".header-actions {\n" +
            "  display: flex;\n" +
            "  gap: 15px;\n" +
            "}\n" +
            ".header-actions a {\n" +
            "  padding: 10px 20px;\n" +
            "  color: white;\n" +
            "  text-decoration: none;\n" +
            "  border-radius: 5px;\n" +
            "  font-weight: bold;\n" +
            "  transition: transform 0.3s ease;\n" +
            "}\n" +
            ".history-btn {\n" +
            "  background: linear-gradient(135deg, #8b5cf6 0%, #6d28d9 100%);\n" +
            "}\n" +
            ".history-btn:hover {\n" +
            "  transform: translateY(-2px);\n" +
            "}\n" +
            ".logout {\n" +
            "  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);\n" +
            "}\n" +
            ".logout:hover {\n" +
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
            ".card-body {\n" +
            "  padding: 25px;\n" +
            "}\n" +
            ".upload-card .card-header {\n" +
            "  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);\n" +
            "}\n" +
            ".upload-section {\n" +
            "  border: 3px dashed #38bdf8;\n" +
            "  border-radius: 15px;\n" +
            "  padding: 40px;\n" +
            "  text-align: center;\n" +
            "  background: rgba(56, 189, 248, 0.05);\n" +
            "  transition: all 0.3s ease;\n" +
            "}\n" +
            ".upload-section:hover {\n" +
            "  border-color: #0ea5e9;\n" +
            "  background: rgba(56, 189, 248, 0.1);\n" +
            "}\n" +
            ".upload-icon {\n" +
            "  font-size: 3em;\n" +
            "  margin-bottom: 15px;\n" +
            "}\n" +
            ".upload-label {\n" +
            "  display: block;\n" +
            "  margin-bottom: 15px;\n" +
            "  font-size: 1.1em;\n" +
            "  font-weight: bold;\n" +
            "}\n" +
            ".file-input-wrapper {\n" +
            "  position: relative;\n" +
            "  display: inline-block;\n" +
            "  cursor: pointer;\n" +
            "  margin-bottom: 20px;\n" +
            "}\n" +
            "input[type='file'] {\n" +
            "  opacity: 0;\n" +
            "  position: absolute;\n" +
            "  width: 100%;\n" +
            "  height: 100%;\n" +
            "  cursor: pointer;\n" +
            "}\n" +
            ".file-input-label {\n" +
            "  display: inline-block;\n" +
            "  padding: 12px 30px;\n" +
            "  background: linear-gradient(135deg, #0ea5e9 0%, #06b6d4 100%);\n" +
            "  color: white;\n" +
            "  border-radius: 8px;\n" +
            "  font-weight: bold;\n" +
            "  cursor: pointer;\n" +
            "  transition: transform 0.3s ease;\n" +
            "}\n" +
            ".file-input-label:hover {\n" +
            "  transform: translateY(-2px);\n" +
            "}\n" +
            ".file-name-display {\n" +
            "  margin-top: 15px;\n" +
            "  color: #cbd5e1;\n" +
            "  font-size: 0.95em;\n" +
            "  min-height: 25px;\n" +
            "}\n" +
            ".file-selected {\n" +
            "  color: #22c55e;\n" +
            "  font-weight: bold;\n" +
            "}\n" +
            ".submit-btn {\n" +
            "  width: 100%;\n" +
            "  padding: 12px;\n" +
            "  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);\n" +
            "  border: none;\n" +
            "  border-radius: 8px;\n" +
            "  color: white;\n" +
            "  font-weight: bold;\n" +
            "  font-size: 1em;\n" +
            "  cursor: pointer;\n" +
            "  margin-top: 20px;\n" +
            "  transition: transform 0.3s ease;\n" +
            "}\n" +
            ".submit-btn:hover {\n" +
            "  transform: translateY(-2px);\n" +
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
            "  font-size: 1.1em;\n" +
            "}\n" +
            ".table-container {\n" +
            "  overflow-x: auto;\n" +
            "}\n" +
            ".scans-table {\n" +
            "  width: 100%;\n" +
            "  border-collapse: collapse;\n" +
            "}\n" +
            ".scans-table thead {\n" +
            "  background: #0f172a;\n" +
            "  border-bottom: 2px solid #334155;\n" +
            "}\n" +
            ".scans-table th {\n" +
            "  padding: 15px;\n" +
            "  text-align: left;\n" +
            "  font-weight: bold;\n" +
            "  color: #94a3b8;\n" +
            "  text-transform: uppercase;\n" +
            "  font-size: 0.9em;\n" +
            "}\n" +
            ".scans-table tbody tr {\n" +
            "  border-bottom: 1px solid #334155;\n" +
            "  transition: background 0.3s ease;\n" +
            "}\n" +
            ".scans-table tbody tr:hover {\n" +
            "  background: #0f172a;\n" +
            "}\n" +
            ".scans-table td {\n" +
            "  padding: 15px;\n" +
            "}\n" +
            ".file-name {\n" +
            "  font-family: 'Courier New', monospace;\n" +
            "  color: #38bdf8;\n" +
            "  font-weight: 500;\n" +
            "}\n" +
            ".date {\n" +
            "  color: #cbd5e1;\n" +
            "  font-size: 0.9em;\n" +
            "}\n" +
            ".count {\n" +
            "  text-align: center;\n" +
            "  font-weight: bold;\n" +
            "}\n" +
            ".risk {\n" +
            "  text-align: center;\n" +
            "  font-weight: bold;\n" +
            "  border-radius: 5px;\n" +
            "  padding: 5px 10px;\n" +
            "}\n" +
            ".risk-high {\n" +
            "  background: rgba(239, 68, 68, 0.2);\n" +
            "  color: #fca5a5;\n" +
            "}\n" +
            ".risk-safe {\n" +
            "  background: rgba(34, 197, 94, 0.2);\n" +
            "  color: #86efac;\n" +
            "}\n" +
            ".actions {\n" +
            "  display: flex;\n" +
            "  gap: 10px;\n" +
            "  justify-content: center;\n" +
            "}\n" +
            ".btn-view, .btn-delete {\n" +
            "  padding: 6px 12px;\n" +
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
            "}\n";
    }
}