package com.test.weather.sdk.weather;

import com.fasterxml.jackson.databind.JsonNode;
import com.test.weather.sdk.model.*;

/**
 * Converts raw JSON data from OpenWeather API into a typed WeatherResponse object.
 * <p>
 * Handles the following:
 * <ul>
 *     <li>Takes the first element of the weather array.</li>
 *     <li>Provides default values if any field is missing.</li>
 *     <li>Produces a fully populated WeatherResponse object ready for SDK usage.</li>
 * </ul>
 */
public class WeatherMapper {
    /**
     * Converts a JSON tree into a WeatherResponse object.
     *
     * @param root JSON node from OpenWeather API response
     * @return WeatherResponse object with all fields populated; defaults used if missing
     */
    public static WeatherResponse toWeatherResponse(JsonNode root) {
        if (root == null || root.isMissingNode()) {
            // Return a default WeatherResponse if JSON is empty or null
            return defaultWeatherResponse();
        }

        final WeatherInfo weather = extractWeather(root.path("weather"));
        final Temperature temperature = extractTemperature(root.path("main"));
        final Wind wind = extractWind(root.path("wind"));
        final SunriseAndSunset sunriseAndSunset = extractSunriseAndSunset(root.path("sys"));

        final int visibility = root.path("visibility").asInt(0);
        final long datetime = root.path("dt").asLong(0L);
        final int timezone = root.path("timezone").asInt(0);
        final String name = root.path("name").asText("");

        return new WeatherResponse(
                weather,
                temperature,
                visibility,
                wind,
                datetime,
                sunriseAndSunset,
                timezone,
                name
        );
    }

    /**
     * Returns a default WeatherResponse with placeholder values.
     *
     * @return WeatherResponse object with default fields
     */
    private static WeatherResponse defaultWeatherResponse() {
        return new WeatherResponse(
                new WeatherInfo("", ""),
                new Temperature(Double.NaN, Double.NaN),
                0,
                new Wind(Double.NaN),
                0L,
                new SunriseAndSunset(0L, 0L),
                0,
                ""
        );
    }

    /**
     * Extracts WeatherInfo from the weather array.
     *
     * @param weatherArray JSON node containing the weather array
     * @return WeatherInfo object; defaults used if array is empty
     */
    private static WeatherInfo extractWeather(JsonNode weatherArray) {
        if (weatherArray.isArray() && !weatherArray.isEmpty()) {
            final JsonNode first = weatherArray.get(0);
            return new WeatherInfo(
                    first.path("main").asText(""),
                    first.path("description").asText("")
            );
        } else {
            return new WeatherInfo("", "");
        }
    }

    /**
     * Extracts temperature information from the "main" node.
     *
     * @param mainNode JSON node containing temperature data
     * @return Temperature object; defaults used if fields are missing
     */
    private static Temperature extractTemperature(JsonNode mainNode) {
        return new Temperature(
                mainNode.path("temp").asDouble(Double.NaN),
                mainNode.path("feels_like").asDouble(Double.NaN)
        );
    }

    /**
     * Extracts wind information from the "wind" node.
     *
     * @param windNode JSON node containing wind data
     * @return Wind object; defaults used if fields are missing
     */
    private static Wind extractWind(JsonNode windNode) {
        return new Wind(windNode.path("speed").asDouble(Double.NaN));
    }

    /**
     * Extracts sunrise and sunset information from the "sys" node.
     *
     * @param sysNode JSON node containing system data
     * @return SunriseAndSunset object; defaults used if fields are missing
     */
    private static SunriseAndSunset extractSunriseAndSunset(JsonNode sysNode) {
        return new SunriseAndSunset(
                sysNode.path("sunrise").asLong(0L),
                sysNode.path("sunset").asLong(0L)
        );
    }
}
