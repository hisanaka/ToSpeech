package jp.or.ixqsware.tospeech;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Locale;

/**
 * Time guidance service.
 *
 * Created by hisanaka on 15/08/17.
 */
public class SpeechService extends Service {
    private TextToSpeech tts = null;

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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        tts = new TextToSpeech(getApplicationContext(), speechInitListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        super.onStartCommand(intent, flags, startID);
        if (tts != null) {
            if (tts.isSpeaking()) { tts.stop(); }
            Calendar calendar = Calendar.getInstance();
            String mAmPm = calendar.get(Calendar.AM_PM) == 0 ?
                    getString(R.string.am_label) : getString(R.string.pm_label);
            int mHour = calendar.get(Calendar.HOUR);
            int mMin = calendar.get(Calendar.MINUTE);
            tts.speak(
                    getString(R.string.guidance_text, mAmPm, mHour, mMin),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    String.valueOf(calendar.getTimeInMillis())
            );
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) { tts.shutdown(); }
    }
}
