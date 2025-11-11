package com.test.weather.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model of sunrise and sunset information.
 */
public record SunriseAndSunset(
        @JsonProperty("sunrise") long sunrise,
        @JsonProperty("sunset") long sunset
) {}