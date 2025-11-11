package com.test.weather.sdk.weather;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Thread-safe LRU cache for storing weather data per city.
 * Evicts oldest entries when max size is exceeded.
 * Supports TTL (time-to-live) for cached entries.
 */
public class WeatherCache<T> {
    private final int maxEntries;
    private final Duration ttl;
    private final Clock clock;

    /** Internal entry with payload and timestamp */
    private record CacheEntry<T>(T data, Instant timestamp) {}

    /** Least Recently Used(LRU) map with access-order eviction policy */
    private final Map<String, CacheEntry<T>> cache;

    public WeatherCache() {
        this(10, Duration.ofMinutes(10), Clock.systemUTC());
    }

    public WeatherCache(int maxEntries, Duration ttl, Clock clock) {
        if (maxEntries <= 0) throw new IllegalArgumentException("maxEntries must be > 0");
        Objects.requireNonNull(ttl, "TTL must not be null");
        Objects.requireNonNull(clock, "Clock must not be null");

        this.maxEntries = maxEntries;
        this.ttl = ttl;
        this.clock = clock;

        // synchronized LinkedHashMap ensures LRU eviction and thread safety
        this.cache = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CacheEntry<T>> eldest) {
                return size() > WeatherCache.this.maxEntries;
            }
        });
    }

    /**
     * Returns a cached value if it exists and is still valid (within TTL).
     */
    public Optional<T> getIfFresh(String city) {
        Objects.requireNonNull(city, "City must not be null");
        final String key = city.toLowerCase(Locale.ROOT);

        CacheEntry<T> entry;
        synchronized (cache) {
            entry = cache.get(key);
        }

        if (entry == null) return Optional.empty();

        if (Duration.between(entry.timestamp(), clock.instant()).compareTo(ttl) > 0) {
            invalidate(city);
            return Optional.empty();
        }
        return Optional.of(entry.data());
    }

    /**
     * Puts (or replaces) a value in cache.
     * If cache exceeds maxEntries, eldest entry is evicted automatically.
     */
    public void put(String city, T data) {
        Objects.requireNonNull(city, "City must not be null");
        Objects.requireNonNull(data, "Data must not be null");
        final String key = city.toLowerCase(Locale.ROOT);
        synchronized (cache) {
            cache.put(key, new CacheEntry<>(data, clock.instant()));
        }
    }

    /**
     * Removes a specific city from cache.
     */
    public void invalidate(String city) {
        if (city != null) {
            synchronized (cache) {
                cache.remove(city.toLowerCase(Locale.ROOT));
            }
        }
    }

    /**
     * Clears the entire cache.
     */
    public void clear() {
        synchronized (cache) {
            cache.clear();
        }
    }

    /**
     * Returns a snapshot of all cached city names.
     */
    public Set<String> getStoredCities() {
        synchronized (cache) {
            return new LinkedHashSet<>(cache.keySet());
        }
    }

    /**
     * Returns whether cached value for a city is still fresh.
     */
    public boolean isFresh(String city) {
        return getIfFresh(city).isPresent();
    }
}
