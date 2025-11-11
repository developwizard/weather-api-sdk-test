package com.test.weather.sdk.exceptions;

/**
 * Base class for all SDK-related checked exceptions.
 *
 * Serves as the parent for specific exception types like ApiKeyException or CityNotFoundException.
 * This ensures consistent handling of errors throughout the SDK.
 */
public class SdkException extends Exception {

    public SdkException(String message) {
        super(message);
    }

    public SdkException(String message, Throwable cause) {
        super(message, cause);
    }
}