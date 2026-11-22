package analytics_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FarmlifeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FarmlifeApplication.class, args);
	}

}
