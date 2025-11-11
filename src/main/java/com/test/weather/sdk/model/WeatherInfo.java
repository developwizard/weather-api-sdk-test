package com.test.weather.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model of main weather conditions (Clouds, Rain, etc.).
 */
public record WeatherInfo(
        @JsonProperty("main") String main,
        @JsonProperty("description") String description
) {}
