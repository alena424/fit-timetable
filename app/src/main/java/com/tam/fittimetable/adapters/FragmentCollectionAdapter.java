package com.tam.fittimetable.adapters;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.tam.fittimetable.fragments.DayFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FragmentCollectionAdapter extends FragmentPagerAdapter {
    public FragmentCollectionAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        DayFragment dayFragment = new DayFragment();
        Bundle bundle = new Bundle();
        position = position+1;
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MM. yyyy");
        Calendar c = Calendar.getInstance();
        position = position - 101; // zaciname ve 100
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.add(Calendar.DAY_OF_MONTH, position * 7);


        Calendar till =  Calendar.getInstance();
        till.add(Calendar.DAY_OF_MONTH, position * 7 + 5);

        //Date after adding the days to the given date
        String newDate = sdf.format(c.getTime());
        String fridayDate = sdf.format(till.getTime());


        bundle.putString("date", newDate + " - " +fridayDate);
        bundle.putString("message", "Actual + "+ position + " day");
        System.out.println("Adding one day to the given date: "+newDate);
        dayFragment.setArguments(bundle);
        return dayFragment;
    }

    @Override
    public int getCount() {
        return 500;
    }
}
