# 🔍 BytecodeTrackerWeb — Java Bytecode Security Analyzer

BytecodeTrackerWeb is a Java-based web application that lets you upload compiled `.class` files, analyzes them for dangerous or suspicious bytecode patterns using Java Reflection, and returns a detailed security report — all served through a lightweight built-in HTTP server with user authentication and scan history.

---

## ✨ Features

- 🔐 **User Authentication** — Register and login with session-based access control
- 📦 **Bytecode Upload** — Upload any compiled `.class` file directly from the browser
- 🧠 **Multi-Tier Analysis** — Flags methods as Dangerous, Suspicious, or Safe
- ⚠️ **Risk Classification** — HIGH / MEDIUM / LOW risk levels with human-readable reasons
- 🗂️ **Scan History** — View all previous scans from your dashboard
- 📊 **Detailed Reports** — Rich HTML report with stat cards, risk breakdown, and recommendations
- 🌑 **Dark UI** — Clean dark-themed frontend

---

## 🏗️ Architecture

```
Browser (port 8080)
       │
       ▼
┌──────────────────┐
│   WebServer.java  │  ← Handles routing, sessions, file uploads
└────────┬─────────┘
         │
         ▼
┌──────────────────────┐
│ BytecodeAnalyzer.java │  ← Reflects over methods, classifies risk
└────────┬─────────────┘
         │
         ▼
┌──────────────────────┐       ┌───────────────────────┐
│ CustomClassLoader.java│       │    MySQL Database      │
│ (loads .class files) │       │ users, scans,violations│
└──────────────────────┘       └───────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java (`com.sun.net.httpserver`) |
| Analysis Engine | Java Reflection API + Custom ClassLoader |
| Database | MySQL 8.0+ via JDBC |
| Frontend | HTML, CSS (dark theme) |
| Session Management | UUID-based in-memory session store |

---

## 📁 Project Structure

```
BytecodeTrackerWeb/
├── server/
│   ├── WebServer.java            # HTTP server, routing, session management
│   ├── BytecodeAnalyzer.java     # Core analysis engine (risk detection)
│   ├── CustomClassLoader.java    # Loads uploaded .class files at runtime
│   ├── DBConnection.java         # MySQL connection and queries
│   ├── ScanHistory.java          # Scan history retrieval
│   └── UserDashboard.java        # Dashboard data handler
├── resources/
│   ├── login.html
│   ├── register.html
│   └── index.html
├── projects/                     # Sample .class and .java test files
├── uploads/                      # Uploaded .class files (runtime-generated)
├── CMS_Project/                  # Bonus CMS sub-project
└── README.md
```

---

## 🚀 Running Locally

### Prerequisites

- Java JDK 11 or higher
- MySQL 8.0+
- MySQL Connector/J JAR (place in `lib/`)

### 1. Clone the Repository

```bash
git clone https://github.com/ajaygowdah/BytecodeTrackerWeb.git
cd BytecodeTrackerWeb
```

### 2. Set Up the Database

Open MySQL and run:

```sql
CREATE DATABASE bytecode_db;
USE bytecode_db;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE scans (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    filename VARCHAR(255),
    scan_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE violations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    scan_id INT,
    method_name VARCHAR(255),
    risk_level VARCHAR(20),
    reason VARCHAR(255)
);
```

### 3. Configure Database Credentials

Open `server/DBConnection.java` and update:

```java
private static final String DB_URL      = "jdbc:mysql://localhost:3306/bytecode_db";
private static final String DB_USER     = "your_username";
private static final String DB_PASSWORD = "your_password";
```

### 4. Compile and Run

```bash
# On Linux / Mac
javac -cp lib/mysql-connector.jar server/*.java
java -cp .:lib/mysql-connector.jar server.WebServer

# On Windows
javac -cp lib\mysql-connector.jar server\*.java
java -cp .;lib\mysql-connector.jar server.WebServer
```

Open your browser and go to:
```
http://localhost:8080
```

---

## 🔌 API Routes

| Method | Route | Description |
|---|---|---|
| GET | `/` | Serves login page |
| GET/POST | `/register` | User registration |
| POST | `/login` | Authenticates user, sets session cookie |
| GET | `/dashboard` | User dashboard with scan history |
| POST | `/upload` | Upload `.class` file and trigger analysis |

---

## 🧪 Sample Test Files

The `projects/` folder contains ready-to-use `.class` files for testing:

| File | Expected Result |
|---|---|
| `SafeTest.class` | All methods → Safe |
| `DangerousTest1.class` | HIGH risk violations |
| `DangerousTest2.class` | HIGH risk violations |
| `UnsafeTest.class` | MEDIUM / HIGH risk |
| `SampleTest.class` | Mixed results |

---

## 🔍 How Detection Works

The analyzer reflects over all declared methods in the uploaded class and flags:

| Risk Level | Triggered By |
|---|---|
| 🔴 **Dangerous** | `exec`, `system`, `exit`, `runtime`, native/JNI calls, reflection abuse |
| 🟡 **Suspicious** | File I/O, network calls, high parameter count methods |
| 🟢 **Safe** | Everything else |

Results are stored in the `violations` table and rendered as a full HTML report with stat cards and recommendations.

---

## ⚙️ Environment Variables

> Currently credentials are hardcoded in `DBConnection.java`. Before deploying, replace them with environment variables:

| Variable | Description |
|---|---|
| `DB_URL` | JDBC connection string |
| `DB_USER` | MySQL username |
| `DB_PASSWORD` | MySQL password |

---

## ⚠️ Known Limitations

- **Arbitrary class execution** — Uploaded `.class` files are loaded into the JVM. Static initializers run at load time. Do not use in production without sandboxing.
- **Plaintext passwords** — Password hashing (e.g. BCrypt) is not yet implemented.
- **In-memory sessions** — Sessions are lost on server restart and are not thread-safe under high concurrency.
- **Name-based detection** — A malicious class can evade detection by using non-flagged method names.

---

## 👤 Author

**Ajay Gowda H**
GitHub: [@ajaygowdah](https://github.com/ajaygowdah)

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
