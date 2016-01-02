package kaoxcix.weathercast.ui.activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import kaoxcix.weathercast.R;
import kaoxcix.weathercast.dao.locationList.LocationList;
import kaoxcix.weathercast.dao.locationList.Prediction;
import kaoxcix.weathercast.dao.weather.current.CurrentWeather;
import kaoxcix.weathercast.dao.weather.forecast.ForecastWeather;
import kaoxcix.weathercast.dao.weather.forecast.List;
import kaoxcix.weathercast.util.httpUtils;
import kaoxcix.weathercast.util.checkUtils;

public class weatherAddActivity extends AppCompatActivity {
    private MaterialSearchView searchView;
    private ListView locationListView;
    private SimpleAdapter locationAdapter;
    private ArrayList<HashMap<String,String>> locationArrayList;
    private final Uri uriLocation = Uri.parse("content://weatherCastDB/location");
    private final Uri uriWeather = Uri.parse("content://weatherCastDB/Weather");
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_add);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initSharedPreferences();
        initInstances();
        initSearchView();

    }

    private void initSharedPreferences() {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sp.edit();
    }

    private void initInstances(){
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        locationListView = (ListView) findViewById(R.id.locationListView);
    }

    private void initSearchView() {
        searchView.setVoiceSearch(false);
//        searchView.setCursorDrawable(R.drawable.ic_menu_camera);
//        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                checkUtils check = new checkUtils();
                if(check.isEnglishAlphabetString(query) == false) {
                    Snackbar.make(searchView, getString(R.string.message_english_only), Snackbar.LENGTH_SHORT).show();
                } else {
                    queryLocation(query.trim());
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });

        locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> getMap = new HashMap<String, String>(locationArrayList.get(position));
                String area1 = getMap.get("area1");
                String area2 = getMap.get("area2");
                String country = getMap.get("country").replace(", ", "");
                Cursor addedAreaCursor = getApplicationContext().getContentResolver().query(uriLocation, null, "area1 = '" + area1 + "'", null, null);
                if (addedAreaCursor.getCount() == 1) {
                    Snackbar.make(view, getString(R.string.message_location_added), Snackbar.LENGTH_SHORT).show();
                } else {
                    queryWeatherData(area1, area2, country);
                }
            }
        });
    }

    private void queryLocation(final String query) {
        final ProgressDialog progressBar = new ProgressDialog(weatherAddActivity.this);
        final View rootView = findViewById(R.id.rootView);
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                progressBar.setMessage(getString(R.string.message_progress_loading));
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setCancelable(true);
                progressBar.show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                httpUtils httpUtil = new httpUtils();
                return httpUtil.getLocationList(query);
            }

            @Override
            protected void onPostExecute(String locationData) {
                Gson gson = new Gson();
                LocationList locationList = gson.fromJson(locationData, LocationList.class);
                String status = locationList.getStatus();
                if(status.equals("OK")) {
                    locationArrayList = new ArrayList<HashMap<String,String>>();
                    ArrayList<Prediction> predictions = locationList.getPredictions();
                    for (Prediction prediction : predictions) {
                        ArrayList<String> terms = prediction.getStringListTerms();
                        if (terms.size() == 2) {
                            HashMap<String,String> map = new HashMap<String,String>();
                            map.put("area1", terms.get(0).toString());
                            map.put("area2", "");
                            map.put("country", terms.get(1).toString());
                            map.put("other", terms.get(1).toString());
                            locationArrayList.add(map);
                        }
                        if (terms.size() == 3) {
                            HashMap<String,String> map = new HashMap<String,String>();
                            map.put("area1", terms.get(0).toString());
                            map.put("area2", terms.get(1).toString());
                            map.put("country", ", "+terms.get(2).toString());
                            map.put("other", terms.get(1)+", "+terms.get(2).toString());
                            locationArrayList.add(map);
                        }
                    }
                    String[] from = new String[]{"area1", "other"};
                    int[] to = new int[]{R.id.txtFoundArea1, R.id.txtFoundOther};
                    int layout = R.layout.list_weather_add_location_list;
                    locationAdapter = new SimpleAdapter(weatherAddActivity.this, locationArrayList, layout, from, to);
                    locationListView.setAdapter(locationAdapter);
                    progressBar.dismiss();
                } else {
                    locationListView.setAdapter(null);
                    progressBar.dismiss();
                    Snackbar.make(rootView, getString(R.string.message_location_notfound), Snackbar.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void queryWeatherData(final String area1, final String area2, final String country) {
        final ProgressDialog progressBar = new ProgressDialog(weatherAddActivity.this);
        final View rootView = findViewById(R.id.rootView);
        new AsyncTask<Void, Void, ArrayList<String>>() {
            @Override
            protected void onPreExecute() {
                progressBar.setMessage(getString(R.string.message_progress_loading));
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setCancelable(true);
                progressBar.show();
            }

            @Override
            protected ArrayList<String> doInBackground(Void... voids) {
                httpUtils httpUtil = new httpUtils();
                ArrayList<String> weatherData = new ArrayList<String>();
                String location = (area1.trim()+" "+area2.trim()).trim()+","+country.trim();
                weatherData.add(httpUtil.getCurrentWeatherData(location));
                weatherData.add(httpUtil.getForecastWeatherData(location));
                return weatherData;
            }

            @Override
            protected void onPostExecute(ArrayList<String> weatherData) {
                Gson gson = new Gson();

                final CurrentWeather currentWeather = gson.fromJson(weatherData.get(0).toString(), CurrentWeather.class);
                final ForecastWeather forecastWeather = gson.fromJson(weatherData.get(1).toString(), ForecastWeather.class);
                String currentCod = currentWeather.getCod();
                String forecastCod = forecastWeather.getCod();
                if(currentCod.equals("200") && forecastCod.equals("200")) {
                    // put selected area to app database "location"
                    ContentValues locationValues = new ContentValues();
                    locationValues.put("area1", area1);
                    locationValues.put("area2", area2);
                    locationValues.put("country", country);
                    getApplicationContext().getContentResolver().insert(uriLocation, locationValues);

                    //put current Weather data to app database "weather"
                    ContentValues currentValues = new ContentValues();
                    currentValues.put("area", (area1+" "+area2).trim());
                    currentValues.put("date", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(currentWeather.getDt() * 1000L)));
                    currentValues.put("temp", currentWeather.getMain().getTemp());
                    currentValues.put("sunrise", new SimpleDateFormat("HH:mm").format(new Date(currentWeather.getSys().getSunrise() * 1000L)));
                    currentValues.put("sunset", new SimpleDateFormat("HH:mm").format(new Date(currentWeather.getSys().getSunset() * 1000L)));
                    currentValues.put("weather", currentWeather.getLatestWeatherId());
                    currentValues.put("description", currentWeather.getLatestWeatherDescription());
                    currentValues.put("created", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
                    currentValues.put("current", "true");
                    getApplicationContext().getContentResolver().insert(uriWeather, currentValues);

                    //check to get only forecast data (json data have current data also)
                    int check = 0;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
                    Date getDate = null;
                    for(List list : forecastWeather.getList()) {
                        if(check == 6){
                            break;
                        }
                        try {
                            getDate = dateFormat.parse(dateFormat.format(new Date(list.getDt() * 1000L)).toString());
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        if (getDate.after(new Date())) {
                            ContentValues forecastValues = new ContentValues();
                            forecastValues.put("area", (area1 + " " + area2).trim());
                            forecastValues.put("date", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date(list.getDt() * 1000L)));
                            forecastValues.put("temp", list.getTemp().getDay());
                            forecastValues.put("temp_min", list.getTemp().getMin());
                            forecastValues.put("temp_max", list.getTemp().getMax());
                            forecastValues.put("weather", list.getLatestWeatherId());
                            forecastValues.put("description", list.getLatestWeatherDescription());
                            forecastValues.put("created", new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
                            getApplicationContext().getContentResolver().insert(uriWeather, forecastValues);
                            check++;
                        }
                    }
                    spEditor.putString("selectedArea1", area1);
                    spEditor.putString("selectedArea2", area2);
                    spEditor.putString("selectedCountry", country);
                    spEditor.commit();
                    Intent intent = new Intent(weatherAddActivity.this, mainActivity.class);
                    startActivity(intent);
                } else {
                    progressBar.dismiss();
                    Snackbar.make(rootView, getString(R.string.message_location_notfound), Snackbar.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_weather_add, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(weatherAddActivity.this, mainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    searchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
