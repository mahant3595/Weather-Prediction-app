package com.example.weatherprediction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.weatherprediction.ml.WeatherPredictor;
import com.example.weatherpredictionapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "weather_prefs";
    public static final String KEY_UNIT = "temp_unit";
    public static final String KEY_DEFAULT_CITY = "default_city";
    public static final String UNIT_CELSIUS = "celsius";
    public static final String UNIT_FAHRENHEIT = "fahrenheit";
    public static final String EXTRA_MISSING_CITY = "extra_missing_city";

    private ImageButton btnBackArrow;
    private FloatingActionButton fabShare;
    private EditText inputReportMessage;
    private Button btnSubmitReport;
    private AutoCompleteTextView inputDefaultCity;
    private Button btnSaveDefaultCity;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        handleIncomingExtras();
        setupListeners();
    }

    private void initViews() {
        btnBackArrow = findViewById(R.id.btn_back_arrow);
        fabShare = findViewById(R.id.fab_share);
        inputReportMessage = findViewById(R.id.input_report_message);
        btnSubmitReport = findViewById(R.id.btn_submit_report);
        inputDefaultCity = findViewById(R.id.input_default_city);
        btnSaveDefaultCity = findViewById(R.id.btn_save_default_city);

        ArrayAdapter<String> autoAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, WeatherPredictor.SUPPORTED_CITIES);
        inputDefaultCity.setAdapter(autoAdapter);
        inputDefaultCity.setThreshold(1);

        String savedDefaultCity = preferences.getString(KEY_DEFAULT_CITY, "Mumbai");
        inputDefaultCity.setText(savedDefaultCity);
    }

    private void handleIncomingExtras() {
        if (getIntent() != null && getIntent().hasExtra(EXTRA_MISSING_CITY)) {
            String missingCity = getIntent().getStringExtra(EXTRA_MISSING_CITY);
            if (missingCity != null && !missingCity.trim().isEmpty()) {
                inputReportMessage.setText("My city \"" + missingCity.trim() + "\" is not available in the weather app. Please add it.");
                inputReportMessage.setSelection(inputReportMessage.getText().length());
            }
        }
    }

    private void setupListeners() {
        btnBackArrow.setOnClickListener(v -> finish());

        btnSaveDefaultCity.setOnClickListener(v -> {
            String city = inputDefaultCity.getText().toString().trim();
            if (city.isEmpty()) {
                Toast.makeText(SettingsActivity.this, "Please enter a default city name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (WeatherPredictor.isValidCity(city)) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(KEY_DEFAULT_CITY, city);
                editor.apply();
                Toast.makeText(SettingsActivity.this, "Default startup city saved: " + city, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SettingsActivity.this, R.string.city_not_available, Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmitReport.setOnClickListener(v -> {
            String reportText = inputReportMessage.getText().toString().trim();
            if (reportText.isEmpty()) {
                Toast.makeText(SettingsActivity.this, "Please enter a message or missing city name", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                Uri uri = Uri.parse("mailto:maxtamil5553@gmail.com" +
                        "?subject=" + Uri.encode("Missing City / Weather Report - SkyPredict AI") +
                        "&body=" + Uri.encode(reportText));
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, uri);

                startActivity(emailIntent);

                // Show Success Dialog with Green Tick Symbol (✓) explaining user needs to tap Send
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("✓ Report Prepared Successfully")
                        .setMessage("Your email app has opened with your report addressed to maxtamil5553@gmail.com.\n\nPlease tap 'Send' inside your email app to finish sending the report!")
                        .setPositiveButton("OK", (dialog, which) -> inputReportMessage.setText(""))
                        .show();

            } catch (Exception e) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Report Not Sent")
                        .setMessage("Error occurred: Report not sent, try again or later.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        });

        fabShare.setOnClickListener(v -> {
            try {
                String sourceDir = getApplicationInfo().sourceDir;
                File sourceFile = new File(sourceDir);

                File cachePath = new File(getCacheDir(), "apk");
                if (!cachePath.exists()) {
                    boolean created = cachePath.mkdirs();
                    if (!created) {
                        Toast.makeText(SettingsActivity.this, "Failed to create APK cache directory", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                File destFile = new File(cachePath, "SkyPredictAI.apk");

                FileInputStream in = new FileInputStream(sourceFile);
                FileOutputStream out = new FileOutputStream(destFile);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.close();

                Uri apkUri = FileProvider.getUriForFile(
                        SettingsActivity.this,
                        getPackageName() + ".fileprovider",
                        destFile
                );

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Download SkyPredict AI App (APK)");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "📥 Install SkyPredict AI App and experience offline local machine learning weather prediction!");
                sendIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
                sendIntent.setType("application/vnd.android.package-archive");
                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                Intent shareIntent = Intent.createChooser(sendIntent, "Share App APK via");
                startActivity(shareIntent);
            } catch (Exception e) {
                Toast.makeText(SettingsActivity.this, "Failed to share app APK: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
