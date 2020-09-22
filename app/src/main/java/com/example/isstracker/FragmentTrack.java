package com.example.isstracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentTrack extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    //initialize variables
    @SuppressLint("StaticFieldLeak")
    static Context context;
    SupportMapFragment mapFragment;
    Button trackIss;
    Button stopIssTrack;
    Button viewMyLocation;
    static GoogleMap mMap;
    static Marker issMarker;
    static Marker myMarker;
    TimerTask doAsynchronousTask;
    Timer timer;
    private static Circle issRadius;
    private static CircleOptions issPast;
    static Boolean firstTime = true;

    FusedLocationProviderClient flpClient;

    public static final int REQUEST_CHECK_SETTINGS = 1;

    View view;
    private FetchIssLocation fetchIssLocation;

    public FragmentTrack() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentTrack.context = context;
    }


    @SuppressLint("CommitTransaction")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.track_fragment, container, false);
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);

        //assign variables
        flpClient = LocationServices.getFusedLocationProviderClient(context);

        trackIss = view.findViewById(R.id.issTrackBtn);
        stopIssTrack = view.findViewById(R.id.issStopBtn);
        viewMyLocation = view.findViewById(R.id.viewMyLocBtn);

        mapFragment.getMapAsync(this);

        fetchIssLocation = new FetchIssLocation(context);

        //track ISS button
        trackIss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //When track ISS button is clicked
                fetchIssLocation = new FetchIssLocation(context);
                fetchIssLocation.execute();
                callAsynchronousTask();
                //Toast.makeText(MainActivity.this,"latitude " + lat + "\nlongitude " + lon, Toast.LENGTH_LONG).show();
                trackIss.setVisibility(View.INVISIBLE);
                stopIssTrack.setVisibility(View.VISIBLE);
                firstTime = true;

            }
        });

        stopIssTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    //issMarker.remove();
                    fetchIssLocation.cancel(true);
                    doAsynchronousTask.cancel();
                    timer.cancel();
                    stopIssTrack.setVisibility(View.INVISIBLE);
                    trackIss.setVisibility(View.VISIBLE);
                    Toast.makeText(context, "ISS tracking stopped", Toast.LENGTH_LONG).show();
                }
                catch (NullPointerException npe){
                    Toast.makeText(context, npe.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d("already","NullPE");

                }
            }
        });

        viewMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCurrentLocation();
            }
        });

        //check permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //When permission granted
            //calling method
            getCurrentLocation();
        }
        else {
            //When permission denied
            //request permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CHECK_SETTINGS);
            }
        return view;
    }

    public void updateMap() {

        try{
            mMap.setOnMarkerClickListener(this);

            issMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0,0))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .snippet("This marker shows the location of ISS")
                    .visible(false));
            myMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(0,0))
                    .snippet("This marker shows your current location")
                    .visible(false));

            issRadius = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(0,0))
                    .radius(80000)
                    .strokeWidth(2)
                    .strokeColor(Color.BLUE)
                    .fillColor(Color.parseColor("#500084d3")).visible(false));

            issPast = new CircleOptions()
                    .center(new LatLng(0,0))
                    .radius(5000)
                    .strokeWidth(2)
                    .strokeColor(Color.RED)
                    .fillColor(Color.parseColor("#ff0000"))
                    .visible(false);
        }
        catch (NullPointerException NPE){
            Toast.makeText(context, NPE.getMessage(), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMap();
    }

    public static void updateIssLocation(final Location issLocation) {
        //get LatLong
        LatLng latLng = new LatLng(issLocation.getLatitude(), issLocation.getLongitude());

        issMarker.setPosition(latLng);
        issMarker.setTitle("ISS is above here...");
        issMarker.setVisible(true);

        issRadius.setCenter(latLng);
        if(!issRadius.isVisible()) {
            issRadius.setVisible(true);
        }

        if(!issPast.isVisible()) {
            issPast.visible(true);
        }
        issPast.center(latLng);
        mMap.addCircle(issPast);
        //Zoom map
        if(firstTime) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
            firstTime = false;
        }
    }

    private void getCurrentLocation() {
        //Initialize task location
        Task<Location> task = flpClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                //When task succeeded
                if(location != null){
                    //Sync map
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            //get LatLong
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                            //create marker
                            myMarker.setPosition(latLng);
                            myMarker.setTitle("You are here...");
                            myMarker.setVisible(true);
                            //Zoom map
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
                        }
                    });
                }
            }
        });
    }

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        timer = new Timer();
        doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            FetchIssLocation performBackgroundTask = new FetchIssLocation(context);
                            // PerformBackgroundTask this class is the class that extends AsynchTask
                            performBackgroundTask.execute();
                        } catch (Exception e) {
                            Toast.makeText(context, e.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 10000); //execute in every 10000 ms
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CHECK_SETTINGS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //When permission granted
                    //call method
                    getCurrentLocation();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("lifeCycle","onResume");

            try{
                //int YOUR_FRAGMENT_POSITION = 0; // in your case you wanna access the 3rd fragment, 0 indexed
                mMap.clear();
                updateMap();
                getCurrentLocation();
                trackIss.setVisibility(View.VISIBLE);
                stopIssTrack.setVisibility(View.INVISIBLE);
            }
            catch (NullPointerException npe){
                Toast.makeText(context, npe.getMessage(), Toast.LENGTH_LONG).show();
            }

    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("lifeCycle","onStop");
        //if(fetchIssLocation.getStatus().toString().equals("RUNNING") || fetchIssLocation.getStatus().toString().equals("PENDING")){
            try{
                issMarker.remove();
                fetchIssLocation.cancel(true);
                doAsynchronousTask.cancel();
                timer.cancel();
                //openFragment(this.getChildFragmentManager().findFragmentById(R.id.notify_fragment));
                //openFragment(mapFragment);
                //ft.replace(R.id.map, mapFragment);
                //ft.commit();
            }
            catch (NullPointerException npe){
                Toast.makeText(context, npe.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("already","NullPE");

            }
            catch (IllegalStateException ise){
                Toast.makeText(context, ise.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("already", "IllegalSE");

            }
        //}
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.isInfoWindowShown()){
            marker.hideInfoWindow();
        }
        else{
            marker.showInfoWindow();
        }
        return false;
    }
}

class FetchIssLocation extends AsyncTask<URL, Void, JSONObject> {
    //public Location issLocation = new Location("issLocation");

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private Location issLocation = new Location("issLocation");
    private String lat, lon;
    private ProgressDialog pd;

//save the context recievied via constructor in a local variable

    FetchIssLocation(Context context){
        this.context=context;
    }

    @Override
    protected JSONObject doInBackground(URL... urls) {

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://api.open-notify.org/iss-now.json";
        //mainActivity = new MainActivity();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject issPosition = response.getJSONObject("iss_position");

                            issLocation.setLatitude(issPosition.getDouble("latitude"));
                            issLocation.setLongitude(issPosition.getDouble("longitude"));

                            lat = issPosition.getString("latitude");
                            lon = issPosition.getString("longitude");

                            Toast.makeText(context, "latitude " + lat + "\nlongitude " + lon, Toast.LENGTH_LONG).show();

                            //fragmentTrack.updateIssLocation(issLocation);
                            FragmentTrack.updateIssLocation(issLocation);


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

        // Add the request to the RequestQueue.
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

