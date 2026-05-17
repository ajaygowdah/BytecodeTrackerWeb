package com.bytecodetracker.config;

import com.bytecodetracker.model.User;
import com.bytecodetracker.model.UserRole;
import com.bytecodetracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode(DataInitializer.DEFAULT_ADMIN_PASSWORD))
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("[dev] Created default admin/admin user");
        }
    }
}
