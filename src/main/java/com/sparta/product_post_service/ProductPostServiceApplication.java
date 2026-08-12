package com.sparta.product_post_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import com.sparta.product_post_service.config.ProductPostPolicyProperties;

@EnableDiscoveryClient
@EnableConfigurationProperties(ProductPostPolicyProperties.class)
@SpringBootApplication
public class ProductPostServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductPostServiceApplication.class, args);
	}

}
