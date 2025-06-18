package com.wtplatform.backend.service.impl;

import com.wtplatform.backend.service.LoggingService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class LoggingServiceImpl implements LoggingService {
    private static final Logger logger = LoggerFactory.getLogger(LoggingServiceImpl.class);
    
    @Value("${logging.file.name}")
    private String logFilePath;
    
    @PostConstruct
    @Override
    public void initializeLogging() {
        try {
            // Print current working directory for debugging
            String currentDir = System.getProperty("user.dir");
            logger.info("Current working directory: {}", currentDir);
            
            // Print configured log file path
            logger.info("Configured log file path: {}", logFilePath);
            
            // Create logs directory if it doesn't exist
            Path logsDir = Paths.get("logs");
            if (!Files.exists(logsDir)) {
                Files.createDirectories(logsDir);
                logger.info("Created logs directory at: {}", logsDir.toAbsolutePath());
            } else {
                logger.info("Existing logs directory found at: {}", logsDir.toAbsolutePath());
            }
            
            // Get and print absolute path of log file for debugging
            Path logFile = Paths.get(logFilePath);
            Path absoluteLogPath = logFile.toAbsolutePath().normalize();
            logger.info("Absolute log file path: {}", absoluteLogPath);
            logger.info("Log file parent directory: {}", absoluteLogPath.getParent());
            
            logger.info("Logging system initialized successfully");
        } catch (IOException e) {
            logger.error("Failed to initialize logging system", e);
            throw new RuntimeException("Failed to initialize logging system", e);
        }
    }
    
    /**
     * Get the absolute path of the log file
     * @return The absolute path of the log file
     */
    public String getAbsoluteLogPath() {
        return Paths.get(logFilePath).toAbsolutePath().normalize().toString();
    }
} 