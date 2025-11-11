# 🌦️ OpenWeather SDK (Java 17)

A lightweight and reliable Java SDK for working with the [OpenWeather API](https://openweathermap.org/api).  
The SDK provides both **on-demand** and **polling** modes for retrieving and caching current weather data.

---

## Features

Fetch current weather data by city name  
Caching for up to **10 cities** with a default **10-minute TTL**  
Two operation modes:
- **ON_DEMAND** – fetches weather data only when requested
- **POLLING** – periodically updates all cached cities in background  
Automatic retries on transient API errors  
Unified exception hierarchy (`SdkException`, `CityNotFoundException`, etc.)  
Logging with `java.util.logging` via centralized `LoggingConfig`  
Thread-safe and memory-efficient caching  
Factory pattern ensures **only one SDK instance per API key**

---

## Requirements

- Java 17+
- Maven 3.8+
- Valid [OpenWeather API key](https://openweathermap.org/appid)

---

## Installation

Clone the repository and build with Maven:

```bash
git clone https://github.com/yourname/openweather-sdk.git
cd openweather-sdk
mvn clean package
```
This will produce an executable JAR file at:
```bash
target/openweather-sdk-1.0.0-jar-with-dependencies.jar
```
### Build and install locally

To build and make the SDK available to other local projects:

```bash
mvn clean install
---
```
This will install the artifact into your local Maven repository (`~/.m2/repository`).
### Add as a dependency
In another project’s pom.xml:
```xml
<dependency>
    <groupId>com.test.weather</groupId>
    <artifactId>openweather-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```
## Running Example
The SDK includes an example entry point (`Example.java`) that demonstrates how to initialize and use the OpenWeatherSdk from the command line.
```bash
java -jar openweather-sdk-1.0.0-jar-with-dependencies.jar <API_KEY> <CITY> [MODE]
```
Arguments:

-`<API_KEY>` – your OpenWeather API key (required)

-`<CITY>` – name of the city to fetch weather for (required)

-`[MODE]` – optional SDK mode:

--`on_demand` (default) – fetches and caches data on request

--`polling` – periodically updates all stored cities

You can test the SDK locally by providing your OpenWeather API key and a city name as command-line arguments.

### Example Code

```java
package org.example;

import com.test.weather.sdk.OpenWeatherSdk;
import com.test.weather.sdk.Mode;
import com.test.weather.sdk.model.WeatherResponse;
import com.test.weather.sdk.exceptions.ApiKeyException;
import com.test.weather.sdk.exceptions.SdkException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java Main <API_KEY> <CITY>");
            return;
        }

        String apiKey = args[0];
        String city = args[1];

        // Use try-with-resources to ensure proper cleanup
        try (OpenWeatherSdk sdk = new OpenWeatherSdk(apiKey, Mode.ON_DEMAND)) {
            WeatherResponse weather = sdk.getWeather(city);
            System.out.println(weather);
        } catch (ApiKeyException e) {
            System.err.println("Invalid API key: " + e.getMessage());
        } catch (SdkException e) {
            System.err.println("Failed to fetch weather: " + e.getMessage());
        }
    }
}
```