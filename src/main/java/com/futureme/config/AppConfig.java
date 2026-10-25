package com.futureme.config;
 
import java.time.Clock;
import java.time.ZoneId;
 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
/**
 * Application-wide beans.
 *
 * The Clock is the single source of "what time is it now?" for the whole
 * application. Using one Clock (instead of calling LocalDateTime.now() all over
 * the place) keeps time handling consistent and makes unit tests easy,
 * because a test can supply a fixed Clock.
 */
@Configuration
public class AppConfig {
 
    @Bean
    public Clock clock(@Value("${app.timezone:Asia/Kolkata}") String timezone) {
        return Clock.system(ZoneId.of(timezone));
    }
}
