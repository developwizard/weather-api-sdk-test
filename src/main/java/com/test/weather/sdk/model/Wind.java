package com.test.weather.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model of Wind information.
 */
public record Wind(
        @JsonProperty("speed") double speed
) {}
