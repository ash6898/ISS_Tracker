package com.example.isstracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static Context context;
    TabLayout mainTabLayout;
    ViewPager mainViewPager;
    MainPagerAdapter mainPagerAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        mainTabLayout = findViewById(R.id.main_tablayout);
        mainViewPager = findViewById(R.id.main_viewpager);

        mainPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mainPagerAdapter.addFragment(new FragmentTrack(),"Track");
        mainPagerAdapter.addFragment(new FragmentInfo(),"INFO");
        mainPagerAdapter.addFragment(new FragmentNotify(),"NOTIFY");

        mainViewPager.setAdapter(mainPagerAdapter);
        mainTabLayout.setupWithViewPager(mainViewPager);
    }
}
