package com.example.weatherprediction;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.weatherprediction.ml.WeatherPredictor;
import com.example.weatherpredictionapp.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    public static final String EXTRA_CITY = "extra_city";

    private String selectedCity = "New York";
    private WeatherPredictor weatherPredictor;

    private TextView citySubtitleText;
    private LineChart temperatureChart;
    private LineChart humidityPrecipChart;
    private BarChart precipBarChart;
    private android.widget.ImageButton btnBackArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_CITY)) {
            String cityExtra = getIntent().getStringExtra(EXTRA_CITY);
            if (cityExtra != null && !cityExtra.trim().isEmpty()) {
                selectedCity = cityExtra;
            }
        }

        weatherPredictor = new WeatherPredictor(this);

        initViews();
        setupCharts();
        setupListeners();
    }

    private void initViews() {
        citySubtitleText = findViewById(R.id.details_city_subtitle);
        temperatureChart = findViewById(R.id.temperature_chart);
        humidityPrecipChart = findViewById(R.id.humidity_precip_chart);
        precipBarChart = findViewById(R.id.precip_bar_chart);
        btnBackArrow = findViewById(R.id.btn_back_arrow);

        citySubtitleText.setText(getString(R.string.city_label, selectedCity));
    }

    private void setupCharts() {
        long startTimestamp = System.currentTimeMillis();
        SimpleDateFormat daySdf = new SimpleDateFormat("EEE", Locale.getDefault());

        List<String> dayLabels = new ArrayList<>();
        List<Entry> tempEntries = new ArrayList<>();
        List<Entry> humidityEntries = new ArrayList<>();
        List<Entry> precipEntries = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            long time = startTimestamp + ((long) i * 24 * 60 * 60 * 1000);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(time);

            dayLabels.add(daySdf.format(cal.getTime()));

            WeatherPredictor.PredictionResult result = weatherPredictor.predict(selectedCity, time);
            tempEntries.add(new Entry(i, (float) result.temperature));
            humidityEntries.add(new Entry(i, (float) result.humidity));
            precipEntries.add(new Entry(i, (float) (result.precipitationProbability * 100.0)));
        }

        // Setup Temperature Chart
        LineDataSet tempDataSet = new LineDataSet(tempEntries, "Temperature (°C)");
        tempDataSet.setColor(Color.parseColor("#FF5722"));
        tempDataSet.setCircleColor(Color.parseColor("#FF5722"));
        tempDataSet.setLineWidth(2.5f);
        tempDataSet.setCircleRadius(4.5f);
        tempDataSet.setValueTextSize(10f);
        tempDataSet.setValueTextColor(Color.parseColor("#333333"));

        LineData tempLineData = new LineData(tempDataSet);
        configureChartStyle(temperatureChart, tempLineData, dayLabels);

        // Setup Humidity & Precipitation Chart
        LineDataSet humidityDataSet = new LineDataSet(humidityEntries, "Humidity (%)");
        humidityDataSet.setColor(Color.parseColor("#00BCD4"));
        humidityDataSet.setCircleColor(Color.parseColor("#00BCD4"));
        humidityDataSet.setLineWidth(2.0f);
        humidityDataSet.setCircleRadius(4.0f);
        humidityDataSet.setValueTextSize(9f);

        LineDataSet precipDataSet = new LineDataSet(precipEntries, "Precipitation Prob (%)");
        precipDataSet.setColor(Color.parseColor("#3F51B5"));
        precipDataSet.setCircleColor(Color.parseColor("#3F51B5"));
        precipDataSet.setLineWidth(2.0f);
        precipDataSet.setCircleRadius(4.0f);
        precipDataSet.setValueTextSize(9f);

        LineData humPrecipData = new LineData(humidityDataSet, precipDataSet);
        configureChartStyle(humidityPrecipChart, humPrecipData, dayLabels);

        // Setup 7-Day Rain Bar Chart
        if (precipBarChart != null) {
            List<BarEntry> barEntries = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                barEntries.add(new BarEntry(i, precipEntries.get(i).getY()));
            }

            BarDataSet barDataSet = new BarDataSet(barEntries, "Rain Probability (%)");
            barDataSet.setColor(Color.parseColor("#00E676"));
            barDataSet.setValueTextColor(Color.parseColor("#1E1E2C"));
            barDataSet.setValueTextSize(10f);

            BarData barData = new BarData(barDataSet);
            barData.setBarWidth(0.5f);

            precipBarChart.setData(barData);
            precipBarChart.getDescription().setEnabled(false);
            precipBarChart.setDrawGridBackground(false);
            precipBarChart.setTouchEnabled(true);
            precipBarChart.setScaleEnabled(false);

            XAxis xAxis = precipBarChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(dayLabels));
            xAxis.setDrawGridLines(false);
            xAxis.setTextSize(11f);
            xAxis.setTextColor(Color.parseColor("#555555"));

            YAxis leftAxis = precipBarChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f);
            leftAxis.setAxisMaximum(100f);
            leftAxis.setDrawGridLines(true);
            leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
            leftAxis.setTextColor(Color.parseColor("#555555"));

            precipBarChart.getAxisRight().setEnabled(false);
            precipBarChart.getLegend().setEnabled(true);
            precipBarChart.getLegend().setTextColor(Color.parseColor("#333333"));
            precipBarChart.animateY(700);
            precipBarChart.invalidate();
        }
    }

    private void configureChartStyle(LineChart chart, LineData lineData, List<String> dayLabels) {
        chart.setData(lineData);
        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dayLabels));
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.parseColor("#555555"));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setTextSize(10f);
        leftAxis.setTextColor(Color.parseColor("#555555"));

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        chart.getLegend().setEnabled(true);
        chart.getLegend().setTextSize(11f);
        chart.getLegend().setTextColor(Color.parseColor("#333333"));

        chart.animateX(700);
        chart.invalidate();
    }

    private void setupListeners() {
        btnBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnShareCharts = findViewById(R.id.btn_share_charts);
        btnShareCharts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    View rootLayout = findViewById(R.id.charts_root_layout);
                    int totalHeight = rootLayout.getHeight();
                    int totalWidth = rootLayout.getWidth();

                    if (totalWidth <= 0 || totalHeight <= 0) {
                        rootLayout.measure(
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                        );
                        totalWidth = rootLayout.getMeasuredWidth();
                        totalHeight = rootLayout.getMeasuredHeight();
                        rootLayout.layout(0, 0, totalWidth, totalHeight);
                    }

                    android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(totalWidth, totalHeight, android.graphics.Bitmap.Config.ARGB_8888);
                    android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
                    rootLayout.draw(canvas);

                    java.io.File cachePath = new java.io.File(getCacheDir(), "images");
                    cachePath.mkdirs();
                    java.io.File file = new java.io.File(cachePath, "weather_charts_" + System.currentTimeMillis() + ".png");
                    java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
                    stream.close();

                    android.net.Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                            DetailsActivity.this,
                            getPackageName() + ".fileprovider",
                            file
                    );

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_SUBJECT, "SkyPredict AI Weather Trends & Charts - " + selectedCity);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "📊 Temperature, Humidity & Rain Probability Trends for " + selectedCity + "\nPowered by SkyPredict AI!");
                    sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    sendIntent.setType("image/png");
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    Intent shareIntent = Intent.createChooser(sendIntent, "Share Weather Charts via");
                    startActivity(shareIntent);
                } catch (Exception e) {
                    e.printStackTrace();
                    android.widget.Toast.makeText(DetailsActivity.this, "Failed to share charts image: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
