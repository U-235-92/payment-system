package aq.project;

import org.springframework.boot.SpringApplication;

public class TestUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(PersonServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
