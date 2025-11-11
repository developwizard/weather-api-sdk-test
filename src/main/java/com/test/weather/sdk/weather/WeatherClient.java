package com.test.weather.sdk.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.weather.sdk.exceptions.ApiKeyException;
import com.test.weather.sdk.exceptions.CityNotFoundException;
import com.test.weather.sdk.exceptions.SdkException;
import com.test.weather.sdk.model.WeatherResponse;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP client for accessing the OpenWeather API.
 *
 * Handles request building, response parsing, and retry logic for transient errors.
 */
public class WeatherClient {
    private static final Logger LOGGER = Logger.getLogger(WeatherClient.class.getName());

    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;
    private final String baseUrl;
    private final Duration timeout;
    private final int maxRetries;

    public WeatherClient(String apiKey) {
        this(apiKey, "https://api.openweathermap.org", Duration.ofSeconds(10), 3);
    }

    public WeatherClient(String apiKey, String baseUrl, Duration timeout, int maxRetries) {
        this.apiKey = Objects.requireNonNull(apiKey, "API key must not be null");
        this.baseUrl = Objects.requireNonNull(baseUrl, "Base URL must not be null");
        this.timeout = Objects.requireNonNull(timeout, "Timeout must not be null");
        if (maxRetries < 1) throw new IllegalArgumentException("maxRetries must be >= 1");
        this.maxRetries = maxRetries;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();
    }

    /**
     * Fetches current weather data for the specified city.
     *
     * @param city city name
     * @return WeatherResponse object
     * @throws SdkException if network, parsing or API error occurs
     */
    public WeatherResponse fetchWeatherByCity(String city) throws SdkException {
        Objects.requireNonNull(city, "City must not be null");

        final String url = String.format("%s/data/2.5/weather?q=%s&appid=%s&units=metric",
                baseUrl, URLEncoder.encode(city, StandardCharsets.UTF_8), apiKey);

        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .GET()
                .build();

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                final HttpResponse<String> response =
                        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                final int status = response.statusCode();

                switch (status) {
                    case 200 -> {
                        final JsonNode rawJson = mapper.readTree(response.body());
                        return WeatherMapper.toWeatherResponse(rawJson);
                    }
                    case 401, 403 -> throw new ApiKeyException("Invalid or unauthorized API key");
                    case 404 -> throw new CityNotFoundException(city);
                    case 429, 500, 502, 503, 504 -> {
                        // Retry on transient server errors
                        if (attempt < maxRetries) {
                            exponentialBackoff(attempt);
                            continue;
                        }
                        throw new SdkException("Temporary API error after retries: " + status);
                    }
                    default -> throw new SdkException("Unexpected API response: " + status);
                }
            } catch (IOException e) {
                if (attempt == maxRetries) throw new SdkException("IO error after retries", e);
                LOGGER.log(Level.WARNING, "IO error, retrying attempt {0} for city {1}", new Object[]{attempt, city});
                exponentialBackoff(attempt);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SdkException("Request interrupted", e);
            }
        }

        throw new SdkException("Failed to fetch weather after retries");
    }

    /** simple exponential backoff: waits 500ms * attempt */
    private void exponentialBackoff(int attempt) {
        try {
            Thread.sleep(attempt * 500L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Exponential backoff interrupted for attempt {0}", attempt);
        }
    }
}