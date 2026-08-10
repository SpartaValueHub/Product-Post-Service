package com.sparta.listing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.sparta.listing_service.config.ListingPolicyProperties;

@EnableDiscoveryClient
@EnableConfigurationProperties(ListingPolicyProperties.class)
@SpringBootApplication
public class ListingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ListingServiceApplication.class, args);
	}

}
