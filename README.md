# ☀️ Weather Prediction App

An intelligent Android application that provides current weather updates and AI-driven weather forecasts using Machine Learning (k-NN Regression) trained on historical weather datasets.

---

## 🚀 Features

- **🤖 AI/ML Weather Prediction**: Uses a data-driven **k-Nearest Neighbors (k-NN)** regression algorithm trained on historical observations (`historical_weather.csv`) to predict future temperature, humidity, and precipitation probability.
- **📍 Current Weather & Multi-Day Forecast**: View current weather metrics and predicted multi-day weather forecasts for various cities.
- **📊 Interactive Charts & Analytics**: Beautiful chart visualizations powered by **MPAndroidChart** to analyze temperature trends and precipitation levels.
- **💾 Local History & Persistence**: Built with **Room Database** to store search queries and historical weather records locally for offline access.
- **🌡️ Unit Customization**: Seamlessly switch between **Celsius (°C)** and **Fahrenheit (°F)**.
- **📱 Modern UI & UX**: Designed with **Material Design 3**, responsive card layouts, weather watermarks, and **SwipeRefreshLayout** for pull-to-refresh updates.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Java & Kotlin
- **Min SDK**: 24 (Android 7.0) | **Target SDK**: 35 (Android 15) | **Compile SDK**: 37
- **UI Framework**: Android XML Views & Jetpack Compose (Material 3)
- **Database / Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) & DataStore Preferences
- **Machine Learning**: Custom k-NN Regression Engine & Historical Dataset Processor
- **Charts**: [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
- **Networking**: Retrofit, Moshi, OkHttp
- **Asynchronous Processing**: Kotlin Coroutines & Background Threads

---

## 📁 Project Structure

```
WeatherPredictionApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/weatherprediction/
│   │   │   │   ├── SplashActivity.java        # Splash screen launcher
│   │   │   │   ├── HomeActivity.java          # Home dashboard with current weather & AI forecast
│   │   │   │   ├── PredictionActivity.java    # Detailed AI multi-day forecast & charts
│   │   │   │   ├── DetailsActivity.java       # Comprehensive weather metrics breakdown
│   │   │   │   ├── HistoryActivity.java       # Saved weather records & search history
│   │   │   │   ├── SettingsActivity.java      # App preferences & unit selection
│   │   │   │   ├── data/                      # Room database entities, DAOs, & AppDatabase
│   │   │   │   └── ml/                        # k-NN WeatherPredictor & HistoricalDatasetLoader
│   │   │   ├── assets/
│   │   │   │   └── historical_weather.csv     # Training dataset for ML prediction
│   │   │   ├── res/                           # Layouts, drawables, values, & themes
│   │   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

---

## 💻 Getting Started

### Prerequisites
- [Android Studio Ladybug | 2024.2.1](https://developer.android.com/studio) or newer
- JDK 11 or higher
- Android SDK 35/37

### Installation & Build

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/mahant3595/Weather-Prediction-app.git
   cd Weather-Prediction-app
   ```

2. **Open in Android Studio**:
   - Open Android Studio and choose **Open an existing project**.
   - Select the `WeatherPredictionApp` project folder.

3. **Build & Run**:
   - Wait for Gradle sync to complete.
   - Select your Android device or emulator and click **Run (Shift + F10)**.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
