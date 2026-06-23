package com.multitenant.config;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;

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
    }
}
