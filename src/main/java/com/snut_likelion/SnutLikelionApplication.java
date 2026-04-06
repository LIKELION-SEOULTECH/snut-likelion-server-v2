package com.snut_likelion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SnutLikelionApplication {

	public static void main(String[] args) {
		SpringApplication.run(SnutLikelionApplication.class, args);
	}

}

