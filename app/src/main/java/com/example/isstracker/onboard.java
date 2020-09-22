package com.example.isstracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.util.Objects;

public class onboard extends AppCompatActivity {

    ViewPager onboard_viewpager;
    OnboardPagerAdapter onboardPagerAdapter;
    TabLayout onboard_tablayout;
    Button back, next;
    int position = 0;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Objects.requireNonNull(getSupportActionBar()).hide();

        if (fetch_shared_pref()) {
            startMainActivity();
            finish();
        }

        setContentView(R.layout.activity_onboard);

        onboardPagerAdapter = new OnboardPagerAdapter(this);
        onboard_viewpager = findViewById(R.id.viewpager_onboard);
        onboard_viewpager.setAdapter(onboardPagerAdapter);

        onboard_tablayout = findViewById(R.id.tablayout1);
        onboard_tablayout.setupWithViewPager(onboard_viewpager);

        back = findViewById(R.id.btn_back);
        next = findViewById(R.id.btn_next);

        onboard_tablayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() < 2) {
                    next.setText(R.string.btn_next);
                    back.setVisibility(View.VISIBLE);
                }
                if (tab.getPosition() == 0) {
                    back.setVisibility(View.INVISIBLE);
                } else if (tab.getPosition() == 2) {
                    next.setText(R.string.btn_start);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = onboard_viewpager.getCurrentItem();
                if (position == 2) {
                    startMainActivity();
                    set_prefs();
                    finish();
                }
                if (position == 1) {
                    position += 1;
                    onboard_viewpager.setCurrentItem(position);
                    next.setText(R.string.btn_start);
                }
                if (position < 1) {
                    position += 1;
                    onboard_viewpager.setCurrentItem(position);
                    back.setVisibility(View.VISIBLE);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position = onboard_viewpager.getCurrentItem();
                if (position == 1) {
                    position -= 1;
                    next.setText(R.string.btn_next);
                    onboard_viewpager.setCurrentItem(position);
                    back.setVisibility(View.INVISIBLE);
                }
                if (position > 1) {
                    position -= 1;
                    next.setText(R.string.btn_next);
                    onboard_viewpager.setCurrentItem(position);
                }
            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private boolean fetch_shared_pref() {

        SharedPreferences get_shared_pref = getApplicationContext().getSharedPreferences("on_board", MODE_PRIVATE);
        return get_shared_pref.getBoolean("is_main_open", false);
    }

    private void set_prefs() {
        SharedPreferences is_start_clicked = getApplicationContext().getSharedPreferences("on_board", MODE_PRIVATE);
        SharedPreferences.Editor editor = is_start_clicked.edit();
        editor.putBoolean("is_main_open", true);
        editor.apply();
    }
}
