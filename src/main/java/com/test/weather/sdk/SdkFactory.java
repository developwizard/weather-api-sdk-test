package com.test.weather.sdk;

import com.test.weather.sdk.exceptions.ApiKeyException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating and managing OpenWeatherSdk instances.
 *
 * Ensures that only one SDK instance exists per API key.
 * Provides methods to create, retrieve, and delete SDK instances.
 */
public class SdkFactory {

    /** Stores SDK instances keyed by API key */
    private static final Map<String, OpenWeatherSdk> instances = new ConcurrentHashMap<>();

    /**
     * Creates a new OpenWeatherSdk instance for the specified API key and mode.
     *
     * @param apiKey API key for OpenWeather API
     * @param mode   operation mode (ON_DEMAND or POLLING)
     * @return newly created OpenWeatherSdk instance
     * @throws ApiKeyException if an SDK instance with the same API key already exists
     */
    public static OpenWeatherSdk create(String apiKey, Mode mode) throws ApiKeyException {
        if (instances.containsKey(apiKey)) {
            throw new ApiKeyException("SDK instance with this API key already exists");
        }
        final OpenWeatherSdk sdk = new OpenWeatherSdk(apiKey, mode);
        instances.put(apiKey, sdk);
        return sdk;
    }

    /**
     * Retrieves an existing OpenWeatherSdk instance for the given API key.
     *
     * @param apiKey API key
     * @return Optional containing the SDK instance if present, empty otherwise
     */
    public static Optional<OpenWeatherSdk> get(String apiKey) {
        return Optional.ofNullable(instances.get(apiKey));
    }

    /**
     * Deletes and closes the SDK instance associated with the given API key.
     *
     * @param apiKey API key of the instance to remove
     */
    public static void delete(String apiKey) {
        final OpenWeatherSdk sdk = instances.remove(apiKey);
        if (sdk != null) {
            sdk.close();
        }
    }
}