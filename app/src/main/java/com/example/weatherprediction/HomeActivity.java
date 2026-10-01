package com.example.weatherprediction;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.weatherprediction.data.AppDatabase;
import com.example.weatherprediction.data.WeatherDao;
import com.example.weatherprediction.data.WeatherRecord;
import com.example.weatherprediction.ml.WeatherPredictor;
import com.example.weatherpredictionapp.R;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private android.widget.AutoCompleteTextView citySearchInput;
    private Button searchButton;
    private Spinner citySpinner;
    
    private TextView currentCityText;
    private TextView currentTempText;
    private TextView currentHumidityText;
    private TextView currentPrecipText;
    
    private TextView predictedTempText;
    private TextView predictedHumidityText;
    private TextView predictedPrecipText;
    private Button btnViewPredictionCharts;
    private Button navHistoryBtn;
    private Button navSettingsBtn;
    private android.widget.RadioGroup homeUnitRadioGroup;
    private android.widget.RadioButton homeRadioCelsius;
    private android.widget.RadioButton homeRadioFahrenheit;
    private com.google.android.material.card.MaterialCardView currentWeatherCard;
    private com.google.android.material.card.MaterialCardView aiForecastCard;
    private android.widget.ImageView cardWeatherWatermark;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout chipsContainer;
    private TextView weatherAdvisoryBadge;
    
    private LinearLayout recentHistoryContainer;
    
    private WeatherPredictor weatherPredictor;
    private WeatherDao weatherDao;
    private String currentSelectedCity = "Mumbai";
    private String[] sortedCities;
    
    private final String[] defaultCities = {
        "Select a city...",
        // Famous Indian Cities
        "Mumbai", "Delhi", "New Delhi", "Bangalore", "Bengaluru", "Hyderabad", "Ahmedabad", "Chennai",
        "Kolkata", "Surat", "Pune", "Jaipur", "Lucknow", "Kanpur", "Nagpur", "Indore", "Bhopal",
        "Visakhapatnam", "Patna", "Vadodara", "Ghaziabad", "Ludhiana", "Agra", "Nashik", "Rajkot",
        "Varanasi", "Srinagar", "Amritsar", "Prayagraj", "Ranchi", "Coimbatore", "Vijayawada",
        "Chandigarh", "Mysore", "Gurgaon", "Gurugram", "Noida", "Bhubaneswar", "Thiruvananthapuram",
        "Dehradun", "Kochi", "Udaipur", "Shimla", "Ayodhya", "Rishikesh", "Haridwar", "Nainital",
        
        // Famous International Cities (United States, Europe, Asia, Australia, Americas, Middle East, Africa)
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio",
        "San Diego", "Dallas", "San Jose", "Austin", "San Francisco", "Seattle", "Miami", "Las Vegas",
        "London", "Manchester", "Birmingham", "Liverpool", "Edinburgh", "Paris", "Marseille", "Lyon",
        "Tokyo", "Osaka", "Kyoto", "Yokohama", "Sydney", "Melbourne", "Brisbane", "Perth",
        "Toronto", "Vancouver", "Montreal", "Ottawa", "Dubai", "Abu Dhabi", "Moscow", "Saint Petersburg",
        "Beijing", "Shanghai", "Guangzhou", "Shenzhen", "Singapore", "Rio de Janeiro", "Sao Paulo",
        "Berlin", "Munich", "Frankfurt", "Hamburg", "Rome", "Milan", "Venice", "Cairo", "Cape Town",
        "Johannesburg", "Bangkok", "Seoul", "Busan", "Kuala Lumpur", "Jakarta", "Istanbul",
        "Madrid", "Barcelona", "Amsterdam", "Zurich", "Geneva", "Vienna", "Athens", "Dublin",
        "Stockholm", "Oslo", "Copenhagen", "Helsinki", "Lisbon", "Brussels", "Warsaw", "Prague",
        "Budapest", "Auckland", "Wellington", "Buenos Aires", "Santiago", "Lima", "Bogota",
        "Mexico City", "Riyadh", "Jeddah", "Doha", "Kuwait City", "Muscat", "Kathmandu", "Colombo", "Dhaka"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize components
        weatherPredictor = new WeatherPredictor(this);
        weatherDao = AppDatabase.getDatabase(this).weatherDao();

        android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        currentSelectedCity = prefs.getString(SettingsActivity.KEY_DEFAULT_CITY, "Mumbai");

        initViews();
        setupSpinner();
        setupListeners();
        updateWeatherForCity(currentSelectedCity);
        loadHistory();
    }

    private void initViews() {
        citySearchInput = findViewById(R.id.city_search_input);
        searchButton = findViewById(R.id.search_button);
        citySpinner = findViewById(R.id.city_spinner);
        
        currentCityText = findViewById(R.id.current_city_text);
        currentTempText = findViewById(R.id.current_temp_text);
        currentHumidityText = findViewById(R.id.current_humidity_text);
        currentPrecipText = findViewById(R.id.current_precip_text);
        
        predictedTempText = findViewById(R.id.predicted_temp_text);
        predictedHumidityText = findViewById(R.id.predicted_humidity_text);
        predictedPrecipText = findViewById(R.id.predicted_precip_text);
        btnViewPredictionCharts = findViewById(R.id.btn_view_prediction_charts);
        
        navHistoryBtn = findViewById(R.id.btn_nav_history);
        navSettingsBtn = findViewById(R.id.btn_nav_settings);

        homeUnitRadioGroup = findViewById(R.id.home_unit_radio_group);
        homeRadioCelsius = findViewById(R.id.home_radio_celsius);
        homeRadioFahrenheit = findViewById(R.id.home_radio_fahrenheit);
        currentWeatherCard = findViewById(R.id.current_weather_card);
        aiForecastCard = findViewById(R.id.ai_forecast_card);
        cardWeatherWatermark = findViewById(R.id.card_weather_watermark);
        
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        chipsContainer = findViewById(R.id.chips_container);
        weatherAdvisoryBadge = findViewById(R.id.weather_advisory_badge);
        
        recentHistoryContainer = findViewById(R.id.recent_history_container);

        android.widget.ArrayAdapter<String> autoAdapter = new android.widget.ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, WeatherPredictor.SUPPORTED_CITIES);
        citySearchInput.setAdapter(autoAdapter);
        citySearchInput.setThreshold(1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedUnitPreference();
        updateWeatherForCity(currentSelectedCity);
    }

    private void loadSavedUnitPreference() {
        android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String unit = prefs.getString(SettingsActivity.KEY_UNIT, SettingsActivity.UNIT_CELSIUS);
        if (SettingsActivity.UNIT_FAHRENHEIT.equals(unit)) {
            homeRadioFahrenheit.setChecked(true);
        } else {
            homeRadioCelsius.setChecked(true);
        }
    }

    private String formatTemperature(double tempInCelsius) {
        android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String unit = prefs.getString(SettingsActivity.KEY_UNIT, SettingsActivity.UNIT_CELSIUS);
        if (SettingsActivity.UNIT_FAHRENHEIT.equals(unit)) {
            double tempF = Math.round((tempInCelsius * 1.8 + 32.0) * 10.0) / 10.0;
            return String.format(Locale.getDefault(), "%.1f °F", tempF);
        } else {
            return String.format(Locale.getDefault(), "%.1f °C", tempInCelsius);
        }
    }

    private void setupSpinner() {
        sortedCities = new String[defaultCities.length];
        sortedCities[0] = defaultCities[0]; // "Select a city..."
        System.arraycopy(defaultCities, 1, sortedCities, 1, defaultCities.length - 1);
        java.util.Arrays.sort(sortedCities, 1, sortedCities.length, java.text.Collator.getInstance(Locale.getDefault()));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, sortedCities);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        citySpinner.setAdapter(adapter);
    }

    private void setupListeners() {
        homeUnitRadioGroup.setOnCheckedChangeListener(new android.widget.RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.RadioGroup group, int checkedId) {
                android.content.SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
                android.content.SharedPreferences.Editor editor = prefs.edit();
                if (checkedId == R.id.home_radio_fahrenheit) {
                    editor.putString(SettingsActivity.KEY_UNIT, SettingsActivity.UNIT_FAHRENHEIT);
                } else {
                    editor.putString(SettingsActivity.KEY_UNIT, SettingsActivity.UNIT_CELSIUS);
                }
                editor.apply();
                updateWeatherForCity(currentSelectedCity);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String city = citySearchInput.getText().toString().trim();
                if (!city.isEmpty()) {
                    if (WeatherPredictor.isValidCity(city)) {
                        updateWeatherForCity(city);
                    } else {
                        new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this)
                                .setTitle("City Not Available")
                                .setMessage("Your city \"" + city + "\" not available, please report it.")
                                .setPositiveButton("Report Now", (dialog, which) -> {
                                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                                    intent.putExtra(SettingsActivity.EXTRA_MISSING_CITY, city);
                                    startActivity(intent);
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Please enter a city name", Toast.LENGTH_SHORT).show();
                }
            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && sortedCities != null && position < sortedCities.length) {
                    String selectedCity = sortedCities[position];
                    updateWeatherForCity(selectedCity);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        btnViewPredictionCharts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, PredictionActivity.class);
                intent.putExtra(PredictionActivity.EXTRA_CITY, currentSelectedCity);
                startActivity(intent);
            }
        });

        navHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        navSettingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        Button btnShareHome = findViewById(R.id.btn_share_home);
        btnShareHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareCurrentWeather();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateWeatherForCity(currentSelectedCity);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void shareCurrentWeather() {
        long now = System.currentTimeMillis();
        long next24h = now + (24 * 60 * 60 * 1000);
        WeatherPredictor.PredictionResult currentResult = weatherPredictor.predict(currentSelectedCity, now);
        WeatherPredictor.PredictionResult forecastResult = weatherPredictor.predict(currentSelectedCity, next24h);
        String condition = WeatherPredictor.getWeatherCondition(forecastResult.temperature, forecastResult.precipitationProbability);

        String shareText = String.format(Locale.getDefault(),
                "🌤️ SkyPredict AI Weather Forecast for %s:\n" +
                "• Current Weather: %.1f°C, Humidity: %.1f%%\n" +
                "• 24h AI Prediction: %.1f°C (%s)\n" +
                "• Rain Probability: %d%%\n\n" +
                "Powered by SkyPredict AI Engine!",
                currentSelectedCity,
                currentResult.temperature, currentResult.humidity,
                forecastResult.temperature, condition,
                (int) Math.round(forecastResult.precipitationProbability * 100));

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Weather Forecast via");
        startActivity(shareIntent);
    }

    private void updateWeatherForCity(final String city) {
        currentSelectedCity = city;
        long now = System.currentTimeMillis();
        long next24h = now + (24 * 60 * 60 * 1000);

        WeatherPredictor.PredictionResult currentResult = weatherPredictor.predict(city, now);
        WeatherPredictor.PredictionResult forecastResult = weatherPredictor.predict(city, next24h);

        if (aiForecastCard != null) {
            aiForecastCard.setCardBackgroundColor(android.graphics.Color.parseColor("#1E1E2C"));
        }

        String currentCondition = WeatherPredictor.getWeatherCondition(currentResult.temperature, currentResult.precipitationProbability);
        int cardColor = android.graphics.Color.parseColor("#1E1E2C"); // Dark default
        if (currentCondition.contains("Rainy")) {
            cardColor = android.graphics.Color.parseColor("#01579B"); // Deep Blue
        } else if (currentCondition.contains("Sunny")) {
            cardColor = android.graphics.Color.parseColor("#E65100"); // Warm Amber
        } else if (currentCondition.contains("Snowy") || currentCondition.contains("Cold")) {
            cardColor = android.graphics.Color.parseColor("#00838F"); // Teal
        } else if (currentCondition.contains("Cloudy")) {
            cardColor = android.graphics.Color.parseColor("#37474F"); // Blue Grey
        }

        if (currentCondition.contains("Rainy")) {
            cardWeatherWatermark.setImageResource(R.drawable.ic_watermark_rain);
        } else if (currentCondition.contains("Sunny")) {
            cardWeatherWatermark.setImageResource(R.drawable.ic_watermark_sun);
        } else if (currentCondition.contains("Snowy") || currentCondition.contains("Cold")) {
            cardWeatherWatermark.setImageResource(R.drawable.ic_watermark_snow);
        } else if (currentCondition.contains("Cloudy")) {
            cardWeatherWatermark.setImageResource(R.drawable.ic_watermark_cloud);
        } else {
            cardWeatherWatermark.setImageResource(R.drawable.ic_watermark_sun);
        }
        currentWeatherCard.setCardBackgroundColor(cardColor);

        // Update Weather Advisory Badge
        if (weatherAdvisoryBadge != null) {
            if (currentResult.precipitationProbability > 0.55) {
                weatherAdvisoryBadge.setText("🌧️ Heavy Rain Risk - Carry an Umbrella");
                weatherAdvisoryBadge.setBackgroundColor(android.graphics.Color.parseColor("#C62828"));
            } else if (currentResult.temperature > 38.0) {
                weatherAdvisoryBadge.setText("🔥 Heat Warning - Stay Hydrated");
                weatherAdvisoryBadge.setBackgroundColor(android.graphics.Color.parseColor("#EF6C00"));
            } else if (currentResult.temperature < 0.0) {
                weatherAdvisoryBadge.setText("❄️ Freezing Conditions - Bundle Up");
                weatherAdvisoryBadge.setBackgroundColor(android.graphics.Color.parseColor("#0277BD"));
            } else {
                weatherAdvisoryBadge.setText("☀️ Ideal Outdoor Conditions");
                weatherAdvisoryBadge.setBackgroundColor(android.graphics.Color.parseColor("#2E7D32"));
            }
        }

        // Update current weather UI
        currentCityText.setText(getString(R.string.city_label, city));
        currentTempText.setText("Temperature: " + formatTemperature(currentResult.temperature));
        currentHumidityText.setText(getString(R.string.humidity_label_value, currentResult.humidity));
        currentPrecipText.setText(getString(R.string.precip_label_value, (int) Math.round(currentResult.precipitationProbability * 100)));

        // Update predicted weather UI
        predictedTempText.setText("Predicted Temp: " + formatTemperature(forecastResult.temperature));
        predictedHumidityText.setText(getString(R.string.predicted_humidity_label, forecastResult.humidity));
        predictedPrecipText.setText(getString(R.string.predicted_precip_label, (int) Math.round(forecastResult.precipitationProbability * 100)));

        animateCardEntrance(currentWeatherCard);
        animateCardEntrance(aiForecastCard);

        // Save record to DB on background thread
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        final String dateStr = sdf.format(new Date(now));
        
        final WeatherRecord record = new WeatherRecord(
                city, dateStr, currentResult.temperature, currentResult.humidity, currentResult.precipitationProbability,
                forecastResult.temperature, forecastResult.humidity, forecastResult.precipitationProbability
        );

        new Thread(new Runnable() {
            @Override
            public void run() {
                weatherDao.insert(record);
                loadHistory();
            }
        }).start();
    }

    private void animateCardEntrance(View view) {
        if (view == null) return;
        view.setAlpha(0.0f);
        view.setTranslationY(25f);
        view.animate()
                .alpha(1.0f)
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(new android.view.animation.DecelerateInterpolator())
                .start();
    }

    private void loadHistory() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<WeatherRecord> records = weatherDao.getAllRecords();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayHistory(records);
                    }
                });
            }
        }).start();
    }

    private void displayHistory(List<WeatherRecord> records) {
        recentHistoryContainer.removeAllViews();
        if (chipsContainer != null) {
            chipsContainer.removeAllViews();
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        
        List<String> seenCities = new ArrayList<>();

        // Populate Quick Search History Chips
        if (chipsContainer != null && records != null) {
            for (WeatherRecord rec : records) {
                if (!seenCities.contains(rec.getCity())) {
                    seenCities.add(rec.getCity());
                    if (seenCities.size() > 5) break;

                    Button chip = new Button(this);
                    chip.setText("📍 " + rec.getCity());
                    chip.setTextSize(11f);
                    chip.setTextColor(android.graphics.Color.WHITE);

                    android.graphics.drawable.GradientDrawable shape = new android.graphics.drawable.GradientDrawable();
                    shape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                    shape.setCornerRadius(24f); // Smooth rounded rectangle corners
                    shape.setColor(android.graphics.Color.parseColor("#1E1E2C"));
                    chip.setBackground(shape);

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, 0, 16, 0);
                    chip.setLayoutParams(params);
                    chip.setPadding(24, 12, 24, 12);
                    chip.setOnClickListener(v -> updateWeatherForCity(rec.getCity()));
                    chipsContainer.addView(chip);
                }
            }
        }

        // Only show up to 5 recent items to avoid overcrowding
        int limit = Math.min(records.size(), 5);
        for (int i = 0; i < limit; i++) {
            final WeatherRecord record = records.get(i);
            View view = inflater.inflate(R.layout.item_recent_city, recentHistoryContainer, false);
            
            TextView cityNameTv = view.findViewById(R.id.history_city_name);
            TextView weatherInfoTv = view.findViewById(R.id.history_weather_info);
            TextView timestampTv = view.findViewById(R.id.history_timestamp);
            
            cityNameTv.setText(record.getCity());
            weatherInfoTv.setText(String.format(Locale.getDefault(), "Temp: %s | Hum: %.1f%%", formatTemperature(record.getTemperature()), record.getHumidity()));
            
            SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            timestampTv.setText(timeSdf.format(new Date(record.getTimestamp())));
            
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateWeatherForCity(record.getCity());
                }
            });
            
            recentHistoryContainer.addView(view);
        }
    }
}
