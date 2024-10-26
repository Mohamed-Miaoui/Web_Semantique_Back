package com.example.jardin_urbain_ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = { "com.example.jardin_urbain_ws", "com.rescuefood.semantique.CorsCongiguration" })

public class JardinUrbainWsApplication {

	public static void main(String[] args) {
		SpringApplication.run(JardinUrbainWsApplication.class, args);
	}

}
