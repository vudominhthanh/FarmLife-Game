package identity_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "identity_service",
        "config",
        "controller",
        "service",
        "repository",
        "scheduler",
        "kafka",
        "messaging",
        "mapper",
        "exception",
        "idempotency",
        "commonlib"
})
@EnableScheduling
public class IdentityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}


