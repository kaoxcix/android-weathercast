package kaoxcix.weathercast.dao.locationList;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class LocationList {
    private String status;

    @SerializedName("predictions")
    private ArrayList<Prediction> predictions;

    public String getStatus() {
        return status;
    }

    public ArrayList<Prediction> getPredictions() {
        return predictions;
    }
}
