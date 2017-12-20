package com.example.windows.plink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.lcodecore.tkrefreshlayout.RefreshListenerAdapter;
import com.lcodecore.tkrefreshlayout.TwinklingRefreshLayout;
import com.lcodecore.tkrefreshlayout.footer.LoadingView;
import com.lcodecore.tkrefreshlayout.header.SinaRefreshView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ProfileListActivity extends AppCompatActivity {

    List<Contact> profileDataAdapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager recyclerViewlayoutManager;
    RecyclerView.Adapter recyclerViewadapter;
    Context mcontext;
    JSONArray responseJson;
    JsonArrayRequest jsonArrayRequest;
    String URL, ip, mob;
    RequestQueue requestQueue;
    private ProgressDialog progressDialog;
    private ProgressDialog mProgressDialog;
    private Context context;
    private List<Integer> jsonData;
    AlertDialog alertD;
    String updateUrl, deleteUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_list);

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        ip = sharedPreferences.getString(SharedPref.IP, null);
        mob = sharedPreferences.getString(SharedPref.MOB_NO, null);

        URL = ip + "/location/profilesList.php?mobile=" + mob;
        jsonData = new ArrayList<>();

        profileDataAdapter = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.profileRecyclerview);
        recyclerViewlayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(recyclerViewlayoutManager);

        TwinklingRefreshLayout refreshLayoutHome = findViewById(R.id.refreshProfile);
        SinaRefreshView headerView = new SinaRefreshView(ProfileListActivity.this);
        headerView.setPullDownStr("Pull down to refresh");
        headerView.setRefreshingStr("Refreshing");
        headerView.setReleaseRefreshStr("Release to refresh");
        headerView.setArrowResource(R.drawable.arrow);
        headerView.setTextColor(getResources().getColor(R.color.colorAccent));
        refreshLayoutHome.setHeaderView(headerView);


        LoadingView loadingView = new LoadingView(ProfileListActivity.this);
        refreshLayoutHome.setBottomView(loadingView);

        refreshLayoutHome.setOnRefreshListener(new RefreshListenerAdapter() {
            @Override
            public void onRefresh(final TwinklingRefreshLayout refreshLayoutHome) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
//                            recyclerView.removeAllViewsInLayout();
                            profileDataAdapter.clear();
                            profileDataAdapter.removeAll(null);
                            recyclerViewadapter.notifyDataSetChanged();
                            (new getProfile()).execute();
                            refreshLayoutHome.finishRefreshing();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore(final TwinklingRefreshLayout refreshLayoutHome) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            refreshLayoutHome.finishLoadmore();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 2000);
            }
        });

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

