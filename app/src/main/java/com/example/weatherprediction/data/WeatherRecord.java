package com.example.weatherprediction.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "weather_history")
public class WeatherRecord {
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String city;
    private String date;
    private double temperature;
    private double humidity;
    private double precipitation;
    private double predictedTemperature;
    private double predictedHumidity;
    private double predictedPrecipitation;
    private long timestamp;

    public WeatherRecord(String city, String date, double temperature, double humidity, 
                         double precipitation, double predictedTemperature, 
                         double predictedHumidity, double predictedPrecipitation) {
        this.city = city;
        this.date = date;
        this.temperature = temperature;
        this.humidity = humidity;
        this.precipitation = precipitation;
        this.predictedTemperature = predictedTemperature;
        this.predictedHumidity = predictedHumidity;
        this.predictedPrecipitation = predictedPrecipitation;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }

    public double getPrecipitation() { return precipitation; }
    public void setPrecipitation(double precipitation) { this.precipitation = precipitation; }

    public double getPredictedTemperature() { return predictedTemperature; }
    public void setPredictedTemperature(double predictedTemperature) { this.predictedTemperature = predictedTemperature; }

    public double getPredictedHumidity() { return predictedHumidity; }
    public void setPredictedHumidity(double predictedHumidity) { this.predictedHumidity = predictedHumidity; }

    public double getPredictedPrecipitation() { return predictedPrecipitation; }
    public void setPredictedPrecipitation(double predictedPrecipitation) { this.predictedPrecipitation = predictedPrecipitation; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
