package com.uber.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

@Configuration
public class ZoneIdConfig {

    @Bean
    public ZoneId zoneId() {
        return ZoneId.systemDefault();
    }
}
