package com.bytecodetracker.config;

import com.bytecodetracker.model.User;
import com.bytecodetracker.model.UserRole;
import com.bytecodetracker.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runs once at startup.
 *
 * Problem 1 - Admin login fails:
 *   If any ADMIN-role user was manually INSERT-ed into the DB with a plain-text
 *   or non-BCrypt password, BCryptPasswordEncoder.matches() always returns false
 *   and login returns 401 forever. This runner finds ALL ADMIN users, detects
 *   invalid hashes (BCrypt always starts with "$2"), and resets them.
 *
 * Problem 2 - No admin user exists:
 *   Creates a default admin account on first run.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    // Default credentials created/reset at startup — change after first login!
    static final String DEFAULT_ADMIN_USERNAME = "admin";
    static final String DEFAULT_ADMIN_PASSWORD = "admin";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureAdminsHaveValidPasswords();
    }

    private void ensureAdminsHaveValidPasswords() {
        // Query by ROLE, not by username — we don't know what the admin was named.
        List<User> admins = userRepository.findByRole(UserRole.ADMIN);

        if (admins.isEmpty()) {
            // No admin at all — create the default one.
            User admin = User.builder()
                    .username(DEFAULT_ADMIN_USERNAME)
                    .password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD))
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            printCredentialsBanner("ADMIN USER CREATED", DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD);
            return;
        }

        // Check every ADMIN user — fix any with a non-BCrypt password.
        boolean anyFixed = false;
        for (User admin : admins) {
            boolean isBcrypt = admin.getPassword() != null
                    && admin.getPassword().startsWith("$2");

            if (!isBcrypt) {
                // Re-hash with BCrypt so login will work.
                admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
                userRepository.save(admin);
                printCredentialsBanner(
                    "ADMIN PASSWORD FIXED (was not BCrypt)",
                    admin.getUsername(),
                    DEFAULT_ADMIN_PASSWORD
                );
                anyFixed = true;
            }
        }

        if (!anyFixed) {
            admins.forEach(a ->
                log.info("[DataInitializer] Admin '{}' has a valid BCrypt password — no changes.", a.getUsername())
            );
        }
    }

    private void printCredentialsBanner(String title, String username, String password) {
        log.warn("╔══════════════════════════════════════════════════╗");
        log.warn("║  {}  ║", title);
        log.warn("║  Username : {}  ║", username);
        log.warn("║  Password : {}  ║", password);
        log.warn("║  CHANGE THIS PASSWORD AFTER FIRST LOGIN!        ║");
        log.warn("╚══════════════════════════════════════════════════╝");
    }
}
