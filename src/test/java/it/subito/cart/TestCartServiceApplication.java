package it.subito.cart;

import org.springframework.boot.SpringApplication;

public class TestCartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.from(CartServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
