package com.sentinela.alpr;

import org.springframework.boot.SpringApplication;

public class TestAlprApplication {

	public static void main(String[] args) {
		SpringApplication.from(AlprApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
