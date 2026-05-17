package com.bytecodetracker;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BytecodeTrackerApplication {

    public static void main(String[] args) {
        // Load environment variables from .env file
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        
        // Map .env variables to Spring properties
        dotenv.entries().forEach(entry -> {
            String key = entry.getKey();
            String value = entry.getValue();
            
            // Set as system properties
            System.setProperty(key, value);
            
            // Also set as environment-like properties for Spring
            // Map DB_* to spring.datasource.* properties
            if (key.equals("DB_URL")) {
                System.setProperty("spring.datasource.url", value);
            } else if (key.equals("DB_USER")) {
                System.setProperty("spring.datasource.username", value);
            } else if (key.equals("DB_PASS")) {
                System.setProperty("spring.datasource.password", value);
            }
        });
        
        SpringApplication.run(BytecodeTrackerApplication.class, args);
    }
}
