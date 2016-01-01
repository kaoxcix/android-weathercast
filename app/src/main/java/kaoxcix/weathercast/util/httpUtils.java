package kaoxcix.weathercast.util;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class httpUtils {
    private final String GOOGLE_PLACE_API_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
    private final String GOOGLE_PLACE_API_AUTH_KEY = "AIzaSyDSDO6K4wXMViN4wCzUJ40FDA4bFyJ22KA";
    private final String OPENWEATHER_API_URL = "http://api.openweathermap.org/data/2.5/";
    private final String OPENWEATHER_API_KEY = "c9807a3a0d005ca32f4223bb3939c59b";

    public httpUtils() {
    }

    public String getLocationList(String location) {
        final StringBuilder url = new StringBuilder(GOOGLE_PLACE_API_URL);
        try {
            url.append("?key=" + GOOGLE_PLACE_API_AUTH_KEY);
            url.append("&types=(cities)");
            url.append("&input=" + URLEncoder.encode(location.trim(), "utf8"));
        } catch (UnsupportedEncodingException e) {
            return "Error - " + e.getMessage();
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(url.toString())
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "Not Success : " + response.code();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error - " + e.getMessage();
        }
    }

    public String getCurrentWeatherData(String location) {
        final StringBuilder url = new StringBuilder(OPENWEATHER_API_URL);
        try {
            url.append("Weather?q=" + URLEncoder.encode(location.trim(), "utf8"));
            url.append("&units=metric");
            url.append("&APPID=" + OPENWEATHER_API_KEY);
        } catch (UnsupportedEncodingException e) {
            return "Error - " + e.getMessage();
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(url.toString())
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "Not Success : " + response.code();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error - " + e.getMessage();
        }
    }

    public String getForecastWeatherData(String location) {
        final StringBuilder url = new StringBuilder(OPENWEATHER_API_URL);
        try {
            url.append("forecast/daily?q=" + URLEncoder.encode(location.trim(), "utf8"));
            url.append("&units=metric");
            url.append("&cnt=8");
            url.append("&APPID=" + OPENWEATHER_API_KEY);
        } catch (UnsupportedEncodingException e) {
            return "Error - " + e.getMessage();
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        Request.Builder builder = new Request.Builder();
        Request request = builder
                .url(url.toString())
                .build();

        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                return "Not Success : " + response.code();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error - " + e.getMessage();
        }
    }
}
