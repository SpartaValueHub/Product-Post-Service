package com.sparta.listing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ListingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ListingServiceApplication.class, args);
	}

}
