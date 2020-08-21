package com.snap2buy.themobilebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource("classpath:base-spring-ctx.xml")
@ComponentScan("com.snap2buy.themobilebackend")
public class TheMobileBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TheMobileBackendApplication.class, args);
	}

}
