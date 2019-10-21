package com.tam.fittimetable.activities;

import android.os.Bundle;

import androidx.viewpager.widget.ViewPager;

import com.tam.fittimetable.R;
import com.tam.fittimetable.adapters.FragmentCollectionAdapter;


public class TimetableActivity extends SampleActivityBase {

    private ViewPager viewPager;
    private FragmentCollectionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = findViewById(R.id.pager);

        adapter = new FragmentCollectionAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(100);

        //Toolbar toolbar = findViewById(R.id.toolBar);
       // setSupportActionBar(toolbar);

    }


}
