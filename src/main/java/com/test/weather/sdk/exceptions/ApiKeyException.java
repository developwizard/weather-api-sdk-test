package com.test.weather.sdk.exceptions;

/**
 * Checked exception thrown when the provided API key is invalid, missing, or unauthorized.
 *
 * This typically indicates a configuration issue or an expired key.
 * Users of the SDK should handle this exception when initializing {@link com.test.weather.sdk.OpenWeatherSdk}.
 */
public class ApiKeyException extends SdkException {

    public ApiKeyException(String message) {
        super(message);
    }

    public ApiKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}