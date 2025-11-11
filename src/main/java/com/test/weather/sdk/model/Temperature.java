package com.test.weather.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model of temperature-related data.
 */
public record Temperature(
        @JsonProperty("temp") double temp,
        @JsonProperty("feels_like") double feelsLike
) {}