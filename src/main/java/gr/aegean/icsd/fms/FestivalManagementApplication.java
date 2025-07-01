package gr.aegean.icsd.fms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for the Festival Management System.
 * This class bootstraps the Spring Boot application and enables
 * necessary features like JPA auditing and transaction management.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableTransactionManagement
public class FestivalManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(FestivalManagementApplication.class, args);
    }

    /**
     * Bean definition for password encoder used throughout the application
     * for encoding user passwords.
     * 
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}