package com.example.weatherprediction.ml;

import android.content.Context;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and parses historical weather observation records from assets CSV file.
 */
public class HistoricalDatasetLoader {

    public static class WeatherObservation {
        public final int dayOfYear;
        public final int month;
        public final double cityModifier;
        public final double temperature;
        public final double humidity;
        public final double precipitation;

        public WeatherObservation(int dayOfYear, int month, double cityModifier, 
                                  double temperature, double humidity, double precipitation) {
            this.dayOfYear = dayOfYear;
            this.month = month;
            this.cityModifier = cityModifier;
            this.temperature = temperature;
            this.humidity = humidity;
            this.precipitation = precipitation;
        }
    }

    /**
     * Loads historical dataset from assets folder or returns default dataset.
     */
    public static List<WeatherObservation> loadDataset(Context context) {
        List<WeatherObservation> list = new ArrayList<>();
        if (context == null) {
            return getDefaultDataset();
        }

        try {
            InputStream is = context.getAssets().open("historical_weather.csv");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip CSV Header
                    continue;
                }
                String[] tokens = line.split(",");
                if (tokens.length >= 6) {
                    int dayOfYear = Integer.parseInt(tokens[0].trim());
                    int month = Integer.parseInt(tokens[1].trim());
                    double cityMod = Double.parseDouble(tokens[2].trim());
                    double temp = Double.parseDouble(tokens[3].trim());
                    double hum = Double.parseDouble(tokens[4].trim());
                    double precip = Double.parseDouble(tokens[5].trim());

                    list.add(new WeatherObservation(dayOfYear, month, cityMod, temp, hum, precip));
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            return getDefaultDataset();
        }

        return list.isEmpty() ? getDefaultDataset() : list;
    }

    /**
     * Fallback dataset used when Android context is unavailable.
     */
    public static List<WeatherObservation> getDefaultDataset() {
        List<WeatherObservation> list = new ArrayList<>();
        list.add(new WeatherObservation(15, 1, 0.0, 3.2, 78.0, 0.45));
        list.add(new WeatherObservation(45, 2, 2.0, 10.2, 70.0, 0.25));
        list.add(new WeatherObservation(75, 3, 5.0, 21.0, 58.0, 0.10));
        list.add(new WeatherObservation(105, 4, 8.0, 32.0, 40.0, 0.05));
        list.add(new WeatherObservation(135, 5, 2.0, 25.8, 55.0, 0.20));
        list.add(new WeatherObservation(165, 6, 5.0, 34.5, 60.0, 0.25));
        list.add(new WeatherObservation(195, 7, 0.0, 29.5, 72.0, 0.55));
        list.add(new WeatherObservation(225, 8, 2.0, 31.5, 72.0, 0.50));
        list.add(new WeatherObservation(255, 9, 5.0, 31.2, 60.0, 0.25));
        list.add(new WeatherObservation(285, 10, 8.0, 31.0, 42.0, 0.10));
        list.add(new WeatherObservation(315, 11, 2.0, 14.5, 66.0, 0.30));
        list.add(new WeatherObservation(345, 12, 0.0, 4.5, 76.0, 0.50));
        return list;
    }
}
