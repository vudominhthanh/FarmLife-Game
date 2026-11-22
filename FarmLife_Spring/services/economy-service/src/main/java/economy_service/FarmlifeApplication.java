package economy_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "economy_service",
        "entity",
        "repository",
        "service",
        "dto",
        "mapper",
        "kafka",
        "outbox",
        "scheduler",
        "config",
        "controller",
        "commonlib",
        "exception",
        "idempotency"
})
@EnableScheduling
public class FarmlifeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FarmlifeApplication.class, args);
	}

}

