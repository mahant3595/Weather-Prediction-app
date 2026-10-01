package com.example.weatherprediction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.example.weatherprediction.data.WeatherRecord;

import org.junit.Test;

public class WeatherRecordTest {

    @Test
    public void testWeatherRecordCreationAndGetters() {
        WeatherRecord record = new WeatherRecord(
                "Tokyo", "2026-09-26 14:00", 22.5, 65.0, 0.20,
                24.0, 60.0, 0.15
        );

        assertNotNull(record);
        assertEquals("Tokyo", record.getCity());
        assertEquals("2026-09-26 14:00", record.getDate());
        assertEquals(22.5, record.getTemperature(), 0.001);
        assertEquals(65.0, record.getHumidity(), 0.001);
        assertEquals(0.20, record.getPrecipitation(), 0.001);
        assertEquals(24.0, record.getPredictedTemperature(), 0.001);
        assertEquals(60.0, record.getPredictedHumidity(), 0.001);
        assertEquals(0.15, record.getPredictedPrecipitation(), 0.001);
    }
}
