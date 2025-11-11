package com.test.weather.sdk.exceptions;

/**
 * Checked exception thrown when the specified city cannot be found by the OpenWeather API.
 *
 * Usually occurs if the API returns a 404 HTTP status code.
 * Users of the SDK should handle this exception when calling methods like {@link com.test.weather.sdk.OpenWeatherSdk#getWeather(String)}.
 */
public class CityNotFoundException extends SdkException {
    public CityNotFoundException(String cityName) {
        super("City not found: " + cityName);
    }

    public CityNotFoundException(String cityName, Throwable cause) {
        super("City not found: " + cityName, cause);
    }
}
