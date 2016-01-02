package kaoxcix.weathercast.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
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

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import kaoxcix.weathercast.R;

public class mainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;
    private final Uri uriLocation = Uri.parse("content://weatherCastDB/location");
    private final Uri uriWeather = Uri.parse("content://weatherCastDB/Weather");
    private Boolean imperial;
    private Boolean auto_refresh;
    private double multiply = 1;
    private double plus = 0;
    private ListView currentWeatherListView;
    private ListView forecastWeatherListView;
    private ArrayList<HashMap<String,String>> currentWeatherInfoList;
    private ArrayList<HashMap<String,String>> forecastWeatherInfoList;
    private String selectedArea1;
    private String selectedArea2;
    private String selectedCountry;
    private String location;

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
        currentWeatherListView = (ListView) findViewById(R.id.currentWeatherListView);
        forecastWeatherListView = (ListView) findViewById(R.id.forecastWeatherListView);

    }

    private void initWeatherInfoListView() {
        imperial = sp.getBoolean("use_imperial", false);
        auto_refresh = sp.getBoolean("auto_refresh", true);
        //calculate Fahrenheit
        if(imperial == true){
            multiply = 9.0/5.0;
            plus = 32.0;
        }
        else{
            multiply = 1;
            plus = 0;
        }

        currentWeatherListView.setDivider(null);
        currentWeatherListView.setDividerHeight(0);
        forecastWeatherListView.setDivider(null);
        forecastWeatherListView.setDividerHeight(0);

        selectedArea1 = sp.getString("selectedArea1", "");
        selectedArea2 = sp.getString("selectedArea2", "");
        selectedCountry = sp.getString("selectedCountry", "");
        location = (selectedArea1+" "+selectedArea2).trim();

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
                }
                getSupportActionBar().setTitle(location);
                setCurrentWeatherInfo(location);
                setForecastWeatherInfo(location);
                spEditor.putString("selectedArea1", selectedArea1);
                spEditor.putString("selectedArea2", selectedArea2);
                spEditor.putString("selectedCountry", selectedCountry);
                spEditor.commit();

                int actionBarColor = sp.getInt("actionBarColor", R.color.colorPrimary);
                int statusBarColor = sp.getInt("statusBarColor", R.color.colorPrimaryDark);
                getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(actionBarColor));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setStatusBarColor(getResources().getColor(statusBarColor));
                }
            }
        } else {
            getSupportActionBar().setTitle(location);
            setCurrentWeatherInfo(location);
            setForecastWeatherInfo(location);

            int actionBarColor = sp.getInt("actionBarColor", R.color.colorPrimary);
            int statusBarColor = sp.getInt("statusBarColor", R.color.colorPrimaryDark);
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(actionBarColor));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(getResources().getColor(statusBarColor));
            }
        }
    }

    //set current listview
    private void setCurrentWeatherInfo(String location){
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
                imgvSunrise.setImageResource(getSunImageId(Integer.parseInt(map.get("weather")), "simple_weather_icon_57"));
                imgvSunset.setImageResource(getSunImageId(Integer.parseInt(map.get("weather")), "simple_weather_icon_56"));

                //set text color acconding to bg color
                txtCurTemp.setTextColor(getResources().getColor(getFontColorId(Integer.parseInt(map.get("weather")))));
                txtCurSunrise.setTextColor(getResources().getColor(getFontColorId(Integer.parseInt(map.get("weather")))));
                txtCurSunset.setTextColor(getResources().getColor(getFontColorId(Integer.parseInt(map.get("weather")))));

                //set bg color
                spEditor.putInt("statusBarColor", getBackgroundDarkColorId(Integer.parseInt(map.get("weather"))));
                spEditor.putInt("actionBarColor", getBackgroundColorId(Integer.parseInt(map.get("weather"))));
                spEditor.commit();
                convertView.setBackgroundResource(getBackgroundColorId(Integer.parseInt(map.get("weather"))));
                imgvCurIcon.setImageResource(getWeatherImageId(Integer.parseInt(map.get("weather"))));
                return super.getView(position, convertView, parent);
            }};
        currentWeatherListView.setAdapter(mAdapter);
    }

    //set forecasr listview
    private void setForecastWeatherInfo(String location){
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

                convertView.setBackgroundResource(getBackgroundColorId(Integer.parseInt(map.get("weather"))));
                if(position == getCount()-1){
                    forecastWeatherListView.setBackgroundResource(getBackgroundColorId(Integer.parseInt(map.get("weather"))));
                }
                imgvForeIcon.setImageResource(getWeatherImageId(Integer.parseInt(map.get("weather"))));
                txtForeDay.setTextColor(getResources().getColor(getFontColorId(Integer.parseInt(map.get("weather")))));
                txtForeTemp.setTextColor(getResources().getColor(getFontColorId(Integer.parseInt(map.get("weather")))));
                return super.getView(position, convertView, parent);
            }};
        forecastWeatherListView.setAdapter(mAdapter);
    }

    //Return Color ImageId BackgroundId FontColor
    private Integer getWeatherImageId(int id){
        if(id >= 200 && id <= 232){return R.drawable.simple_weather_icon_27;} //Thunderstorm, Tornado
        else if(id >= 900 && id <= 902){return R.drawable.simple_weather_icon_27;} //Thunderstorm, Tornado
        else if(id >= 951 && id <= 956){return R.drawable.simple_weather_icon_30;} //Breeze
        else if(id >= 300 && id <= 321){return R.drawable.simple_weather_icon_21;} //Drizzle
        else if(id >= 500 && id <= 531){return R.drawable.simple_weather_icon_11;} //Rain
        else if(id >= 600 && id <= 622){return R.drawable.simple_weather_icon_24_black;} //Snow-
        else if(id >= 701 && id <= 781){return R.drawable.simple_weather_icon_10;} //Atmosphere
        else if(id == 800){return R.drawable.simple_weather_icon_01;} //Clear sky
        else if(id >= 801 && id <= 804){return R.drawable.simple_weather_icon_06_black;} //Clouds-
        else if(id == 903){return R.drawable.simple_weather_icon_53_black;} //Cold-
        else if(id == 904){return R.drawable.simple_weather_icon_55;} //Hot
        else if(id == 905){return R.drawable.simple_weather_icon_30;} //Windy
        else if(id >= 957 && id <= 962){return R.drawable.simple_weather_icon_30;} //Gale
        else if(id == 906){return R.drawable.simple_weather_icon_28_black;} //Hail-
        else{return R.drawable.simple_weather_icon_04_black;} //-
    }

    private Integer getBackgroundColorId(int id){
        if(id >= 200 && id <= 232){return R.color.weatherThunderstormBlue;}//Thunderstorm, Tornado
        else if(id >= 900 && id <= 902){return R.color.weatherThunderstormBlue;} //Thunderstorm, Tornado
        else if(id >= 951 && id <= 956){return R.color.weatherLightWindBrown;} //Breeze
        else if(id >= 300 && id <= 321){return R.color.weatherLightRainyBlue;} //Drizzle
        else if(id >= 500 && id <= 531){return R.color.weatherDarkRainyBlue;} //Rain
        else if(id >= 600 && id <= 622){return R.color.weatherSnowWhite;} //Snow-
        else if(id >= 701 && id <= 781){return R.color.weatherLightWindBrown;} //Atmosphere
        else if(id == 800){return R.color.weatherSunnyRed;} //Clear sky
        else if(id >= 801 && id <= 804){return R.color.weatherCloudCream;} //Clouds-
        else if(id == 903){return R.color.weatherSnowWhite;} //Cold-
        else if(id == 904){return R.color.weatherSunnyRed;} //Hot
        else if(id == 905){return R.color.weatherLightWindBrown;} //Windy
        else if(id >= 957 && id <= 962){return R.color.weatherDarkWindBrown;} //Gale
        else if(id == 906){return R.color.weatherSnowWhite;} //Hail-
        else{return R.color.weatherOther;} //-
    }

    private Integer getBackgroundDarkColorId(int id){
        if(id >= 200 && id <= 232){return R.color.weatherThunderstormBlueDark;}//Thunderstorm, Tornado
        else if(id >= 900 && id <= 902){return R.color.weatherThunderstormBlueDark;} //Thunderstorm, Tornado
        else if(id >= 951 && id <= 956){return R.color.weatherLightWindBrownDark;} //Breeze
        else if(id >= 300 && id <= 321){return R.color.weatherLightRainyBlueDark;} //Drizzle
        else if(id >= 500 && id <= 531){return R.color.weatherDarkRainyBlueDark;} //Rain
        else if(id >= 600 && id <= 622){return R.color.weatherSnowWhiteDark;} //Snow-
        else if(id >= 701 && id <= 781){return R.color.weatherLightWindBrownDark;} //Atmosphere
        else if(id == 800){return R.color.weatherSunnyRedDark;} //Clear sky
        else if(id >= 801 && id <= 804){return R.color.weatherCloudCreamDark;} //Clouds-
        else if(id == 903){return R.color.weatherSnowWhiteDark;} //Cold-
        else if(id == 904){return R.color.weatherSunnyRedDark;} //Hot
        else if(id == 905){return R.color.weatherLightWindBrownDark;} //Windy
        else if(id >= 957 && id <= 962){return R.color.weatherDarkWindBrownDark;} //Gale
        else if(id == 906){return R.color.weatherSnowWhiteDark;} //Hail-
        else{return R.color.weatherOtherDark;} //-
    }

    private Integer getFontColorId(int id){
        if(id >= 200 && id <= 232){return R.color.weatherFontOpacityBlack;}//Thunderstorm, Tornado
        else if(id >= 900 && id <= 902){return R.color.weatherFontWhite;} //Thunderstorm, Tornado
        else if(id >= 951 && id <= 956){return R.color.weatherFontWhite;} //Breeze
        else if(id >= 300 && id <= 321){return R.color.weatherFontWhite;} //Drizzle
        else if(id >= 500 && id <= 531){return R.color.weatherFontWhite;} //Rain
        else if(id >= 600 && id <= 622){return R.color.weatherFontOpacityBlack;} //Snow-
        else if(id >= 701 && id <= 781){return R.color.weatherFontWhite;} //Atmosphere
        else if(id == 800){return R.color.weatherFontWhite;} //Clear sky
        else if(id >= 801 && id <= 804){return R.color.weatherFontOpacityBlack;} //Clouds-
        else if(id == 903){return R.color.weatherFontOpacityBlack;} //Cold-
        else if(id == 904){return R.color.weatherFontWhite;} //Hot
        else if(id == 905){return R.color.weatherFontWhite;} //Windy
        else if(id >= 957 && id <= 962){return R.color.weatherFontWhite;} //Gale
        else if(id == 906){return R.color.weatherFontOpacityBlack;} //Hail-
        else{return R.color.weatherFontOpacityBlack;} //-
    }

    private Integer getSunImageId(int id,String imageId){
        if(id >= 200 && id <= 232){return getResources().getIdentifier(imageId, "drawable", getApplicationContext().getPackageName());} //Thunderstorm, Tornado
        else if(id >= 900 && id <= 902){return getResources().getIdentifier(imageId, "drawable", getApplicationContext().getPackageName());} //Thunderstorm, Tornado
        else if(id >= 951 && id <= 956){return getResources().getIdentifier(imageId, "drawable", getApplicationContext().getPackageName());} //Breeze
        else if(id >= 300 && id <= 321){return getResources().getIdentifier(imageId, "drawable", getApplicationContext().getPackageName());} //Drizzle
        else if(id >= 500 && id <= 531){return getResources().getIdentifier(imageId, "drawable", getApplicationContext().getPackageName());} //Rain
        else if(id >= 600 && id <= 622){return getResources().getIdentifier(imageId+"_black", "drawable", getApplicationContext().getPackageName());} //Snow-
        else if(id >= 701 && id <= 781){return getResources().getIdentifier(imageId, "drawable", getApplicationContext().getPackageName());} //Atmosphere
        else if(id == 800){return getResources().getIdentifier(imageId, "drawable", getApplicationContext().getPackageName());} //Clear sky
        else if(id >= 801 && id <= 804){return getResources().getIdentifier(imageId+"_black", "drawable", getApplicationContext().getPackageName());} //Clouds-
        else if(id == 903){return getResources().getIdentifier(imageId+"_black", "drawable", getApplicationContext().getPackageName());} //Cold-
        else if(id == 904){return getResources().getIdentifier(imageId, "drawable", getApplicationContext().getPackageName());} //Hot
        else if(id == 905){return getResources().getIdentifier(imageId, "drawable", getApplicationContext().getPackageName());} //Windy
        else if(id >= 957 && id <= 962){return getResources().getIdentifier(imageId, "drawable", getApplicationContext().getPackageName());} //Gale
        else if(id == 906){return getResources().getIdentifier(imageId+"_black", "drawable", getApplicationContext().getPackageName());} //Hail-
        else{return getResources().getIdentifier(imageId+"_black", "drawable", getApplicationContext().getPackageName());} //-
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
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
}
