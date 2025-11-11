package com.test.weather.sdk;

/**
 * Enum representing the operation mode of the OpenWeather SDK.
 *
 * <ul>
 *   <li>{@code ON_DEMAND} – fetches and caches weather data only when requested.</li>
 *   <li>{@code POLLING} – automatically updates cached data for all stored cities at fixed intervals.</li>
 * </ul>
 */
public enum Mode {
    ON_DEMAND,
    POLLING
}