package sunshine.android.example.com.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ShareActionProvider;
import android.widget.TextView;

public class ChildActivity extends Activity {

    private static final String LOG_TAG = ChildActivity.class.getSimpleName();

    private ShareActionProvider mShareActionProvider;

    private String mForecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {

            mForecast = intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView) findViewById(R.id.day_weather_detail)).setText(mForecast);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.child, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem shareEntry = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) shareEntry.getActionProvider();

        if (mShareActionProvider != null)
            mShareActionProvider.setShareIntent(getShareIntent());
        else
            Log.e(LOG_TAG, "Share Action Provider is null");

        return super.onCreateOptionsMenu(menu);
    }

    private Intent getShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String app_hashtag = " #SunshineApp";
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + app_hashtag);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");

        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_refresh:
                // TODO
                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

