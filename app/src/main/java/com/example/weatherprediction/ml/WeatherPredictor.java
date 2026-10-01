package com.example.weatherprediction.ml;

import android.content.Context;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A data-driven Machine Learning Weather Predictor.
 * It uses a k-Nearest Neighbors (k-NN) Regression model trained on historical weather
 * observation records to compute predicted temperature, humidity, and precipitation.
 */
public class WeatherPredictor {

    public static class PredictionResult {
        public final double temperature;
        public final double humidity;
        public final double precipitationProbability;

        public PredictionResult(double temperature, double humidity, double precipitationProbability) {
            this.temperature = temperature;
            this.humidity = humidity;
            this.precipitationProbability = precipitationProbability;
        }
    }

    private static class NeighborDistance {
        final HistoricalDatasetLoader.WeatherObservation observation;
        final double distance;

        NeighborDistance(HistoricalDatasetLoader.WeatherObservation observation, double distance) {
            this.observation = observation;
            this.distance = distance;
        }
    }

    private final List<HistoricalDatasetLoader.WeatherObservation> dataset;

    public WeatherPredictor() {
        this.dataset = HistoricalDatasetLoader.getDefaultDataset();
    }

    public WeatherPredictor(Context context) {
        this.dataset = HistoricalDatasetLoader.loadDataset(context);
    }

    /**
     * Predicts weather parameters using k-NN Regression on the historical dataset.
     */
    public PredictionResult predict(String city, long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);

        int targetDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int cityHash = Math.abs(city.hashCode());
        double targetCityModifier = (cityHash % 100) / 10.0;

        List<NeighborDistance> neighborDistances = new ArrayList<>();

        for (HistoricalDatasetLoader.WeatherObservation obs : dataset) {
            // Circular day difference (since day 365 is adjacent to day 1)
            int dayDiff = Math.abs(obs.dayOfYear - targetDayOfYear);
            if (dayDiff > 182) {
                dayDiff = 365 - dayDiff;
            }

            double normDayDiff = dayDiff / 182.5;
            double normCityDiff = Math.abs(obs.cityModifier - targetCityModifier) / 10.0;

            // Euclidean distance in normalized feature space
            double distance = Math.sqrt((normDayDiff * normDayDiff) + (normCityDiff * normCityDiff));
            neighborDistances.add(new NeighborDistance(obs, distance));
        }

        // Sort by feature distance ascending
        Collections.sort(neighborDistances, new Comparator<NeighborDistance>() {
            @Override
            public int compare(NeighborDistance o1, NeighborDistance o2) {
                return Double.compare(o1.distance, o2.distance);
            }
        });

        // Use top k=4 nearest historical neighbors
        int k = Math.min(4, neighborDistances.size());
        double weightedTempSum = 0.0;
        double weightedHumSum = 0.0;
        double weightedPrecipSum = 0.0;
        double totalWeight = 0.0;

        for (int i = 0; i < k; i++) {
            NeighborDistance nd = neighborDistances.get(i);
            // Distance inverse weighting (add epsilon 0.01 to prevent div by zero)
            double weight = 1.0 / (nd.distance + 0.01);

            weightedTempSum += nd.observation.temperature * weight;
            weightedHumSum += nd.observation.humidity * weight;
            weightedPrecipSum += nd.observation.precipitation * weight;
            totalWeight += weight;
        }

        double predTemp = weightedTempSum / totalWeight;
        double predHum = weightedHumSum / totalWeight;
        double predPrecip = weightedPrecipSum / totalWeight;

        // Clamp predicted values to physical limits
        predTemp = Math.max(-10.0, Math.min(50.0, predTemp));
        predHum = Math.max(0.0, Math.min(100.0, predHum));
        predPrecip = Math.max(0.0, Math.min(1.0, predPrecip));

