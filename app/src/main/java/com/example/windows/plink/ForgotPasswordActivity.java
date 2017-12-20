package com.example.windows.plink;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class ForgotPasswordActivity extends AppCompatActivity {

    EditText txtmob;
    String ip, mob;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        //ipc mechanism getting data from login activity
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ip = extras.getString("ip");
        }

        txtmob = (EditText) findViewById(R.id.fmobile);

        //creating progress dialog
        progressDialog = new ProgressDialog(ForgotPasswordActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Forgot Password");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
    }

    //called when button pressed
    //construct the url and call network task
    public void forget_pass(View view) {
        mob = txtmob.getText().toString().trim();

        if (mob.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter registered mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        String URL = ip + "/location/getpass.php?mobile=" + mob;
        URL = URL.trim();
        System.out.println(URL);

        GetXMLTask task = new GetXMLTask();
        task.execute(new String[]{URL});
    }

    //network task performed
    private class GetXMLTask extends AsyncTask<String, Void, String> {

        //show progress dialog
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        //perform network operations in background
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
            }
            return output;
        }


        //read the response
        @Override
        protected void onPostExecute(String output) {
            progressDialog.dismiss();
            if (output != null) {
                output = output.trim();
                Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(getApplicationContext(), "Please try again...", Toast.LENGTH_LONG).show();
        }
    }
}