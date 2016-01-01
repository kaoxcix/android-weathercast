package kaoxcix.weathercast.dao.locationList;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Prediction {
    private String description;

    @SerializedName("terms")
    private ArrayList<Term> terms;

    public String getDescription() {
        return description;
    }

    public ArrayList<Term> getTerms() {
        return terms;
    }

    public ArrayList<String> getStringListTerms() {
        ArrayList<String> terms = new ArrayList();
        for(Term term : this.terms) {
            terms.add(term.getValue());
        }
        return terms;
    }
}
