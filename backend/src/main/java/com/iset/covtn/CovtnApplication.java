package com.iset.covtn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
public class CovtnApplication {
	public static final String currentLocation = System.getProperty("user.dir");

	public static void main(String[] args) {
		SpringApplication.run(CovtnApplication.class, args);
	}

}
