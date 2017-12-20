package com.example.windows.plink;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity {

    String ip, mob, pass;
    Intent intent = null;
    private static final String TAG = "ServicesDemo";
    NotificationManager mNotificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
//removing title bar and setting activity fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //getting values
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        ip = sharedPreferences.getString(SharedPref.IP, null);
        mob = sharedPreferences.getString(SharedPref.MOB_NO, null);

        LinearLayout mLinear = findViewById(R.id.btn_stop_service);
        mLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop_click();
            }
        });

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    //button click for checking permission or if service is already running
    public void start_click(View view) {

        //if permissions are not given then asks for permission
        //if permissions are already given starts the service
        NotificationManager n = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(n.isNotificationPolicyAccessGranted()) {
                start_service();
            }else{
                // Ask the user to grant access
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }
        }else {
            start_service();
        }
    }

    //function to start the service
    public void start_service(){
        if(isMyServiceRunning(MyService.class)) {
            Toast.makeText(HomeActivity.this, "Service Running", Toast.LENGTH_SHORT).show();
        }else {

            Toast.makeText(this, "Starting Service", Toast.LENGTH_LONG).show();

//            Toast.makeText(this, ip + "," + mob, Toast.LENGTH_LONG).show();
            Log.d(TAG, "onClick: starting service");

            intent = new Intent(getApplicationContext(), MyService.class);
            intent.putExtra("ip", ip);
            intent.putExtra("mobile", mob);
            startService(intent);
        }
    }

    //checks if service is already running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    //function to stop running service
    public void stop_click() {
        Toast.makeText(HomeActivity.this, "Plink Service Stopped", Toast.LENGTH_LONG).show();

        try {
            stopService(new Intent(HomeActivity.this, MyService.class));
            Log.d(TAG, "onDestroy");
            stopService(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //button function to open createProfile activity
    public void createNew(View view) {
        Intent intent = new Intent(HomeActivity.this, CreateProfileActivity.class);
        startActivity(intent);
    }

    //button function to open profile list activity
    public void profileList(View view) {
        Intent intent = new Intent(HomeActivity.this, ProfileListActivity.class);
        startActivity(intent);
    }

    //logout function, clears all stored values and goto login activity
    public void logout(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();
        stop_click();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
        } else {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        }

        Toast.makeText(HomeActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    //backpress listner, asks if user really want to exit
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        HomeActivity.this.finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
