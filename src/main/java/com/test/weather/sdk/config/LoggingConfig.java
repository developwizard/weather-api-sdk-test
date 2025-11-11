package com.test.weather.sdk.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * Centralized logger configuration for the SDK.
 * Loads configuration from logging.properties in resources.
 */
public final class LoggingConfig {

    private LoggingConfig() {
    }

    /** Initializes logging configuration. Should be called once. */
    public static void init() {
        try (InputStream configFile = LoggingConfig.class.getResourceAsStream("/logging.properties")) {
            if (configFile != null) {
                LogManager.getLogManager().readConfiguration(configFile);
            } else {
                System.err.println("logging.properties not found in resources!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}