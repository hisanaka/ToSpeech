package jp.or.ixqsware.tospeech;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static jp.or.ixqsware.tospeech.Constants.EXTRA_CONTENT;
import static jp.or.ixqsware.tospeech.Constants.OPTION_TIME;
import static jp.or.ixqsware.tospeech.Constants.OPTION_WEATHER;
import static jp.or.ixqsware.tospeech.Constants.URL_OPEN_WEATHER_WEB_API;

/**
 * Guidance service.
 *
 * Created by hisanaka on 15/08/17.
 */
public class SpeechService extends Service {
    private TextToSpeech tts = null;
    private LocationManager locationManager;
    private String mContents;
    private Timer timer;

    private TextToSpeech.OnInitListener speechInitListener = new TextToSpeech.OnInitListener() {
        @Override
        public void onInit(int status) {
            switch (status) {
                case TextToSpeech.SUCCESS:
                    Locale loc = Locale.getDefault();
                    if (tts.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE) {
                        tts.setLanguage(loc);
                    } else {
                        tts.setLanguage(Locale.ENGLISH);
                    }
                    if (tts.isSpeaking()) { tts.stop(); }
                    Calendar calendar = Calendar.getInstance();
                    tts.speak(
                            mContents,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            String.valueOf(calendar.getTimeInMillis())
                    );
                    break;

                default:
                    tts = null;
                    Toast.makeText(getApplicationContext(), "Failed to init.", Toast.LENGTH_SHORT)
                            .show();
                    stopSelf();
                    break;
            }
        }
    };

    private LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            locationManager.removeUpdates(networkLocationListener);
            ObtainForecastTask task = new ObtainForecastTask(this);
            task.execute(longitude, latitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    private LocationListener networkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            locationManager.removeUpdates(gpsLocationListener);
            ObtainForecastTask task = new ObtainForecastTask(this);
            task.execute(longitude, latitude);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        super.onStartCommand(intent, flags, startID);

        String mTarget = intent.getStringExtra(EXTRA_CONTENT);
        switch (mTarget) {
            case OPTION_TIME:
                Calendar calendar = Calendar.getInstance();
                String mAmPm = calendar.get(Calendar.AM_PM) == 0 ?
                        getString(R.string.am_label) : getString(R.string.pm_label);
                int mHour = calendar.get(Calendar.HOUR);
                int mMin = calendar.get(Calendar.MINUTE);
                mContents = getString(R.string.guidance_text, mAmPm, mHour, mMin);
                toSpeech();
                break;

            case OPTION_WEATHER:
                requestLocation();
                break;

            default:
                return START_NOT_STICKY;
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
    }

    private void toSpeech() {
        if (tts == null) {
            tts = new TextToSpeech(getApplicationContext(), speechInitListener);
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                }

                @Override
                public void onDone(String utteranceId) {
                    tts.shutdown();
                    tts = null;
                }

                @Override
                public void onError(String utteranceId) {
                }
            });
        }
    }

    private void requestLocation() {
        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        String bestProvider = locationManager.getBestProvider(criteria, true);

        timer = new Timer();
        timer.schedule(new LocationUpdateCancelTimerTask(), 3000);

        if (LocationManager.GPS_PROVIDER.equals(bestProvider)) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 1500, 0.01f, networkLocationListener);
        }
        locationManager.requestLocationUpdates(bestProvider, 500, 0.01f, gpsLocationListener);
    }

    private class LocationUpdateCancelTimerTask extends TimerTask {
        @Override
        public void run() {
            if (locationManager != null) {
                locationManager.removeUpdates(networkLocationListener);
                locationManager.removeUpdates(gpsLocationListener);
                locationManager = null;
            }
            mContents = getString(R.string.failed_to_get_location);
            toSpeech();
            timer.cancel();
        }
    }

    private class ObtainForecastTask extends AsyncTask<Double, Void, String> {
        private LocationListener mListener;

        public ObtainForecastTask(LocationListener listener) {
            super();
            mListener = listener;
        }

        @Override
        protected String doInBackground(Double... params) {
            double longitude = params[0];
            double latitude = params[1];
            String result = null;
            HttpURLConnection connection = null;
            InputStream inputStream = null;
            try {
                URL url = new URL(URL_OPEN_WEATHER_WEB_API + "lat=" + latitude
                        + "&lon=" + longitude + "&units=metric");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                if (connection.getResponseCode() != 200) {
                    return getString(R.string.failed_to_get_forecast);
                }

                inputStream = connection.getInputStream();
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                String mLine;
                while ((mLine = br.readLine()) != null) {
                    sb.append(mLine);
                }
                inputStream.close();
                Log.d("DEBUG:", "JSON: " + sb.toString());

                JSONObject json = new JSONObject(sb.toString());
                String resultCode = json.getString("cod");
                if (!resultCode.matches("20\\d+")) {
                    return getString(R.string.failed_to_get_forecast);
                }

                String location = json.getString("name");

                JSONArray arrWeather = json.getJSONArray("weather");
                String iconNow = arrWeather.getJSONObject(0).getString("icon");
                String weather = iconToString(iconNow);

                String forecast = getString(R.string.weather_unknown);
                if (arrWeather.length() > 1) {
                    String iconNext = arrWeather.getJSONObject(1).getString("icon");
                    forecast = iconToString(iconNext);
                }

                JSONObject objMain = json.getJSONObject("main");
                int temp = objMain.getInt("temp");
                int maxTemp = objMain.getInt("temp_max");

                result = getString(R.string.forecast_guidance,
                        location, weather, temp, maxTemp, forecast);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                result = getString(R.string.error_result);
            } finally {
                if (connection != null) { connection.disconnect(); }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null) { return; }
            mContents = result;
            locationManager.removeUpdates(mListener);
            timer.cancel();
            toSpeech();
        }

        private String iconToString(String iconName) {
            String forecast;
            switch (iconName) {
                case "01d":
                case "01n":
                    forecast = getString(R.string.weather_clear_sky);
                    break;
                case "02d":
                case "02n":
                    forecast = getString(R.string.weather_few_clouds);
                    break;
                case "03d":
                case "03n":
                    forecast = getString(R.string.weather_scattered_clouds);
                    break;
                case "04d":
                case "04n":
                    forecast = getString(R.string.weather_broken_clouds);
                    break;
                case "09d":
                case "09n":
                    forecast = getString(R.string.weather_shower_rain);
                    break;
                case "10d":
                case "10n":
                    forecast = getString(R.string.weather_rain);
                    break;
                case "11d":
                case "11n":
                    forecast = getString(R.string.weather_thunderstorm);
                    break;
                case "13d":
                case "13n":
                    forecast = getString(R.string.weather_snow);
                    break;
                case "50d":
                case "50n":
                    forecast = getString(R.string.weather_mist);
                    break;
                default:
                    forecast = getString(R.string.weather_unknown);
            }
            return forecast;
        }

    }
}
