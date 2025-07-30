package com.aura.anime_updates;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
public class AnimeUpdatesApplication {

	public static void main(String[] args) {
		SpringApplication.run(AnimeUpdatesApplication.class, args);
	}

}
