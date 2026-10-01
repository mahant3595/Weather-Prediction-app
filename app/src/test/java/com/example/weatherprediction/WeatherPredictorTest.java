package com.example.weatherprediction;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.weatherprediction.ml.WeatherPredictor;

import org.junit.Test;

public class WeatherPredictorTest {
    @Test
    public void testPrediction() {
        WeatherPredictor predictor = new WeatherPredictor();
        WeatherPredictor.PredictionResult result = predictor.predict("London", System.currentTimeMillis());
        
        assertNotNull(result);
        System.out.println("Temp: " + result.temperature);
        System.out.println("Humidity: " + result.humidity);
        System.out.println("Precipitation: " + result.precipitationProbability);
        
        assertTrue(result.temperature >= -10.0 && result.temperature <= 50.0);
        assertTrue(result.humidity >= 0.0 && result.humidity <= 100.0);
        assertTrue(result.precipitationProbability >= 0.0 && result.precipitationProbability <= 1.0);
    }

    @Test
    public void testWeatherConditionLogic() {
        String rainyCondition = WeatherPredictor.getWeatherCondition(22.0, 0.75);
        assertTrue(rainyCondition.contains("Rainy"));

        String cloudyCondition = WeatherPredictor.getWeatherCondition(18.0, 0.40);
        assertTrue(cloudyCondition.contains("Cloudy"));

        String sunnyCondition = WeatherPredictor.getWeatherCondition(25.0, 0.10);
        assertTrue(sunnyCondition.contains("Sunny"));

        String snowyCondition = WeatherPredictor.getWeatherCondition(-2.0, 0.10);
        assertTrue(snowyCondition.contains("Snowy"));
    }

    @Test
    public void testCityValidation() {
        assertTrue(WeatherPredictor.isValidCity("London"));
        assertTrue(WeatherPredictor.isValidCity("Tokyo"));
        assertTrue(WeatherPredictor.isValidCity("New York"));
        assertTrue(WeatherPredictor.isValidCity("Mumbai"));
        assertTrue(WeatherPredictor.isValidCity("Delhi"));
        assertTrue(WeatherPredictor.isValidCity("Pune"));
        assertTrue(WeatherPredictor.isValidCity("Bengaluru"));
        assertTrue(WeatherPredictor.isValidCity("Kolkata"));
        assertTrue(WeatherPredictor.isValidCity("Shimla"));
        assertTrue(WeatherPredictor.isValidCity("Surat"));

        org.junit.Assert.assertFalse(WeatherPredictor.isValidCity("screeen"));
        org.junit.Assert.assertFalse(WeatherPredictor.isValidCity("invalid_city_xyz"));
        org.junit.Assert.assertFalse(WeatherPredictor.isValidCity(""));
        org.junit.Assert.assertFalse(WeatherPredictor.isValidCity(null));
    }
}
