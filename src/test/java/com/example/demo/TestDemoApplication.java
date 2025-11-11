package com.example.demo;

import org.springframework.boot.SpringApplication;

public class TestDemoApplication {

	public static void main(String[] args) {
		SpringApplication.from(DemoApplication::main).with(IntegrationTestsConfiguration.class).run(args);
	}

}
