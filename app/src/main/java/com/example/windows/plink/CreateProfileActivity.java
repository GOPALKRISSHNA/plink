package com.example.windows.plink;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class CreateProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, LocationListener {

    EditText editName, editLongitude, editLatitude;
    String profile, longitude, latitude, pname, fetch;
    Spinner profileList;
    String ip, mob;
    CheckBox fetchBox;
    GPSTracker gps;
    ProgressDialog progressDialog;
    LocationManager LC;
    boolean isGPS;
    Criteria criteria;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //hides automatic opening of keyboad
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);

        LC = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();

        //getting values from sharedpreferences
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        ip = sharedPreferences.getString(SharedPref.IP, null);
        mob = sharedPreferences.getString(SharedPref.MOB_NO, null);

        editName = (EditText) findViewById(R.id.pname);
        editLongitude = (EditText) findViewById(R.id.longitude);
        editLatitude = (EditText) findViewById(R.id.latitude);

        //creating drop down menu
        profileList = (Spinner) findViewById(R.id.spinnerProfile);
        List<String> categories = new ArrayList<>();
        categories.add("Normal");
        categories.add("Silent");
        categories.add("Vibrate");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        profileList.setAdapter(dataAdapter);
        profileList.setOnItemSelectedListener(this);

        //checkbox to get location from gps and set it to editText
        fetchBox = (CheckBox) findViewById(R.id.fetch);
        fetchBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                                                @Override
                                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                                                    if (ActivityCompat.checkSelfPermission(CreateProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CreateProfileActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                        // TODO: Consider calling
                                                        //    ActivityCompat#requestPermissions
                                                        // here to request the missing permissions, and then overriding
                                                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                        //                                          int[] grantResults)
                                                        // to handle the case where the user grants the permission. See the documentation
                                                        // for ActivityCompat#requestPermissions for more details.
                                                        return;
                                                    }
                                                    Location getLastLocation = LC.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                                                    isGPS = LC.isProviderEnabled(LocationManager.GPS_PROVIDER);
                                                    gps = new GPSTracker(CreateProfileActivity.this);
                                                    if (ActivityCompat.checkSelfPermission(CreateProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(CreateProfileActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                        return;
                                                    }
//                LC.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, CreateProfileActivity.this);
//                String provider = LC.getBestProvider(criteria, false);
//                Location loc = LC.getLastKnownLocation(provider);

                                                    if (fetchBox.isChecked() == true) {
//                                                        if (!isGPS) {
//                                                            gps.showSettingsAlert();
//                                                        }

                                                        try {
                                                            latitude = String.valueOf(getLastLocation.getLatitude()).substring(0, 5);
                                                            longitude = String.valueOf(getLastLocation.getLongitude()).substring(0, 5);
                                                            editLatitude.setText(latitude);
                                                            editLongitude.setText(longitude);
                                                        } catch (StringIndexOutOfBoundsException s) {
                                                            System.out.print("array index out of bound:" + s);
                                                        } catch (NullPointerException n) {
//                                                            gps.showSettingsAlert();
                                                        }
                                                    }
                                                }
                                            }
        );

        //creating progress dialog while doing network calls which will called later
        progressDialog = new ProgressDialog(CreateProfileActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Creating Profile");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(true);
    }

    //whenever location get changed it will update the location values
    @Override
    public void onLocationChanged(Location location) {

        //if checkbox is checked it will get location and set the values
        if (fetchBox.isChecked() == true) {
            location.getLatitude();
            location.getLongitude();
            double lt = (double) (location.getLatitude());
            double ln = (double) (location.getLongitude());
            editLatitude.setText(String.valueOf(lt).substring(0, 6));
            editLongitude.setText(String.valueOf(ln).substring(0, 6));
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {
//        Toast.makeText(this, "Enabled Provider: "+ provider, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        gps.showSettingsAlert();
    }

    //it will construct url and call asynchronous network task to create profile
    public void createProfile(View view) {
        pname = editName.getText().toString().trim();
        longitude = editLongitude.getText().toString().trim();
        latitude = editLatitude.getText().toString().trim();

        if (pname.equals("") || longitude.equals("") || latitude.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter all the fileds", Toast.LENGTH_SHORT).show();
            return;
        }

        String URL = ip + "/location/createprofile.php?name=" + pname + "&profile=" + profile + "&latitude=" + latitude + "&longitude=" + longitude + "&mob=" + mob;
        URL = URL.trim();
        System.out.println(URL);

        //Toast.makeText(getApplicationContext(), URL, Toast.LENGTH_SHORT).show();
        GetXMLTask task = new GetXMLTask();
        task.execute(new String[]{URL});
//        Toast.makeText(CreateProfileActivity.this, "Passwords are not matching", Toast.LENGTH_SHORT).show();
    }

    // it will read the item selected in drop down menu (spinner)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        parent.getItemAtPosition(position);
        switch (parent.getId()) {
            case R.id.spinnerProfile:
                profile = parent.getItemAtPosition(position).toString();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    //perform network call and reads the response
    private class GetXMLTask extends AsyncTask<String, Void, String> {

        //performed before network call starts
        //will show progress dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        //start network operation
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
                url = url.replaceAll(" ", "%20");
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                output = EntityUtils.toString(httpEntity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return output;
        }

        //it will read the response
        @Override
        protected void onPostExecute(String output) {
            //outputText.setText(output);
            progressDialog.dismiss();
            if (output != null) {
                Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
                if (output.equals("Profile created successfully")) {
                    Intent intent = new Intent(CreateProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            } else
                Toast.makeText(getApplicationContext(), "Please try again...", Toast.LENGTH_LONG).show();

        }
    }
}