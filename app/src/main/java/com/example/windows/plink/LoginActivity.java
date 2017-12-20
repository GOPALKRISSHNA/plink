package com.example.windows.plink;

import android.Manifest;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class LoginActivity extends AppCompatActivity {

    EditText txtmob, txtpass;
    String mob, pass;
    String ip = "http://192.168.43.105";
    ProgressDialog progressDialog;
    private boolean loggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //hides automatic opening of keyboard, hide title bar , make activity fullscreen
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //requests location,storage permissions
        ActivityCompat.requestPermissions(LoginActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_NOTIFICATION_POLICY, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        txtmob = (EditText) findViewById(R.id.editUsername);
        txtpass = (EditText) findViewById(R.id.editPassword);

        //creating progress dialog
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Login");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
    }

//asking for permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(LoginActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    //called when app resumes it will check if values are already available
    //if user already logged in it will store LOGGED_IN value as TRUE
    @Override
    protected void onResume(){
        super.onResume();
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        loggedIn = sharedPreferences.getBoolean(SharedPref.LOGGED_IN, false);
        if (loggedIn) {
            String ipStr,mobStr;
            ipStr = sharedPreferences.getString(SharedPref.IP,null);
            mobStr = sharedPreferences.getString(SharedPref.MOB_NO,null);
            openLogin(ipStr, mobStr);
        }
    }

    //starts the home activity
    public void openLogin(String ip, String mob) {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    //open register activity
    public void register_click(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        intent.putExtra("ip", ip);
        startActivity(intent);
    }

    //get editText values and call network operation task
    public void login_click(View view) {
        mob = txtmob.getText().toString().trim();
        pass = txtpass.getText().toString().trim();

        if (mob.equals("") || pass.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter all the fileds", Toast.LENGTH_SHORT).show();
            return;
        }

        String URL = ip + "/location/login.php?mobile=" + mob + "&pass=" + pass;

        URL = URL.trim();
        System.out.println(URL);

        GetXMLTask task = new GetXMLTask();
        task.execute(new String[]{URL});
    }

    //starts forget password activity
    public void forget_password(View view) {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        intent.putExtra("ip", ip);
        startActivity(intent);
    }

    //perform network operation
    private class GetXMLTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
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
            }finally {
//                progressDialog.dismiss();
            }
            return output;
        }

        @Override
        protected void onPostExecute(String output) {
            //outputText.setText(output);
            progressDialog.dismiss();
//            output = output.trim();

if(output != null) {
    Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
    if (output.equals("Login successful")) {

        //if login successful it will add the user details so that user need not to login again n again
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPref.LOGGED_IN, true);
        editor.putString(SharedPref.MOB_NO, mob);
        editor.putString(SharedPref.IP, ip);
        editor.commit();

        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }
}else     Toast.makeText(getApplicationContext(), "Please try again...", Toast.LENGTH_LONG).show();

        }
    }


    //backpress listner
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        LoginActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
