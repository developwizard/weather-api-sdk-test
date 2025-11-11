package com.test.weather.sdk.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model of weather response returned by the SDK.
 */
public record WeatherResponse(

        @JsonProperty("weather")
        WeatherInfo weather,

        @JsonProperty("temperature")
        Temperature temperature,

        @JsonProperty("visibility")
        int visibility,

        @JsonProperty("wind")
        Wind wind,

        @JsonProperty("datetime")
        long datetime,

        @JsonProperty("sys")
        SunriseAndSunset sunriseAndSunset,

        @JsonProperty("timezone")
        int timezone,

        @JsonProperty("name")
        String name
){}
