package com.multitenant.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EmbeddedPostgresEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static EmbeddedPostgres postgres;

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (postgres == null) {
            try {
                // Ensure the mock database runs on port 5433 while tests are running
                postgres = EmbeddedPostgres.builder().setPort(5433).start();
                
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        postgres.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
            } catch (IOException e) {
                throw new RuntimeException("Could not start Embedded Postgres on port 5433", e);
            }
        }

        // Dynamically override datasource configuration for all Spring Boot integration tests
        Map<String, Object> map = new HashMap<>();
        map.put("spring.datasource.url", "jdbc:postgresql://localhost:5433/postgres");
        map.put("spring.datasource.username", "postgres");
        map.put("spring.datasource.password", "postgres");
        map.put("spring.datasource.driver-class-name", "org.postgresql.Driver");

        environment.getPropertySources().addFirst(new MapPropertySource("embeddedPostgresProperties", map));
    }
}
