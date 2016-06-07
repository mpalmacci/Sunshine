package sunshine.android.example.com.sunshine;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class WeatherDataParser {

    private final String LOG_TAG;

    private final String OPEN_WEATHER_APP_ID = "3c53d23f9cd3d026ff3c628651ffaf34";
    private final String DAILY_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily",
            ZIP_PARAM = "zip", FORMAT_PARAM = "mode", UNITS_PARAM = "units", DAYS_PARAM = "cnt",
            APPLICATION_ID_PARAM = "appid";

    public WeatherDataParser(String logTag) {
        this.LOG_TAG = logTag;
    }

    public List<String> getSevenDayForecastJson(String zip, String metric, String format, int numOfDays) {
        Uri uri = Uri.parse(DAILY_BASE_URL).buildUpon()
                .appendQueryParameter(ZIP_PARAM, zip)
                .appendQueryParameter(FORMAT_PARAM, format)
                .appendQueryParameter(UNITS_PARAM, metric)
                .appendQueryParameter(DAYS_PARAM, Integer.toString(numOfDays))
                .appendQueryParameter(APPLICATION_ID_PARAM, OPEN_WEATHER_APP_ID)
                .build();

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            URL url = new URL(uri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null)
                // Nothing to do.
                return null;

            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null)
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");

            if (buffer.length() == 0)
                // Stream was empty.  No point in parsing.
                return null;

            forecastJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();

            if (reader != null)
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
        }

        try {
            return getWeatherDataFromJson(new JSONObject(forecastJsonStr));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error getting JSON Object", e);
        }

        return null;
    }

    /**
     * The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd", Locale.US);
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        return roundedHigh + "/" + roundedLow;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * <p/>
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private List<String> getWeatherDataFromJson(JSONObject forecastJson)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);


        List<String> resultStrs = new ArrayList<>();
        for (int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            //create a Gregorian Calendar, which is in current date
            GregorianCalendar gc = new GregorianCalendar();
            //add i dates to current date of calendar
            gc.add(GregorianCalendar.DATE, i);
            //get that date, format it, and "save" it on variable day
            Date time = gc.getTime();
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd", Locale.US);
            day = shortenedDateFormat.format(time);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs.add(day + " - " + description + " - " + highAndLow);
        }

        return resultStrs;
    }

}
