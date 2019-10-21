package com.tam.fittimetable.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.tam.fittimetable.R;

/**
 * DayFragment class
 */
public class DayFragment extends Fragment {

    private TextView textView;


    public DayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_day, container, false);
        textView = view.findViewById(R.id.txt_display);

        TextView weekTextView = view.findViewById(R.id.date);
        TextView messageWeekTextView = view.findViewById(R.id.dateInfo);

        LinearLayout dayLayout = view.findViewById(R.id.dayLayout);
        LinearLayout timeLayout = view.findViewById(R.id.timeLayout);
        String[] days = {"Po", "Út", "St", "Čt", "Pá"};
        String[] times = { "7:00", "8:00","9:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00","19:00", "20:00"};

        for (int i = 0; i < days.length; i++) {
            System.out.println(days[i]);
            TextView day = new TextView(getActivity());
            day.setTextSize(20);
            day.setText(days[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1
            );
            day.setLayoutParams(params);
            dayLayout.addView(day);

        }

        for (int i = 0; i < times.length; i++) {
            System.out.println(times[i]);
            TextView day = new TextView(getActivity());
            day.setTextSize(20);
            day.setText(times[i]);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0, 1
            );
            day.setLayoutParams(params);
            timeLayout.addView(day);
        }

        String dateMessage = getArguments().getString("date");
        String dateMessageSmall = getArguments().getString("message");
       // textView.setText(message);
        weekTextView.setText(dateMessage);
        messageWeekTextView.setText(dateMessageSmall);
        return view;
    }


}
