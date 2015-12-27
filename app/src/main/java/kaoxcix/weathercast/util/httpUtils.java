package kaoxcix.weathercast.util;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class httpUtils {
    private String googlePlaceApiUrl;
    private String googlePlaceApiKey;

    public httpUtils() {
        this.googlePlaceApiUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json";
        this.googlePlaceApiKey = "AIzaSyDSDO6K4wXMViN4wCzUJ40FDA4bFyJ22KA";
    }

    public String getLocationListByCity(String city) {
        final StringBuilder url = new StringBuilder(googlePlaceApiUrl);
        try {
            url.append("?key=" + googlePlaceApiKey);
            url.append("&types=(cities)");
            url.append("&input=" + URLEncoder.encode(city, "utf8"));
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
