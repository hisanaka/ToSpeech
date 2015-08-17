package jp.or.ixqsware.tospeech;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

import static jp.or.ixqsware.tospeech.Constants.ACTION_TIME_SPEECH;

/**
 * This fragment's purpose is checking operation.
 *
 * Created by hisanaka on 15/08/17.
 */
public class TimeFragment extends Fragment {

    public TimeFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_time, container, false);

        TextView timeView = (TextView) rootView.findViewById(R.id.display_text);

        Calendar calendar = Calendar.getInstance();
        String mTime = (String) DateFormat.format("kk:mm", calendar.getTime());
        timeView.setText(mTime);

        Button speechButton = (Button) rootView.findViewById(R.id.speech_button);
        speechButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent();
                intent.setAction(ACTION_TIME_SPEECH);
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
