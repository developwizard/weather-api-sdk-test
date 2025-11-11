package com.test.weather.sdk;

import com.test.weather.sdk.exceptions.ApiKeyException;
import com.test.weather.sdk.exceptions.SdkException;
import com.test.weather.sdk.model.WeatherResponse;
import com.test.weather.sdk.weather.WeatherCache;
import com.test.weather.sdk.weather.WeatherClient;

import java.time.Clock;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 * Main entry point for interacting with the OpenWeather SDK.
 * <p>
 * Provides two modes of operation:
 * <ul>
 *   <li><b>ON_DEMAND</b> – fetches and caches weather data only when requested.</li>
 *   <li><b>POLLING</b> – automatically updates cached data for all stored cities at fixed intervals.</li>
 * </ul>
 *
 * <p>
 * Handles caching (up to 10 cities), retry on network errors, and exception management.
 * </p>
 */
public class OpenWeatherSdk implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(OpenWeatherSdk.class.getName());

    private final WeatherClient weatherClient;
    private final WeatherCache<WeatherResponse> cache;
    private final Mode mode;
    private final ScheduledExecutorService scheduler;

    /** Default polling interval for background updates (in minutes) */
    private static final int DEFAULT_POLL_INTERVAL_MIN = 5;

    /**
     * Creates an instance of the SDK with default TTL (10 min) and polling interval.
     *
     * @param apiKey your OpenWeather API key
     * @param mode   SDK operation mode (ON_DEMAND or POLLING)
     * @throws ApiKeyException if API key is invalid or missing
     */
    public OpenWeatherSdk(String apiKey, Mode mode) throws ApiKeyException {
        this(apiKey, mode, Duration.ofMinutes(10), DEFAULT_POLL_INTERVAL_MIN);
    }

    /**
     * Creates an instance of the SDK with custom TTL and polling interval.
     *
     * @param apiKey             OpenWeather API key
     * @param mode               SDK operation mode
     * @param ttl                time-to-live for cached data
     * @param pollIntervalMinutes interval for polling (only for POLLING mode)
     * @throws ApiKeyException if API key is null or empty
     */
    public OpenWeatherSdk(String apiKey, Mode mode, Duration ttl, int pollIntervalMinutes) throws ApiKeyException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new ApiKeyException("API key must not be null or empty");
        }
        Objects.requireNonNull(mode, "Mode must not be null");
        Objects.requireNonNull(ttl, "TTL must not be null");

        this.weatherClient = new WeatherClient(apiKey);
        this.cache = new WeatherCache<>(10, ttl, Clock.systemUTC());
        this.mode = mode;

        if (mode == Mode.POLLING) {
            this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "WeatherPollingThread");
                t.setDaemon(true);
                return t;
            });
            startPolling(pollIntervalMinutes);
        } else {
            this.scheduler = null;
        }
    }

    /**
     * Retrieves weather data for the specified city.
     *
     * ON_DEMAND mode:
     * - Returns cached data if fresh (≤ TTL)
     * - Otherwise fetches from API and caches it
     *
     * POLLING mode:
     * - Returns cached data (updated in background)
     * - If not cached, fetches once and caches
     *
     * @param city name of the city
     * @return current weather data
     * @throws SdkException if fetching fails or API error occurs
     */
    public WeatherResponse getWeather(String city) throws SdkException {
        Objects.requireNonNull(city, "City must not be null");

        Optional<WeatherResponse> cached = cache.getIfFresh(city);
        if (cached.isPresent()) {
            return cached.get();
        }

        WeatherResponse fresh = weatherClient.fetchWeatherByCity(city);
        cache.put(city, fresh);
        return fresh;
    }

    /**
     * Starts background polling for POLLING mode.
     */
    private void startPolling(int intervalMinutes) {
        if (scheduler == null) return;

        scheduler.scheduleAtFixedRate(() -> {
            Set<String> cities = cache.getStoredCities();
            for (String city : cities) {
                try {
                    final WeatherResponse updated = weatherClient.fetchWeatherByCity(city);
                    cache.put(city, updated);
                    LOGGER.fine("[Polling] Updated weather for " + city);
                } catch (SdkException e) {
                    LOGGER.warning("[Polling] Failed to update " + city + ": " + e.getMessage());
                }
            }
        }, 0, intervalMinutes, TimeUnit.MINUTES);
    }

    /**
     * Clears cached weather data.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Returns all cached cities.
     */
    public Set<String> getCachedCities() {
        return cache.getStoredCities();
    }

    /**
     * Shuts down polling (if active) and clears cache.
     */
    @Override
    public void close() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        cache.clear();
    }
}
