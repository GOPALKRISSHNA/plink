package com.example.windows.plink;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class RegisterActivity extends AppCompatActivity {

    EditText txtname, txtmob, txtpass, txtemail,txtpassconfirm;
    String name, mob, pass, email, passConfirm;
    String ip;
    ProgressDialog progressDialog;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //hides the keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //ipc mechanism get data
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            ip = extras.getString("ip");
        }

        txtname =  findViewById(R.id.txtname);
        txtmob = findViewById(R.id.txtmobile);
        txtemail = findViewById(R.id.txtemail);
        txtpass = findViewById(R.id.txtpassword);
        txtpassconfirm = findViewById(R.id.txtconfirmpassword);

        //create progress dilaog
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setTitle("Register");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
    }

    //peform register button operations
    public void reg_click(View view) {
        name = txtname.getText().toString().trim();
        mob = txtmob.getText().toString().trim();
        pass = txtpass.getText().toString().trim();
        email = txtemail.getText().toString().trim();
        passConfirm = txtpassconfirm.getText().toString().trim();

        if (name.equals("") || email.equals("") || mob.equals("") || pass.equals("") || passConfirm.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter all the fileds", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!email.matches(emailPattern) || email.length() == 0){
            Toast.makeText(getApplicationContext(), "Please enter valid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mob.length() != 10) {
            Toast.makeText(getApplicationContext(), "Mobile No should be 10 Digits only", Toast.LENGTH_SHORT).show();
            return;
        }

        if(pass.equals(passConfirm)) {
            String URL = ip + "/location/register.php?name=" + name + "&mobile=" + mob + "&pass=" + pass + "&email=" + email;
            URL = URL.trim();
            System.out.println(URL);

            //Toast.makeText(getApplicationContext(), URL, Toast.LENGTH_SHORT).show();
            GetXMLTask task = new GetXMLTask();
            task.execute(new String[]{URL});
        }else{
            Toast.makeText(RegisterActivity.this, "Passwords are not matching", Toast.LENGTH_SHORT).show();
        }

    }

//network operations
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
                progressDialog.dismiss();
            }
            return output;
        }

        @Override
        protected void onPostExecute(String output) {
            //outputText.setText(output);

            if(output != null) {
                Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
                if (output.equals("Registration Successful")) {
                    SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putBoolean(SharedPref.LOGGED_IN, true);
                    editor.putString(SharedPref.MOB_NO, mob);
                    editor.putString(SharedPref.IP, ip);
                    editor.commit();

                    Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                    intent.putExtra("ip", ip);
                    intent.putExtra("mob", mob);
                    startActivity(intent);
                    finish();
                }
            }else     Toast.makeText(getApplicationContext(), "Please try again...", Toast.LENGTH_LONG).show();

        }
    }
}