//                        openNewsDetails(position);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        showMyDialog(position);
                    }
                })
        );

        progressDialog = new ProgressDialog(ProfileListActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Fetching Profiles");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        (new getProfile()).execute();
    }

    public void showMyDialog(int pos) {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.prompt, null);

        alertD = new AlertDialog.Builder(this).create();

        final EditText pname = (EditText) promptView.findViewById(R.id.pname);
        final EditText elatitude = (EditText) promptView.findViewById(R.id.latitude);
        final EditText elongitude = (EditText) promptView.findViewById(R.id.longitude);

        Button btnUpdate = (Button) promptView.findViewById(R.id.updateBtn);
        Button btnDelete = (Button) promptView.findViewById(R.id.deleteBtn);

        Spinner profiles = (Spinner) promptView.findViewById(R.id.spinnerProfile);
        List<String> categories = new ArrayList<>();
        categories.add("Normal");
        categories.add("Silent");
        categories.add("Vibrate");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profiles.setAdapter(dataAdapter);

        final String[] latitude = new String[1];
        final String[] longitude = new String[1];
        String profileName;
        final String[] place = new String[1];
        String id = null;
        final String[] profile = {""};

        AdapterView.OnItemSelectedListener myClickListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                parent.getItemAtPosition(position);
                switch (parent.getId()) {
                    case R.id.spinnerProfile:
                        profile[0] = parent.getItemAtPosition(position).toString().trim();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        profiles.setOnItemSelectedListener(myClickListener);

        try {
            JSONObject json = responseJson.getJSONObject(jsonData.get(pos));
            place[0] = json.getString("place");
            profileName = json.getString("profile");
            latitude[0] = json.getString("lat");
            longitude[0] = json.getString("lng");
            id = json.getString("id");

            pname.setText(place[0]);
            elatitude.setText(latitude[0]);
            elongitude.setText(longitude[0]);

            if (profileName.equals(categories.get(0)))
                profiles.setSelection(0);
            else if (profileName.equals(categories.get(1)))
                profiles.setSelection(1);
            else if (profileName.equals(categories.get(2)))
                profiles.setSelection(2);


//                updateUrl = ip + "/location/updateProfile.php?id=" + id + "&name=" + place + "&latitude=" + latitude + "&longitude=" + longitude + "&mob=" + mob;
//                deleteUrl = ip + "/location/deleteProfile.php?id=" + id + "&mob=" + mob;

            System.out.println(updateUrl);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String finalId = id;
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                place[0] = pname.getText().toString().trim();
                latitude[0] = elatitude.getText().toString().trim();
                longitude[0] = elongitude.getText().toString().trim();

                updateUrl = ip + "/location/updateProfile.php?id=" + finalId + "&name=" + place[0] + "&latitude=" + latitude[0] + "&longitude=" + longitude[0] + "&mob=" + mob + "&profile=" + profile[0];

                (new updateProfile()).execute(new String[]{updateUrl.toString().trim()});
            }
        });

        final String finalId1 = id;
        btnDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteUrl = ip + "/location/deleteProfile.php?id=" + finalId1;
                (new updateProfile()).execute(new String[]{deleteUrl});
            }
        });

        alertD.setView(promptView);
        alertD.show();
    }

    private class updateProfile extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            String output = null;
            for (String url : urls) {
                output = getOutputFromUrl(url);
            }
            return output;
        }

        private String getOutputFromUrl(String url) {
            String output = null;
            try {
                if (url != null) {
                    url = url.replaceAll(" ", "%20");
                    DefaultHttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet(url);
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    HttpEntity httpEntity = httpResponse.getEntity();
                    output = EntityUtils.toString(httpEntity);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
//                progressDialog.dismiss();
//                alertD.hide();
            }
            return output;
        }

        @Override
        protected void onPostExecute(String output) {

            if (output != null) {
                Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
                alertD.dismiss();
                profileDataAdapter.clear();
                profileDataAdapter.removeAll(null);
                recyclerViewadapter.notifyDataSetChanged();
                (new getProfile()).execute();
            } else
                Toast.makeText(getApplicationContext(), "Please try again...", Toast.LENGTH_LONG).show();
        }
    }


    public class getProfile extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            JSON_DATA_WEB_CALL();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    public void JSON_DATA_WEB_CALL() {
//        mProgressDialog.show();
        jsonArrayRequest = new JsonArrayRequest(URL,

                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {

                        JSON_PARSE_DATA_AFTER_WEBCALL(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                    }
                });

        requestQueue = Volley.newRequestQueue(this);

        requestQueue.add(jsonArrayRequest);
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array) {

        responseJson = array;

        for (int i = 0; i < array.length(); i++) {

            Contact GetDataAdapter2 = new Contact();
            JSONObject json = null;
            try {
                json = array.getJSONObject(i);
                GetDataAdapter2.setName(json.getString("place"));
                GetDataAdapter2.setProfile(json.getString("profile"));
                jsonData.add(i);
//                jsonData.add(json.getInt("id"));
            } catch (JSONException e) {
                progressDialog.dismiss();
                e.printStackTrace();
            }
            profileDataAdapter.add(GetDataAdapter2);
        }

        recyclerViewadapter = new ProfileRecyclerAdapter(profileDataAdapter, this);
        recyclerView.setAdapter(recyclerViewadapter);
        progressDialog.dismiss();
    }
}
