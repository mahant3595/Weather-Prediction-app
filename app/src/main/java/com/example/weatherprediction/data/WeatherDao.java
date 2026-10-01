package com.example.weatherprediction.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface WeatherDao {
    @Insert
    void insert(WeatherRecord record);

    @Query("SELECT * FROM weather_history ORDER BY timestamp DESC")
    List<WeatherRecord> getAllRecords();

    @Query("SELECT * FROM weather_history WHERE city = :city ORDER BY timestamp DESC")
    List<WeatherRecord> getRecordsByCity(String city);

    @Query("DELETE FROM weather_history")
    void deleteAll();
}
