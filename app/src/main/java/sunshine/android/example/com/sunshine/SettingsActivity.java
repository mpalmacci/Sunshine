package sunshine.android.example.com.sunshine;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.view.MenuItem;

public class SettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
