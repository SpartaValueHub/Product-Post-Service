package com.sparta.product_post_service.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

class MediaPropertiesBindingTest {

	// yaml 맵 키(`/` 포함)가 image/jpeg 로 바인딩되는지 확인
	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
			.withUserConfiguration(TestConfig.class)
			.withInitializer(this::loadYaml);

	@Test
	void bindsSlashContentTypeKeysAsImageJpeg() {
		contextRunner.run(context -> {
			MediaProperties properties = context.getBean(MediaProperties.class);
			assertThat(properties.resolvedExtensionByContentType())
					.containsEntry("image/jpeg", "jpg")
					.containsEntry("image/png", "png")
					.containsEntry("image/webp", "webp")
					.containsEntry("image/gif", "gif");
		});
	}

	private void loadYaml(ConfigurableApplicationContext context) {
		try {
			new YamlPropertySourceLoader()
					.load("media-extension-map", new ClassPathResource("media-extension-map.yml"))
					.forEach(source -> context.getEnvironment().getPropertySources().addFirst(source));
		} catch (IOException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Configuration
	@EnableConfigurationProperties(MediaProperties.class)
	static class TestConfig {
	}
}
