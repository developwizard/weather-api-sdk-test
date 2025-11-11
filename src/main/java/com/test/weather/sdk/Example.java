package com.test.weather.sdk;

import com.test.weather.sdk.exceptions.SdkException;
import com.test.weather.sdk.model.WeatherResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for quick testing of the SDK functionality.
 * <p>
 * This class demonstrates how to create and use {@link OpenWeatherSdk}.
 * It is not required for SDK integration — only for internal verification.
 */
public  class Example {
    private static final Logger LOGGER = Logger.getLogger(Example.class.getName());

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java -jar openweather-sdk.jar <API_KEY> <CITY> [MODE]");
            System.exit(1);
        }

        String apiKey = args[0];
        String city = args[1];
        Mode mode = Mode.ON_DEMAND;

        if (args.length >= 3) {
            try {
                mode = Mode.valueOf(args[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                LOGGER.warning("Invalid mode provided. Using ON_DEMAND by default.");
            }
        }

        try {
            OpenWeatherSdk sdk = SdkFactory.create(apiKey, mode);

            try {
                WeatherResponse response = sdk.getWeather(city);
                System.out.println("Weather in " + city + ": " + response);
                LOGGER.info("Successfully retrieved weather for " + city);
            } catch (SdkException e) {
                LOGGER.log(Level.SEVERE, "Failed to get weather for " + city, e);
            } finally {
                SdkFactory.delete(apiKey);
                LOGGER.info("SDK instance deleted");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize OpenWeatherSdk", e);
        }
    }
}