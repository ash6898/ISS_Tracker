package com.example.isstracker;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class FragmentNotify extends Fragment {

    static ArrayList<Integer> durationList = new ArrayList<>();
    static ArrayList<Long> timestampList = new ArrayList<>();
    String myLatitude,myLongitude;
    CardView passInfo;
    TextView passInfoTxt;

    FusedLocationProviderClient flpClient;

    @SuppressLint("StaticFieldLeak")
    static NotifyRecyclerViewAdapter notifyRecyclerViewAdapter;
    @SuppressLint("StaticFieldLeak")
    static RecyclerView recyclerView;
    @SuppressLint("StaticFieldLeak")
    static View view;
    @SuppressLint("StaticFieldLeak")
    static Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentNotify.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.notify_fragment,container,false);

        flpClient = LocationServices.getFusedLocationProviderClient(context);

        recyclerView = view.findViewById(R.id.notify_recycler_view);

        if(isNetworkConnected() || isInternetAvailable()){

            Task<Location> task = flpClient.getLastLocation();
            task.addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(final Location location) {
                    //When task succeeded
                    if(location != null){
                        myLatitude = String.valueOf(location.getLatitude());
                        myLongitude = String.valueOf(location.getLongitude());

                        FetchIssPassTime fetchIssPassTime = new FetchIssPassTime(context);
                        fetchIssPassTime.execute(myLatitude, myLongitude);
                    }
                }
            });
        }

        passInfo = view.findViewById(R.id.passInfo);
        passInfoTxt = view.findViewById(R.id.pass_info_txt);
        passInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionManager.beginDelayedTransition(passInfo);
                if(passInfoTxt.getVisibility() == View.GONE) {
                    passInfoTxt.setVisibility(View.VISIBLE);
                }
                else{
                    passInfoTxt.setVisibility(View.GONE);
                }
            }
        });

        return view;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    public void onResume() {
        super.onResume();
        try{
            recyclerView.setAdapter(notifyRecyclerViewAdapter);
            /*Fragment frg;
            frg = getChildFragmentManager().findFragmentByTag("notify_fragment");
            final FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            if(frg!=null) {
                ft.detach(frg);
                ft.attach(frg);
                ft.commit();
            }*/
        }
        catch (NullPointerException npe){
            Log.d("npe", npe.getMessage());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        recyclerView.setAdapter(null);
    }

    public boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.toString().equals("");

        } catch (Exception e) {
            return false;
        }
    }

    public static void updateDuration(ArrayList<Integer> duration){
        durationList = duration;
    }

    public static void updateTimestamp(ArrayList<Long> timestamp){
        timestampList = timestamp;

        ArrayList<String> istTime = convertUnixTimestampToIst(timestampList);
        ArrayList<String> durationInMin = new ArrayList<>();

        //String[] tsArray = new String[timestamp.size()];
        for(int index=0;index<durationList.size();index++){
            durationInMin.add((int)(Math.ceil(durationList.get(index)/60)) + " min approx");
        }

        notifyRecyclerViewAdapter = new NotifyRecyclerViewAdapter(context, durationInMin, istTime);
        recyclerView.setAdapter(notifyRecyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }
    public static ArrayList<String> convertUnixTimestampToIst(ArrayList<Long> timestamp) {
        ArrayList<String> istTime = new ArrayList<>();
        for (int index = 0; index < timestamp.size(); index++) {
            // convert seconds to milliseconds
            Date date = new java.util.Date(timestamp.get(index) * 1000L);
            // the format of your date
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy\nh:mm a", Locale.ENGLISH);
            // give a timezone reference for formatting (see comment at the bottom)
            sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Kolkata"));
            String formattedDate = sdf.format(date);
            //System.out.println(formattedDate);
            istTime.add(formattedDate);
            date.getTime();
        }
        return istTime;
    }
}

class FetchIssPassTime extends AsyncTask<String, Void, JSONObject>{

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ProgressDialog pd;

    private ArrayList<Long> timestampList = new ArrayList<>();
    private ArrayList<Integer> durationList = new ArrayList<>();

    FetchIssPassTime(Context context) {
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {
        String latitude = strings[0];
        String longitude = strings[1];
        String url = "http://api.open-notify.org/iss-pass.json?lat=" + latitude + "&lon=" + longitude;

        RequestQueue queue = Volley.newRequestQueue(context);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray responseObject = response.getJSONArray("response");
                    for(int index=0;index<responseObject.length();index++){
                        JSONObject passObject = responseObject.getJSONObject(index);
                        int duration = passObject.getInt("duration");
                        long timestamp = passObject.getLong("risetime");
                        durationList.add(duration);
                        timestampList.add(timestamp);
                    }
                    FragmentNotify.updateDuration(durationList);
                    FragmentNotify.updateTimestamp(timestampList);
                } catch (JSONException e) {
                    Toast.makeText(context,
                            e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Toast.makeText(context,
                            "No Internet Connection",
                            Toast.LENGTH_LONG).show();

                }
            }
        });
        queue.add(jsonObjectRequest);

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        pd = new ProgressDialog(context);
        pd.setMessage("Loading Information...");
        pd.setCancelable(false);
        pd.show();
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        if (pd.isShowing()){
            pd.dismiss();
        }
    }
}