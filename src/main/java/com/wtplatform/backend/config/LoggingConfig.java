package com.wtplatform.backend.config;

import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LoggingConfig {

    @PostConstruct
    public void clearLogFileOnStartup() {
        try {
            Path logFile = Paths.get("logs/application.log");
            if (Files.exists(logFile)) {
                Files.write(logFile, new byte[0]);
                log.info("Log file cleared on startup");
            }
        } catch (IOException e) {
            log.warn("Could not clear log file on startup: {}", e.getMessage());
        }
    }
} 