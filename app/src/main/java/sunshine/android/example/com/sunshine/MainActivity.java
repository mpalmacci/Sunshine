package sunshine.android.example.com.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private ArrayAdapter<String> forecastListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        forecastListAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>());

        ListView forecastListView = (ListView) findViewById(R.id.list_view_forecast);
        forecastListView.setAdapter(forecastListAdapter);

        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = forecastListAdapter.getItem(position);
                // Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), ChildActivity.class).putExtra(Intent.EXTRA_TEXT, text);
                startActivity(intent);
            }
        });
        Log.i(LOG_TAG, "onCreate Called");
    }

    @Override
    protected void onStart() {
        super.onStart();
        executeWeatherTaskUpdate();
        Log.i(LOG_TAG, "onStart Called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(LOG_TAG, "onPause Called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop Called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume Called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy Called");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart Called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    private void executeWeatherTaskUpdate() {
        String zip = getCurrentLocation();
        String units = getUnitsPref();

        FetchWeatherTask weeklyWeather = new FetchWeatherTask();
        weeklyWeather.execute(zip, units);
    }

    private String getCurrentLocation() {
        return getSharedPrefs().getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
    }

    private String getUnitsPref() {
        return getSharedPrefs().getString(getString(R.string.pref_units_key), getString(R.string.pref_units_default));
    }

    private SharedPreferences getSharedPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case (R.id.action_refresh):
                executeWeatherTaskUpdate();
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.view_location:
                openPreferredLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openPreferredLocation() {
        Intent locationIntent = new Intent(Intent.ACTION_VIEW);
        Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q", getCurrentLocation()).build();
        locationIntent.setData(geoLocation);
        if (locationIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(locationIntent);
        } else {
            Toast.makeText(getApplicationContext(), "You do not have any apps to open a map", Toast.LENGTH_SHORT).show();
        }
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, List<String>> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected List<String> doInBackground(String... params) {
            if (params.length == 0)
                return null;

            WeatherDataParser parser = new WeatherDataParser(LOG_TAG);
            return parser.getSevenDayForecastJson(params[0], params[1], "json", 7);
        }

        @Override
        protected void onPostExecute(List<String> result) {
            if (result != null) {
                forecastListAdapter.clear();
                forecastListAdapter.addAll(result);
            }

            super.onPostExecute(result);
        }
    }
}