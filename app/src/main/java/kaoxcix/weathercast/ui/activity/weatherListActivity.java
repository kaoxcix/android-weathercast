package kaoxcix.weathercast.ui.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import kaoxcix.weathercast.R;

public class weatherListActivity extends AppCompatActivity {
    private ListView locationListView;
    private ArrayList<HashMap<String,String>> locationArrayList;
    private SimpleAdapter locationAdapter;
    private final Uri uriLocation = Uri.parse("content://weatherCastDB/location");
    private final Uri uriWeather = Uri.parse("content://weatherCastDB/Weather");
    private SharedPreferences sp;
    private SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weathet_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initSharedPreferences();
        initInstances();
        initLocationListView();
    }

    private void initSharedPreferences() {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        spEditor = sp.edit();
    }

    private void initInstances() {
        locationListView = (ListView) findViewById(R.id.locationListView);
    }

    private void initLocationListView() {
        Cursor cursor = getApplicationContext().getContentResolver().query(uriLocation, null, null, null, null);

        String[] from = new String[]{"area1", "other"};
        int[] to = new int[]{R.id.txtDrawerArea1, R.id.txtDrawerOther};
        int layout = R.layout.list_weather_show_location_list;
        locationArrayList = new ArrayList<HashMap<String,String>>();
        while(cursor.moveToNext()) {
            HashMap<String,String> map = new HashMap<String,String>();
            String area1 = cursor.getString(cursor.getColumnIndex("area1")),
                    area2 = cursor.getString(cursor.getColumnIndex("area2")),
                    country = cursor.getString(cursor.getColumnIndex("country"));
            map.put("area1", area1);
            map.put("area2", area2);
            map.put("country", country);
            if(area2.isEmpty()){
                map.put("other", country);
            }
            else{
                map.put("other", area2+", "+country);
            }
            locationArrayList.add(map);
        }

        locationAdapter = new SimpleAdapter(weatherListActivity.this, locationArrayList, layout, from, to){
            //Change layout for each item
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                LayoutInflater Inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = Inflater.inflate(R.layout.list_weather_show_location_list, null);
                    HashMap<String,String> getMap = new HashMap<String,String>(locationArrayList.get(position));
                    final String selectedArea1 = getMap.get("area1");
                    final String selectedArea2 = getMap.get("area2");
                    final String selectedArea = (selectedArea1+" "+selectedArea2).trim();
                    ImageView mImgvDelete = (ImageView) convertView.findViewById(R.id.imgvDelete);
                    mImgvDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            final AlertDialog.Builder dialog = new AlertDialog.Builder(weatherListActivity.this);
                            dialog.setTitle(getString(R.string.title_delete_location));
                            dialog.setMessage(getString(R.string.message_delete_location));
                            dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getApplicationContext().getContentResolver().delete(uriLocation, "area1 = '"+selectedArea1+"' and area2 = '"+selectedArea2+"'", null);
                                    getApplicationContext().getContentResolver().delete(uriWeather, "area = '"+selectedArea+"'", null);
                                    locationArrayList.remove(position);
                                    locationAdapter.notifyDataSetChanged();
                                    dialog.dismiss();
                                    Snackbar.make(view, selectedArea+" has been deleted", Snackbar.LENGTH_SHORT).show();
                                }
                            });
                            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                        }
                    });
                return super.getView(position, convertView, parent);
            }};
        locationListView.setAdapter(locationAdapter);

        locationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,String> getMap = new HashMap<String,String>(locationArrayList.get(position));
                String selectedArea1 = getMap.get("area1");
                String selectedArea2 = getMap.get("area2");
                String selectedCountry = getMap.get("country");
                spEditor.putString("selectedArea1", selectedArea1);
                spEditor.putString("selectedArea2", selectedArea2);
                spEditor.putString("selectedCountry", selectedCountry);
                spEditor.commit();
                Intent intent = new Intent(weatherListActivity.this, mainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = new Intent(weatherListActivity.this, mainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
