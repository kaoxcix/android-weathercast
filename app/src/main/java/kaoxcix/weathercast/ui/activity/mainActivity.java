package kaoxcix.weathercast.ui.activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import kaoxcix.weathercast.R;
import kaoxcix.weathercast.dao.weather.current.CurrentWeather;
import kaoxcix.weathercast.dao.weather.forecast.ForecastWeather;
import kaoxcix.weathercast.dao.weather.forecast.List;
import kaoxcix.weathercast.util.assetUtils;
import kaoxcix.weathercast.util.checkUtils;
import kaoxcix.weathercast.util.httpUtils;

public class mainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;
    private final Uri uriLocation = Uri.parse("content://weatherCastV2DB/location");
    private final Uri uriWeather = Uri.parse("content://weatherCastV2DB/Weather");
    private Boolean imperial;
    private Boolean auto_refresh;
    private double multiply = 1;
    private double plus = 0;
    private ListView currentWeatherListView;
    private ListView forecastWeatherListView;
    private ArrayList<HashMap<String,String>> currentWeatherInfoList;
    private ArrayList<HashMap<String,String>> forecastWeatherInfoList;
    private SimpleAdapter currentWeatherAdapter;
    private SimpleAdapter forecastWeatherAdapter;
    private String selectedArea1;
    private String selectedArea2;
    private String selectedCountry;
    private String location;
    private String createDate;
    private checkUtils checkUtils;
    private View rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mainActivity.this, weatherListActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initSharedPreferences();
        initInstances();
        initWeatherInfoListView();
    }

    private void initSharedPreferences() {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sp.edit();
    }

    private void initInstances() {
        checkUtils = new checkUtils(this);
        currentWeatherListView = (ListView) findViewById(R.id.currentWeatherListView);
        forecastWeatherListView = (ListView) findViewById(R.id.forecastWeatherListView);
        rootView = findViewById(R.id.rootView);

    }

    private void getWeather(String date){
        imperial = sp.getBoolean("use_imperial", false);
        auto_refresh = sp.getBoolean("auto_refresh", true);
        if(imperial == true){
            multiply = 9.0/5.0;
            plus = 32.0;
        }
        else{
            multiply = 1;
            plus = 0;
        }

        setCurrentWeatherInfo(location);
        setForecastWeatherInfo(location);

        if(auto_refresh == true) {
            Date c_date = new Date(new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(new Date()));
            Date saved_date = new Date(date);
            long diffInMillisec = c_date.getTime() - saved_date.getTime();
            long diffInSec = TimeUnit.MILLISECONDS.toSeconds(diffInMillisec);
            long seconds = diffInSec % 60;
            diffInSec /= 60;
            long minutes = diffInSec % 60;
            diffInSec /= 60;
            long hours = diffInSec % 24;
            diffInSec /= 24;
            long days = diffInSec;
            Double diffHour = Double.parseDouble(hours + "." + minutes);
            if (diffHour >= 3.0) {
                if(checkUtils.isNetworkAvailable() == true) {
                    getLatestSelectedWeatherInfo(selectedArea1, selectedArea2, selectedCountry);
                } else {
                    Snackbar.make(rootView, getString(R.string.message_no_network_connection), Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void initWeatherInfoListView() {

        currentWeatherListView.setDivider(null);
        currentWeatherListView.setDividerHeight(0);
        forecastWeatherListView.setDivider(null);
        forecastWeatherListView.setDividerHeight(0);

        selectedArea1 = sp.getString("selectedArea1", "");
        selectedArea2 = sp.getString("selectedArea2", "");
        selectedCountry = sp.getString("selectedCountry", "");
        location = (selectedArea1+" "+selectedArea2).trim();
        createDate = null;

        Cursor checkLocationCursor = getApplicationContext().getContentResolver().query(uriLocation, null, "area1 = '" + selectedArea1.trim() + "' and area2 = '" + selectedArea2.trim() + "' and country = '" + selectedCountry.trim() + "'", null, null);
        if(checkLocationCursor.getCount() == 0){
            Cursor firstWeatherCursor = getApplicationContext().getContentResolver().query(uriLocation, null, null, null, null);
            if(firstWeatherCursor.getCount() == 0){
                Intent intent = new Intent(mainActivity.this, weatherAddActivity.class);
                startActivity(intent);
            } else {
                if (firstWeatherCursor.moveToFirst()) {
                    selectedArea1 = firstWeatherCursor.getString(firstWeatherCursor.getColumnIndex("area1"));
                    selectedArea2 = firstWeatherCursor.getString(firstWeatherCursor.getColumnIndex("area2"));
                    selectedCountry = firstWeatherCursor.getString(firstWeatherCursor.getColumnIndex("country"));
                    location = (selectedArea1+" "+selectedArea2).trim();
                    Cursor dateCursor = getApplicationContext().getContentResolver().query(uriWeather, null, "area = '"+location+"' and current = 'true'", null, null);
                    if(dateCursor.moveToFirst()) {
                        createDate = dateCursor.getString(dateCursor.getColumnIndex("created"));
                    }
                }
                getSupportActionBar().setTitle(location);
                getWeather(createDate);
                spEditor.putString("selectedArea1", selectedArea1);
                spEditor.putString("selectedArea2", selectedArea2);
                spEditor.putString("selectedCountry", selectedCountry);
                spEditor.commit();
            }
        } else {
            getSupportActionBar().setTitle(location);
            Cursor dateCursor = getApplicationContext().getContentResolver().query(uriWeather, null, "area = '"+location+"' and current = 'true'", null, null);
            if(dateCursor.moveToFirst()) {
                createDate = dateCursor.getString(dateCursor.getColumnIndex("created"));
            }
            getWeather(createDate);
        }
    }

    private void getLatestSelectedWeatherInfo(final String area1, final String area2, final String country) {
        final ProgressDialog progressBar = new ProgressDialog(mainActivity.this);
        new AsyncTask<Void, Void, String>() {
            private String area;
            @Override
            protected void onPreExecute() {
                progressBar.setMessage(getString(R.string.message_progress_loading));
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setCancelable(true);
                progressBar.show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                area = (area1+" "+area2).trim();

                getApplicationContext().getContentResolver().delete(uriWeather, "area = '"+area+"'", null);

                httpUtils httpUtil = new httpUtils();
                ArrayList<String> weatherData = new ArrayList<String>();
                String location = (area1.trim()+" "+area2.trim()).trim()+","+country.trim();
                weatherData.add(httpUtil.getCurrentWeatherData(location));
                weatherData.add(httpUtil.getForecastWeatherData(location));

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
                } else {
                    getApplicationContext().getContentResolver().delete(uriLocation, "area1 = '"+area1+"' and area2 = '"+area2+"'", null);
                    getApplicationContext().getContentResolver().delete(uriWeather, "area = '"+area+"'", null);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String string) {
                progressBar.dismiss();
                setCurrentWeatherInfo(location);
                setForecastWeatherInfo(location);
                Snackbar.make(rootView, getString(R.string.message_update_current_weather), Snackbar.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void updateAllWeatherInfo() {
        final View rootView = findViewById(R.id.rootView);
        final ProgressDialog progressBar = new ProgressDialog(mainActivity.this);
        new AsyncTask<Void, Void, String>() {
            private String area, area1, area2, country;
            @Override
            protected void onPreExecute() {
                progressBar.setMessage(getString(R.string.message_progress_loading));
                progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressBar.setCancelable(true);
                progressBar.show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                Cursor locationCursor = getApplicationContext().getContentResolver().query(uriLocation, null, null, null, null);
                while (locationCursor.moveToNext()) {
                    area1 = locationCursor.getString(locationCursor.getColumnIndex("area1"));
                    area2 = locationCursor.getString(locationCursor.getColumnIndex("area2"));
                    country = locationCursor.getString(locationCursor.getColumnIndex("country"));
                    area = (area1+" "+area2).trim();

                    getApplicationContext().getContentResolver().delete(uriWeather, "area = '"+area+"'", null);

                    httpUtils httpUtil = new httpUtils();
                    ArrayList<String> weatherData = new ArrayList<String>();
                    String location = (area1.trim()+" "+area2.trim()).trim()+","+country.trim();
                    weatherData.add(httpUtil.getCurrentWeatherData(location));
                    weatherData.add(httpUtil.getForecastWeatherData(location));

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
                    } else {
                        getApplicationContext().getContentResolver().delete(uriLocation, "area1 = '"+area1+"' and area2 = '"+area2+"'", null);
                        getApplicationContext().getContentResolver().delete(uriWeather, "area = '"+area+"'", null);
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String string) {
                initWeatherInfoListView();
                progressBar.dismiss();
                Snackbar.make(rootView, getString(R.string.message_update_all_weather), Snackbar.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private void setCurrentWeatherInfo(String location){
        final assetUtils assetUtils = new assetUtils(this);
        Cursor currentCursor = getApplicationContext().getContentResolver().query(uriWeather, null, "area = '"+location+"' and current = 'true'", null, null);
        String[] from = new String[]{"temp", "sunrise", "sunset", "description", "updated"};
        int[] to = new int[]{R.id.txtCurTemp, R.id.txtCurSunrise, R.id.txtCurSunset, R.id.txtCurDescription, R.id.txtCurUpdated};
        currentWeatherInfoList= new ArrayList<HashMap<String,String>>();
        while(currentCursor.moveToNext()) {
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("weather", currentCursor.getString(currentCursor.getColumnIndex("weather")));
            map.put("temp", new DecimalFormat("####0.0").format((multiply*currentCursor.getDouble(currentCursor.getColumnIndex("temp")))+plus)+"°");
            map.put("sunrise", currentCursor.getString(currentCursor.getColumnIndex("sunrise")));
            map.put("sunset", currentCursor.getString(currentCursor.getColumnIndex("sunset")));
            map.put("description", currentCursor.getString(currentCursor.getColumnIndex("description")).substring(0,1).toUpperCase() +
                    currentCursor.getString(currentCursor.getColumnIndex("description")).toLowerCase().substring(1));
            map.put("updated", "Updated : "+currentCursor.getString(currentCursor.getColumnIndex("created")));
            currentWeatherInfoList.add(map);
        }

        int layout = R.layout.list_weather_info_current;
        SimpleAdapter mAdapter = new SimpleAdapter(mainActivity.this, currentWeatherInfoList, layout, from, to){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater Inflater = (LayoutInflater) mainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(convertView == null){
                    convertView = Inflater.inflate(R.layout.list_weather_info_current, null);
                }
                HashMap<String,String> map = new HashMap<String,String>(currentWeatherInfoList.get(position));
                ImageView imgvCurIcon = (ImageView) convertView.findViewById(R.id.imgvCurIcon);
                ImageView imgvSunrise = (ImageView) convertView.findViewById(R.id.imgvSunrise);
                ImageView imgvSunset = (ImageView) convertView.findViewById(R.id.imgvSunset);

                TextView txtCurTemp = (TextView ) convertView.findViewById(R.id.txtCurTemp);
                TextView txtCurSunrise = (TextView ) convertView.findViewById(R.id.txtCurSunrise);
                TextView txtCurSunset = (TextView ) convertView.findViewById(R.id.txtCurSunset);

                //set Sunrise Sunset image black/white
                imgvSunrise.setImageResource(assetUtils.getSunImageId(Integer.parseInt(map.get("weather")), "simple_weather_icon_57"));
                imgvSunset.setImageResource(assetUtils.getSunImageId(Integer.parseInt(map.get("weather")), "simple_weather_icon_56"));

                //set text color acconding to bg color
                txtCurTemp.setTextColor(getResources().getColor(assetUtils.getFontColorId(Integer.parseInt(map.get("weather")))));
                txtCurSunrise.setTextColor(getResources().getColor(assetUtils.getFontColorId(Integer.parseInt(map.get("weather")))));
                txtCurSunset.setTextColor(getResources().getColor(assetUtils.getFontColorId(Integer.parseInt(map.get("weather")))));

                //set bg color
                int statusBarColor = assetUtils.getBackgroundDarkColorId(Integer.parseInt(map.get("weather")));
                int actionBarColor = assetUtils.getBackgroundColorId(Integer.parseInt(map.get("weather")));
                spEditor.putInt("statusBarColor", statusBarColor);
                spEditor.putInt("actionBarColor", actionBarColor);
                spEditor.commit();
                getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(actionBarColor));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(getResources().getColor(statusBarColor));
                }

                convertView.setBackgroundResource(assetUtils.getBackgroundColorId(Integer.parseInt(map.get("weather"))));
                imgvCurIcon.setImageResource(assetUtils.getWeatherImageId(Integer.parseInt(map.get("weather"))));
                return super.getView(position, convertView, parent);
            }};
        currentWeatherListView.setAdapter(mAdapter);
    }

    private void setForecastWeatherInfo(String location){
        final assetUtils assetUtils = new assetUtils(this);
        Cursor forecastCursor = getApplicationContext().getContentResolver().query(uriWeather, null, "area = '"+location+"' and current = 'false'", null, null);
        String[] from = new String[]{"day", "temp", "description"};
        int[] to = new int[]{R.id.txtForeDay, R.id.txtForeTemp, R.id.txtForeDescription};
        forecastWeatherInfoList = new ArrayList<HashMap<String,String>>();
        while(forecastCursor.moveToNext())
        {
            String temp_min = new DecimalFormat("####0.0").format((multiply*forecastCursor.getDouble(forecastCursor.getColumnIndex("temp_min")))+plus),
                    temp_max = new DecimalFormat("####0.0").format((multiply*forecastCursor.getDouble(forecastCursor.getColumnIndex("temp_max")))+plus);
            HashMap<String,String> map = new HashMap<String,String>();
            map.put("weather", forecastCursor.getString(forecastCursor.getColumnIndex("weather")));
            if(forecastCursor.isFirst() == true){
                map.put("day", "Tomorrow");
            }
            else{
                Date input = null;
                try {
                    input = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(forecastCursor.getString(forecastCursor.getColumnIndex("date")));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                map.put("day", new SimpleDateFormat("EEEE").format(input));
            }
            map.put("temp", temp_min+"°"+" — "+temp_max+"°");
            map.put("description", forecastCursor.getString(forecastCursor.getColumnIndex("description")).substring(0,1).toUpperCase() +
                    forecastCursor.getString(forecastCursor.getColumnIndex("description")).toLowerCase().substring(1));
            forecastWeatherInfoList.add(map);
        }

        int layout = R.layout.list_weather_info_forecast;
        SimpleAdapter mAdapter = new SimpleAdapter(mainActivity.this, forecastWeatherInfoList, layout, from, to){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater Inflater = (LayoutInflater) mainActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if(convertView == null){
                    convertView = Inflater.inflate(R.layout.list_weather_info_forecast, null);
                }
                HashMap<String,String> map = new HashMap<String,String>(forecastWeatherInfoList.get(position));
                ImageView imgvForeIcon = (ImageView) convertView.findViewById(R.id.imgvForeIcon);
                TextView txtForeDay = (TextView ) convertView.findViewById(R.id.txtForeDay);
                TextView txtForeTemp = (TextView ) convertView.findViewById(R.id.txtForeTemp);

                convertView.setBackgroundResource(assetUtils.getBackgroundColorId(Integer.parseInt(map.get("weather"))));
                if(position == getCount()-1){
                    forecastWeatherListView.setBackgroundResource(assetUtils.getBackgroundColorId(Integer.parseInt(map.get("weather"))));
                }
                imgvForeIcon.setImageResource(assetUtils.getWeatherImageId(Integer.parseInt(map.get("weather"))));
                txtForeDay.setTextColor(getResources().getColor(assetUtils.getFontColorId(Integer.parseInt(map.get("weather")))));
                txtForeTemp.setTextColor(getResources().getColor(assetUtils.getFontColorId(Integer.parseInt(map.get("weather")))));
                return super.getView(position, convertView, parent);
            }};
        forecastWeatherListView.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_weather_add) {
            Intent intent = new Intent(this, weatherAddActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(this, settingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_weather_update) {
            if(checkUtils.isNetworkAvailable() == true) {
                updateAllWeatherInfo();
            } else {
                Snackbar.make(rootView, getString(R.string.message_no_network_connection), Snackbar.LENGTH_SHORT).show();
            }
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(this, aboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
}
