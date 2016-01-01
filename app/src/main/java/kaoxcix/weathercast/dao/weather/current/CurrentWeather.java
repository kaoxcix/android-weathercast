package kaoxcix.weathercast.dao.weather.current;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;


public class CurrentWeather {
    private ArrayList<Weather> weather;
    private Main main;
    private Sys sys;
    private int dt;
    private String cod;

    public String getLatestWeatherId() {
        String id = null;
        for(Weather weather : this.weather) {
            id = weather.getId();
        }
        return id;
    }

    public String getLatestWeatherDescription() {
        String description = null;
        for(Weather weather : this.weather) {
            description = weather.getDescription();
        }
        return description;
    }

    public ArrayList<Weather> getWeather() {
        return weather;
    }

    public Main getMain() {
        return main;
    }

    public Sys getSys() {
        return sys;
    }

    public int getDt() {
        return dt;
    }

    public String getCod() {
        return cod;
    }
}
