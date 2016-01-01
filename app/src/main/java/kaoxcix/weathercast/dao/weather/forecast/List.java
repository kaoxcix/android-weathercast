package kaoxcix.weathercast.dao.weather.forecast;

import java.util.ArrayList;

import kaoxcix.weathercast.dao.weather.current.Weather;

public class List {
    private ArrayList<Weather> weather;
    private int dt;
    private Temp temp;

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

    public int getDt() {
        return dt;
    }

    public Temp getTemp() {
        return temp;
    }
}

