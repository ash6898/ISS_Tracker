package com.example.isstracker;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.ImageView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FragmentInfo extends Fragment {

    @SuppressLint("StaticFieldLeak")
    static Context context;
    View view;
    static ArrayList<String> originalNameList = new ArrayList<>();
    static Map<String, String> desc = new HashMap<>();
    static Map<String, String> imageURL = new HashMap<>();
    static ArrayList<String> descList = new ArrayList<>();
    static ArrayList<String> imageList = new ArrayList<>();
    static int peopleCount;
    static int counter = 1;
    boolean firstOpen = true;
    CardView issCard;

    FetchPeopleName fetchPeopleName;
    @SuppressLint("StaticFieldLeak")
    static FetchPeopleDescription fetchPeopleDescription;
    @SuppressLint("StaticFieldLeak")
    static FetchPeopleImageURL fetchPeopleImageURL;

    @SuppressLint("StaticFieldLeak")
    static RecyclerViewAdapter recyclerViewAdapter;

    @SuppressLint("StaticFieldLeak")
    static RecyclerView recyclerView;
    ImageView noInternet;
    ImageView issImage;
    TextView issDesc;

    /*
    @Override
    public void onStop() {
        super.onStop();
        Log.d("lifecyclee", "onStop");
        try{
            //recyclerViewAdapter.clear();
        }
        catch (NullPointerException npe){
            Toast.makeText(context, npe.getMessage(), Toast.LENGTH_LONG).show();
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentInfo.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.info_fragment, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        noInternet = view.findViewById(R.id.no_internet_img);

        issCard = view.findViewById(R.id.issInfo);
        issImage = view.findViewById(R.id.iss_image);
        issDesc = view.findViewById(R.id.iss_description);

        issCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TransitionManager.beginDelayedTransition(issCard);
                if(issImage.getVisibility() == View.GONE) {
                    issImage.setVisibility(View.VISIBLE);
                    issDesc.setVisibility(View.VISIBLE);
                }
                else{
                    issImage.setVisibility(View.GONE);
                    issDesc.setVisibility(View.GONE);
                }
            }
        });

        /*originalNameList.add("International Space Station\n(ISS)");
        descList.add(getResources().getString(R.string.iss_description));
        imageList.add(getResources().getString(R.string.iss_url));*/

        try{
            if(recyclerViewAdapter.getItemCount() > 0) {
                recyclerView.setAdapter(recyclerViewAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                return view;
            }
        }
        catch (NullPointerException npe){
            Log.d("catch","npe");
        }

            if(isNetworkConnected() || isInternetAvailable()){
                fetchPeopleName = new FetchPeopleName(context);
                fetchPeopleName.execute();
            }
            else{
                recyclerView.setVisibility(View.INVISIBLE);
                noInternet.setVisibility(View.VISIBLE);
            }
            firstOpen = false;
        return view;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
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

    public static void updateNameList(ArrayList<String> names) {
        peopleCount = names.size();
        String[] nameArray = names.toArray(new String[0]);
        /*nameList.set(0, nameList.get(0).substring(0, 1).toUpperCase() + nameList.get(0).substring(1));
        nameList.set(1, nameList.get(1).substring(0, 1).toUpperCase() + nameList.get(1).substring(1));
        nameList.set(2, nameList.get(2).substring(0, 1).toUpperCase() + nameList.get(2).substring(1));*/

        fetchPeopleDescription = new FetchPeopleDescription(context);
        fetchPeopleDescription.execute(nameArray);
    }

    public static void updateDescription(String key, String description) {
        desc.put(key, description);

        if(counter == peopleCount){

            Collections.sort(originalNameList, String.CASE_INSENSITIVE_ORDER);

            for(String s : originalNameList) {
                descList.add(desc.get(s));
            }

            fetchPeopleImageURL = new FetchPeopleImageURL(context);
            String[] nameArray = originalNameList.toArray(new String[0]);
            fetchPeopleImageURL.execute(nameArray);

            counter = 1;

        }
        else{
            counter += 1;
        }
    }


    public static void updateImageURL(String key, String value) {
        imageURL.put(key, value);
        if(counter == peopleCount){
            for(String s : originalNameList){
                imageList.add(imageURL.get(s));
            }
            recyclerViewAdapter = new RecyclerViewAdapter(context, originalNameList, descList, imageList);
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            counter = 1;
        }
        else{
            counter += 1;
        }
    }

    public static void updateOriginalNameList(String originalName){
        originalNameList.add(originalName);
    }
}

class FetchPeopleName extends AsyncTask<String, Void, JSONObject> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private int numberOfPeople;

    private ArrayList<String> names = new ArrayList<>();

    FetchPeopleName(Context context) {
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://api.open-notify.org/astros.json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            numberOfPeople = response.getInt("number");
                            JSONArray people = response.getJSONArray("people");
                            for (int index = 0; index < numberOfPeople; index++) {
                                JSONObject ind = people.getJSONObject(index);
                                names.add(ind.getString("name"));
                            }
                            FragmentInfo.updateNameList(names);

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
}

class FetchPeopleDescription extends AsyncTask<String, Void, JSONObject> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private String originalName;
    FetchPeopleDescription(Context context) {
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(String... urls) {
        for (String s: urls){

            RequestQueue queue = Volley.newRequestQueue(context);

            final String name = s;
            final String url = "https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&titles=" + name;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject queryObject = response.getJSONObject("query");
                                JSONObject pagesObject = queryObject.getJSONObject("pages");
                                if(queryObject.has("redirects")){
                                    JSONArray redirectArray = queryObject.getJSONArray("redirects");
                                    JSONObject redirectObject = redirectArray.getJSONObject(0);
                                    originalName = redirectObject.getString("to");
                                    FragmentInfo.updateOriginalNameList(originalName);
                                }
                                else {
                                    originalName = name;
                                    FragmentInfo.updateOriginalNameList(name);
                                }
                                Iterator iteratorObj = pagesObject.keys();
                                String pageIdString = (String) iteratorObj.next();
                                JSONObject pageId = pagesObject.getJSONObject(pageIdString);
                                String description = pageId.getString("extract");

                                FragmentInfo.updateDescription(originalName, description);
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
        }
        return null;
    }
}

class FetchPeopleImageURL extends AsyncTask<String, Void, JSONObject> {

    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ProgressDialog pd;

    FetchPeopleImageURL(Context context) {
        this.context = context;
    }

    protected void onPreExecute() {
        super.onPreExecute();

        pd = new ProgressDialog(context);
        pd.setMessage("Loading Information...");
        pd.setCancelable(false);
        pd.show();
    }

    @Override
    protected JSONObject doInBackground(String... urls) {

        for (String s : urls) {
            RequestQueue queue = Volley.newRequestQueue(context);

            final String url = "https://en.wikipedia.org/w/api.php?action=query&titles=" + s + "&prop=pageimages&format=json&pithumbsize=300";

            final String key = s;

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONObject queryObject = response.getJSONObject("query");
                                JSONObject pagesObject = queryObject.getJSONObject("pages");
                                Iterator iteratorObj = pagesObject.keys();
                                String pageIdString = (String) iteratorObj.next();
                                JSONObject pageId = pagesObject.getJSONObject(pageIdString);
                                JSONObject thumbnailObject = pageId.getJSONObject("thumbnail");
                                String imageUrl = (String) thumbnailObject.get("source");
                                FragmentInfo.updateImageURL(key, imageUrl);
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
        }
        return null;
}

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        if (pd.isShowing()){
            pd.dismiss();
        }
    }
}