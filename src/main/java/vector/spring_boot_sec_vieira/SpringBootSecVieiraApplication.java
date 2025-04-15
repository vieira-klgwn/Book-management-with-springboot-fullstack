package vector.spring_boot_sec_vieira;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import vector.spring_boot_sec_vieira.auth.AuthenticationService;
import vector.spring_boot_sec_vieira.auth.RegisterRequest;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class SpringBootSecVieiraApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSecVieiraApplication.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(AuthenticationService service) {
		return args -> {
			var admin = RegisterRequest.builder()
					.firstName("vieira")
					.lastName("ntwali")
					.email("vieira@ntwali.com")
					.password("admin@123")
					.build();
			System.out.println("Admin token: " + service.register(admin).getAccessToken());
			var manager = RegisterRequest.builder()
					.firstName("vieira")
					.lastName("ntwali")
					.email("manager@vieira.com")
					.password("manager@123")
					.build();
			System.out.println("Manager token: " + service.register(manager).getAccessToken());

		};
	}

}
