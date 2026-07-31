package aq.project;

import org.springframework.boot.SpringApplication;

public class TestPaymentProviderServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(PaymentProviderServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
