package jp.or.ixqsware.tospeech;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import static jp.or.ixqsware.tospeech.Constants.ACTION_SPEECH;
import static jp.or.ixqsware.tospeech.Constants.EXTRA_CONTENT;
import static jp.or.ixqsware.tospeech.Constants.OPTION_TIME;
import static jp.or.ixqsware.tospeech.Constants.OPTION_WEATHER;

/**
 * This fragment's purpose is checking operation.
 *
 * Created by hisanaka on 15/08/17.
 */
public class MainFragment extends Fragment {
    public MainFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_time, container, false);

        Button timeButton = (Button) rootView.findViewById(R.id.speech_time);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent();
                intent.setAction(ACTION_SPEECH);
                intent.putExtra(EXTRA_CONTENT, OPTION_TIME);
                context.sendBroadcast(intent);
            }
        });

        Button weatherButton = (Button) rootView.findViewById(R.id.speech_weather);
        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent();
                intent.setAction(ACTION_SPEECH);
                intent.putExtra(EXTRA_CONTENT, OPTION_WEATHER);
                context.sendBroadcast(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
