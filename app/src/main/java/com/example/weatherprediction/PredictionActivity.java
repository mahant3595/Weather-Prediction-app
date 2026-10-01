package com.example.weatherprediction;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.weatherprediction.ml.WeatherPredictor;
import com.example.weatherpredictionapp.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class PredictionActivity extends AppCompatActivity {

    public static final String EXTRA_CITY = "extra_city";

    private String selectedCity = "New York";
    private WeatherPredictor weatherPredictor;

    private TextView cityTitleText;
    private TextView predTempText;
    private TextView predHumidityText;
    private TextView predPrecipText;
    private TextView predConditionText;
    private LinearLayout multiDayContainer;
    private Button viewChartsButton;
    private ImageButton btnBackArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_CITY)) {
            String cityExtra = getIntent().getStringExtra(EXTRA_CITY);
            if (cityExtra != null && !cityExtra.trim().isEmpty()) {
                selectedCity = cityExtra;
            }
        }

        weatherPredictor = new WeatherPredictor(this);

        initViews();
        displayPredictionData();
        setupListeners();
    }

    private void initViews() {
        cityTitleText = findViewById(R.id.prediction_city_text);
        predTempText = findViewById(R.id.pred_temp_text);
        predHumidityText = findViewById(R.id.pred_humidity_text);
        predPrecipText = findViewById(R.id.pred_precip_text);
        predConditionText = findViewById(R.id.pred_condition_text);
        multiDayContainer = findViewById(R.id.multi_day_forecast_container);
        viewChartsButton = findViewById(R.id.view_charts_button);
        btnBackArrow = findViewById(R.id.btn_back_arrow);
    }

    private void displayPredictionData() {
        cityTitleText.setText(getString(R.string.city_label, selectedCity));

        long now = System.currentTimeMillis();
        long next24h = now + (24 * 60 * 60 * 1000);
        WeatherPredictor.PredictionResult next24hResult = weatherPredictor.predict(selectedCity, next24h);

        predTempText.setText(getString(R.string.predicted_temp_label, next24hResult.temperature));
        predHumidityText.setText(getString(R.string.predicted_humidity_label, next24hResult.humidity));
        predPrecipText.setText(getString(R.string.predicted_precip_label, (int) Math.round(next24hResult.precipitationProbability * 100)));
        
        String conditionStr = WeatherPredictor.getWeatherCondition(next24hResult.temperature, next24hResult.precipitationProbability);
        predConditionText.setText(String.format(Locale.getDefault(), "Expected Condition: %s", conditionStr));

        displayMultiDayForecast(now);
    }

    private void displayMultiDayForecast(long startTimestamp) {
        multiDayContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());

        for (int i = 1; i <= 7; i++) {
            long dayTime = startTimestamp + ((long) i * 24 * 60 * 60 * 1000);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dayTime);

            WeatherPredictor.PredictionResult dayResult = weatherPredictor.predict(selectedCity, dayTime);
            String dayCondition = WeatherPredictor.getWeatherCondition(dayResult.temperature, dayResult.precipitationProbability);

            View view = inflater.inflate(R.layout.item_forecast_day, multiDayContainer, false);
            TextView dayNameTv = view.findViewById(R.id.forecast_day_name);
            TextView conditionTv = view.findViewById(R.id.forecast_condition);
            TextView tempTv = view.findViewById(R.id.forecast_temp);
            TextView humPrecipTv = view.findViewById(R.id.forecast_humidity_precip);

            dayNameTv.setText(dayFormat.format(cal.getTime()));
            conditionTv.setText(String.format(Locale.getDefault(), "Condition: %s", dayCondition));
            tempTv.setText(String.format(Locale.getDefault(), "%.1f °C", dayResult.temperature));
            humPrecipTv.setText(String.format(Locale.getDefault(), "Hum: %.0f%% | Rain: %.0f%%", 
                    dayResult.humidity, dayResult.precipitationProbability * 100));

            multiDayContainer.addView(view);
            animateItemEntrance(view, i);
        }
    }

    private void animateItemEntrance(View view, int position) {
        if (view == null) return;
        view.setAlpha(0.0f);
        view.setTranslationY(20f);
        view.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setDuration(350)
                .setStartDelay((long) position * 50)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private void setupListeners() {
        viewChartsButton.setOnClickListener(v -> {
            Intent intent = new Intent(PredictionActivity.this, DetailsActivity.class);
            intent.putExtra(DetailsActivity.EXTRA_CITY, selectedCity);
            startActivity(intent);
        });

        btnBackArrow.setOnClickListener(v -> finish());
    }
}
