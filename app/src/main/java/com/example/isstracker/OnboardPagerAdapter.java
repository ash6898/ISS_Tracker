package com.example.isstracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class OnboardPagerAdapter extends PagerAdapter {

    private Context context;

    OnboardPagerAdapter(Context context) {
        this.context = context;
    }

    private int[] images = {
            R.drawable.track_icon,
            R.drawable.info_icon,
            R.drawable.notify_icon
    };

    private String[] headings = {
            "TRACK",
            "INFO",
            "NOTIFY"
    };

    private String[] descriptions = {
            "Track ISS live location",
            "Get all current information about ISS",
            "Get notified when ISS is nearby you"
    };

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View view = layoutInflater.inflate(R.layout.onboard_pager_adapter, null);

        ImageView image = view.findViewById(R.id.imageview1);
        TextView heading = view.findViewById(R.id.txt_heading);
        TextView description = view.findViewById(R.id.txt_description);

        image.setImageResource(images[position]);
        heading.setText(headings[position]);
        description.setText(descriptions[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }

    @Override
    public int getCount() {
        return headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }
}
