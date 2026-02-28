package com.example.sitema_de_turnos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SitemaDeTurnosApplication {

	public static void main(String[] args) {
		SpringApplication.run(SitemaDeTurnosApplication.class, args);
	}

}