        return new PredictionResult(
            Math.round(predTemp * 10.0) / 10.0,
            Math.round(predHum * 10.0) / 10.0,
            Math.round(predPrecip * 100.0) / 100.0
        );
    }

    /**
     * List of supported cities across India and top global countries for local prediction.
     */
    public static final String[] SUPPORTED_CITIES = {
        // India (Top 100+ Famous Cities)
        "Mumbai", "Delhi", "New Delhi", "Bangalore", "Bengaluru", "Hyderabad", "Ahmedabad",
        "Chennai", "Kolkata", "Surat", "Pune", "Jaipur", "Lucknow", "Kanpur", "Nagpur",
        "Indore", "Thane", "Bhopal", "Visakhapatnam", "Pimpri-Chinchwad", "Patna", "Vadodara",
        "Ghaziabad", "Ludhiana", "Agra", "Nashik", "Faridabad", "Meerut", "Rajkot", "Kalyan",
        "Varanasi", "Srinagar", "Aurangabad", "Chhatrapati Sambhajinagar", "Dhanbad", "Amritsar",
        "Navi Mumbai", "Allahabad", "Prayagraj", "Ranchi", "Howrah", "Coimbatore", "Jabalpur",
        "Gwalior", "Vijayawada", "Jodhpur", "Madurai", "Raipur", "Kota", "Guwahati", "Chandigarh",
        "Solapur", "Hubli", "Dharwad", "Bareilly", "Moradabad", "Mysore", "Mysuru", "Gurgaon",
        "Gurugram", "Aligarh", "Jalandhar", "Tiruchirappalli", "Trichy", "Bhubaneswar", "Salem",
        "Mira-Bhayandar", "Warangal", "Thiruvananthapuram", "Bhiwandi", "Saharanpur", "Guntur",
        "Amravati", "Bikaner", "Noida", "Jamshedpur", "Bhilai", "Cuttack", "Firozabad", "Kochi",
        "Cochin", "Bhavnagar", "Dehradun", "Durgapur", "Asansol", "Nanded", "Kolhapur", "Ajmer",
        "Gulbarga", "Kalaburagi", "Jamnagar", "Ujjain", "Loni", "Siliguri", "Jhansi", "Ulhasnagar",
        "Jammu", "Sangli", "Mangalore", "Mangaluru", "Erode", "Belgaum", "Belagavi", "Kurnool",
        "Ambattur", "Rajahmundry", "Tirunelveli", "Malegaon", "Gaya", "Udaipur", "Kakinada",
        "Davanagere", "Kozhikode", "Calicut", "Maheshtala", "Rajpur Sonarpur", "Rourkela",
        "Thothukudi", "Dhamtari", "Muzaffarnagar", "Bhagalpur", "Bhilwara", "Mathura", "Shivamogga",
        "Shimoga", "Muzaffarpur", "Patiala", "Rohtak", "Kamarhati", "Tiruppur", "Roorkee",
        "Gandhinagar", "Hospet", "Bhangar", "Panipat", "Darbhanga", "Bally", "Puri", "Shimla",
        "Dharamshala", "Gangtok", "Shillong", "Imphal", "Aizawl", "Agartala", "Kohima", "Itanagar",
        "Panaji", "Margao", "Puducherry", "Pondicherry", "Port Blair", "Leh", "Kargil", "Ayodhya",
        "Manali", "Kullu", "Rishikesh", "Haridwar", "Nainital", "Latur", "Akola", "Dhule",
        "Ahmednagar", "Chandrapur", "Satna", "Ratlam", "Rewa", "Singrauli", "Bathinda", "Mohali",

        // United States (USA)
        "New York", "Los Angeles", "Chicago", "Houston", "Phoenix", "Philadelphia", "San Antonio",
        "San Diego", "Dallas", "San Jose", "Austin", "Jacksonville", "Fort Worth", "Columbus",
        "Charlotte", "Indianapolis", "San Francisco", "Seattle", "Denver", "Washington DC",
        "Boston", "El Paso", "Nashville", "Las Vegas", "Detroit", "Portland", "Memphis",
        "Louisville", "Baltimore", "Milwaukee", "Albuquerque", "Tucson", "Fresno", "Sacramento",
        "Atlanta", "Kansas City", "Miami", "Raleigh", "Omaha", "Oakland", "Minneapolis", "Tampa", "Orlando",

        // United Kingdom (UK)
        "London", "Birmingham", "Manchester", "Glasgow", "Liverpool", "Edinburgh", "Bristol",
        "Leeds", "Sheffield", "Newcastle", "Belfast", "Leicester", "Nottingham", "Southampton",
        "Portsmouth", "Cardiff", "Aberdeen", "Cambridge", "Oxford",

        // Canada
        "Toronto", "Montreal", "Vancouver", "Calgary", "Edmonton", "Ottawa", "Winnipeg",
        "Quebec City", "Hamilton", "Kitchener", "Victoria", "Halifax", "Windsor",

        // Australia
        "Sydney", "Melbourne", "Brisbane", "Perth", "Adelaide", "Gold Coast", "Canberra",
        "Newcastle", "Sunshine Coast", "Wollongong", "Hobart", "Geelong", "Townsville", "Cairns", "Darwin",

        // Germany
        "Berlin", "Hamburg", "Munich", "Cologne", "Frankfurt", "Stuttgart", "Düsseldorf",
        "Dortmund", "Essen", "Leipzig", "Bremen", "Dresden", "Hannover", "Nuremberg", "Duisburg",

        // France
        "Paris", "Marseille", "Lyon", "Toulouse", "Nice", "Nantes", "Montpellier",
        "Strasbourg", "Bordeaux", "Lille", "Rennes", "Reims", "Le Havre", "Toulon", "Grenoble",

        // Japan
        "Tokyo", "Yokohama", "Osaka", "Nagoya", "Sapporo", "Kobe", "Kyoto", "Fukuoka",
        "Kawasaki", "Saitama", "Hiroshima", "Sendai", "Chiba", "Kitakyushu",

        // China
        "Beijing", "Shanghai", "Guangzhou", "Shenzhen", "Chengdu", "Chongqing", "Tianjin",
        "Wuhan", "Xi'an", "Hangzhou", "Nanjing", "Shenyang", "Harbin", "Qingdao", "Dalian",

        // Brazil
        "São Paulo", "Rio de Janeiro", "Brasília", "Salvador", "Fortaleza", "Belo Horizonte",
        "Manaus", "Curitiba", "Recife", "Porto Alegre", "Belém", "Goiânia",

        // Italy
        "Rome", "Milan", "Naples", "Turin", "Palermo", "Genoa", "Bologna", "Florence", "Bari", "Venice", "Verona", "Catania",

        // Russia
        "Moscow", "Saint Petersburg", "Novosibirsk", "Yekaterinburg", "Kazan", "Nizhny Novgorod", "Chelyabinsk", "Samara", "Omsk", "Rostov-on-Don",

        // South Korea
        "Seoul", "Busan", "Incheon", "Daegu", "Daejeon", "Gwangju", "Suwon", "Ulsan", "Changwon", "Goyang",

        // UAE
        "Dubai", "Abu Dhabi", "Sharjah", "Al Ain", "Ajman", "Ras Al Khaimah", "Fujairah",

        // South Africa
        "Johannesburg", "Cape Town", "Durban", "Pretoria", "Port Elizabeth", "Bloemfontein",

        // Spain
        "Madrid", "Barcelona", "Valencia", "Seville", "Zaragoza", "Malaga", "Murcia", "Palma", "Las Palmas", "Bilbao"
    };

    /**
     * Validates whether the given city name is in the list of available cities.
     */
    public static boolean isValidCity(String city) {
        if (city == null) return false;
        String trimmed = city.trim();
        if (trimmed.isEmpty() || trimmed.length() < 2 || trimmed.length() > 50) return false;

        String lower = trimmed.toLowerCase(java.util.Locale.ROOT);
        for (String validCity : SUPPORTED_CITIES) {
            if (validCity.toLowerCase(java.util.Locale.ROOT).equals(lower)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines weather condition string based on temperature and precipitation.
     */
    public static String getWeatherCondition(double temp, double precipProb) {
        if (precipProb > 0.55) {
            return "Rainy 🌧️";
        } else if (precipProb > 0.30) {
            return "Cloudy ⛅";
        } else if (temp < 5.0) {
            return "Cold / Snowy ❄️";
        } else {
            return "Sunny / Clear ☀️";
        }
    }
}
