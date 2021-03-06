package jp.or.ixqsware.tospeech;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static jp.or.ixqsware.tospeech.Constants.ACTION_SPEECH;
import static jp.or.ixqsware.tospeech.Constants.EXTRA_CONTENT;

/**
 * BroadcastReceiver to catch the action "jp.or.ixqsware.tospeech.action.TIME_SPEECH"
 * and to start the time guidance service.
 *
 * Created by hisanaka on 15/08/17.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_SPEECH.equals(action)) {
            Intent serviceIntent = new Intent(context, SpeechService.class);
            serviceIntent.putExtra(EXTRA_CONTENT, intent.getStringExtra(EXTRA_CONTENT));
            context.startService(serviceIntent);
        }
    }
}
