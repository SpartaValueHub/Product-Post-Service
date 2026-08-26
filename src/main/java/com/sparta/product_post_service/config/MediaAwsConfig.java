package com.sparta.product_post_service.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import lombok.extern.slf4j.Slf4j;

// S3 Presigner — EC2 Instance Role / 로컬 DefaultCredentialsProvider
@Slf4j
@Configuration
@EnableConfigurationProperties(MediaProperties.class)
public class MediaAwsConfig {

	@Bean(destroyMethod = "close")
	public S3Presigner s3Presigner(MediaProperties mediaProperties) {
		String region = mediaProperties.awsRegion() == null || mediaProperties.awsRegion().isBlank()
				? "ap-northeast-2"
				: mediaProperties.awsRegion();
		log.info(
				"app.media.extension-by-content-type loaded keys={}",
				mediaProperties.resolvedExtensionByContentType().keySet()
		);
		return S3Presigner.builder()
				.region(Region.of(region))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}

	@Bean(destroyMethod = "close")
	public S3Client s3Client(MediaProperties mediaProperties) {
		String region = mediaProperties.awsRegion() == null || mediaProperties.awsRegion().isBlank()
				? "ap-northeast-2"
				: mediaProperties.awsRegion();
		return S3Client.builder()
				.region(Region.of(region))
				.credentialsProvider(DefaultCredentialsProvider.create())
				.build();
	}
}
