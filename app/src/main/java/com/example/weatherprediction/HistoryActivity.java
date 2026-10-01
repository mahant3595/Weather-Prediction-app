package com.example.weatherprediction;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.weatherprediction.data.AppDatabase;
import com.example.weatherprediction.data.WeatherDao;
import com.example.weatherprediction.data.WeatherRecord;
import com.example.weatherprediction.ml.WeatherPredictor;
import com.example.weatherpredictionapp.R;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private WeatherDao weatherDao;
    private TextView countText;
    private TextView emptyText;
    private LinearLayout historyContainer;
    private Button clearHistoryButton;
    private Button btnExportCsv;
    private AutoCompleteTextView historySearchInput;
    private android.widget.ImageButton btnBackArrow;

    private List<WeatherRecord> allRecordsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        weatherDao = AppDatabase.getDatabase(this).weatherDao();

        initViews();
        setupListeners();
        loadFullHistory();
    }

    private void initViews() {
        countText = findViewById(R.id.history_count_text);
        emptyText = findViewById(R.id.empty_history_text);
        historyContainer = findViewById(R.id.full_history_container);
        clearHistoryButton = findViewById(R.id.btn_clear_history);
        btnExportCsv = findViewById(R.id.btn_export_csv);
        historySearchInput = findViewById(R.id.history_search_input);
        btnBackArrow = findViewById(R.id.btn_back_arrow);

        if (historySearchInput != null) {
            ArrayAdapter<String> autoAdapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_dropdown_item_1line, WeatherPredictor.SUPPORTED_CITIES);
            historySearchInput.setAdapter(autoAdapter);
            historySearchInput.setThreshold(1);
        }
    }

    private void setupListeners() {
        clearHistoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHistory();
            }
        });

        if (btnExportCsv != null) {
            btnExportCsv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    exportHistoryToCsv();
                }
            });
        }

        if (historySearchInput != null) {
            historySearchInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterHistory(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        btnBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void filterHistory(String query) {
        if (allRecordsList == null) return;
        if (query == null || query.trim().isEmpty()) {
            displayHistoryList(allRecordsList);
            return;
        }
        String lowerQuery = query.toLowerCase(Locale.getDefault()).trim();
        List<WeatherRecord> filtered = new ArrayList<>();
        for (WeatherRecord rec : allRecordsList) {
            if (rec.getCity() != null && rec.getCity().toLowerCase(Locale.getDefault()).contains(lowerQuery)) {
                filtered.add(rec);
            }
        }
        displayHistoryList(filtered);
    }

    private void exportHistoryToCsv() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<WeatherRecord> records = weatherDao.getAllRecords();
                if (records == null || records.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(HistoryActivity.this, "No history records to export", Toast.LENGTH_SHORT).show());
                    return;
                }

                try {
                    StringBuilder csvBuilder = new StringBuilder();
                    csvBuilder.append("City,Timestamp,Temperature_C,Humidity_%,Precipitation_Prob_%,Predicted_Temp_C\n");

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    for (WeatherRecord rec : records) {
                        csvBuilder.append(String.format(Locale.getDefault(),
                                "\"%s\",\"%s\",%.1f,%.1f,%d,%.1f\n",
                                rec.getCity(),
                                sdf.format(new Date(rec.getTimestamp())),
                                rec.getTemperature(),
                                rec.getHumidity(),
                                rec.getPrecipitation() > 1.0 ? (int) Math.round(rec.getPrecipitation()) : (int) Math.round(rec.getPrecipitation() * 100),
                                rec.getPredictedTemperature()));
                    }

                    File cachePath = new File(getCacheDir(), "csv");
                    cachePath.mkdirs();
                    File csvFile = new File(cachePath, "sky_predict_history_export.csv");
                    FileOutputStream out = new FileOutputStream(csvFile);
                    out.write(csvBuilder.toString().getBytes());
                    out.close();

                    android.net.Uri csvUri = androidx.core.content.FileProvider.getUriForFile(
                            HistoryActivity.this,
                            getPackageName() + ".fileprovider",
                            csvFile
                    );

                    runOnUiThread(() -> {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "SkyPredict AI Weather Log History Export");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "📊 Attached: SkyPredict AI Weather Prediction Log History CSV Export.");
                        sendIntent.putExtra(Intent.EXTRA_STREAM, csvUri);
                        sendIntent.setType("text/csv");
                        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        Intent shareIntent = Intent.createChooser(sendIntent, "Share Weather History CSV via");
                        startActivity(shareIntent);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(HistoryActivity.this, "Failed to export CSV: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            }
        }).start();
    }

    private void loadFullHistory() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<WeatherRecord> records = weatherDao.getAllRecords();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        allRecordsList = records != null ? records : new ArrayList<>();
                        displayHistoryList(allRecordsList);
                    }
                });
            }
        }).start();
    }

    private void displayHistoryList(List<WeatherRecord> records) {
        historyContainer.removeAllViews();
        int count = records != null ? records.size() : 0;
        countText.setText(String.format(Locale.getDefault(), "Total Logs: %d", count));

        if (count == 0) {
            emptyText.setVisibility(View.VISIBLE);
            return;
        }

        emptyText.setVisibility(View.GONE);
        LayoutInflater inflater = LayoutInflater.from(this);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault());

        for (final WeatherRecord record : records) {
            View view = inflater.inflate(R.layout.item_recent_city, historyContainer, false);
            TextView cityNameTv = view.findViewById(R.id.history_city_name);
            TextView weatherInfoTv = view.findViewById(R.id.history_weather_info);
            TextView timestampTv = view.findViewById(R.id.history_timestamp);

            cityNameTv.setText(record.getCity());
            weatherInfoTv.setText(String.format(Locale.getDefault(), 
                    "Temp: %.1f°C | Hum: %.1f%% | Forecast: %.1f°C", 
                    record.getTemperature(), record.getHumidity(), record.getPredictedTemperature()));
            timestampTv.setText(sdf.format(new Date(record.getTimestamp())));

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HistoryActivity.this, PredictionActivity.class);
                    intent.putExtra(PredictionActivity.EXTRA_CITY, record.getCity());
                    startActivity(intent);
                }
            });

            historyContainer.addView(view);
        }
    }

    private void clearHistory() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                weatherDao.deleteAll();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(HistoryActivity.this, R.string.history_cleared_toast, Toast.LENGTH_SHORT).show();
                        allRecordsList.clear();
                        loadFullHistory();
                    }
                });
            }
        }).start();
    }
}
