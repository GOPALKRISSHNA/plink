package com.example.windows.plink;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


@SuppressWarnings("ALL")
public class MyService extends Service {

    String ip, mobile;
    Boolean flag = false;
    private static final String TAG = "PlinkService";
    NotificationManager mNotificationManager;
    GPSTracker gps;
    NotificationCompat.Builder builder;
    NotificationManager nManager;
    int NOTIFICATION_ID = 12345;
    Intent targetIntent;
    PendingIntent contentIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Plink Service Created", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onCreate");

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        gps = new GPSTracker(getApplicationContext());
        showNotification();

    }

    private void showNotification() {
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Plink App ")
                .setContentText("Service is running")
                .setAutoCancel(false);

        targetIntent = new Intent(this, HomeActivity.class);
        contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }

//    private RemoteViews getNotificationContent() {
//
//        RemoteViews notificationContent = new RemoteViews(getPackageName(), R.layout.keyphrase_recogniser_notification);
//        notificationContent.setTextViewText(R.id.notification_title, "title");
//        notificationContent.setTextViewText(R.id.notification_subtitle, "subtitle");
//        return notificationContent;
//    }

    @Override
    public void onDestroy() {
//        Toast.makeText(getApplicationContext(), "Plink Service Stopped", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy");
        flag = true;
//        mNotificationManager.cancel(NOTIFICATION_ID);

    }


    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startid) {

//        Toast.makeText(getApplicationContext(), "Started", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onStart");

        SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        ip = sharedPreferences.getString(SharedPref.IP, null);
        mobile = sharedPreferences.getString(SharedPref.MOB_NO, null);

        Runnable r = new Runnable() {
            public void run() {

                try {

                    while (true) {



                        //if flag value is true it will stop the service
                        if (flag) {
                            //updating the notification content
                            builder.setContentText("Service stopped");
                            nManager.notify(NOTIFICATION_ID, builder.build());
                            return;
                        }
//                                    if (gps.canGetLocation()) {

                            double latitude = gps.getLatitude();
                            double longitude = gps.getLongitude();

                            String lat = String.valueOf(latitude).substring(0,5);
                            String log = String.valueOf(longitude).substring(0,5);

                            //update notification
                            builder.setContentText("Lat:"+lat+" Log:"+log);
                            nManager.notify(NOTIFICATION_ID, builder.build());
                            String URL = ip + "/location/profile.php?lat=" + latitude + "&lon=" + longitude + "&mobile=" + mobile;

                            //remove whitespaces
                            URL = URL.trim();

                            System.out.println(URL);

                            //Toast.makeText(getApplicationContext(), URL, Toast.LENGTH_SHORT).show();
                            GetXMLTask task = new GetXMLTask();
                            task.execute(new String[]{URL});
//                        }
                        Thread.sleep(10000);
                    }
                } catch (Exception e) {
                    disp(e.toString());
                }
            }
        };

//        Toast.makeText(getApplicationContext(), "Starting Thread", Toast.LENGTH_LONG).show();
        Thread t = new Thread(r);
        t.start();
    }

    public void disp(String str) {
        //Toast.makeText(this, str, Toast.LENGTH_LONG).show();
        System.out.println(str);

    }

    //perform network operations and read the response
    private class GetXMLTask extends AsyncTask<String, Void, String> {
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

        @Override
        protected void onPostExecute(String output) {
            //outputText.setText(output);

            //depending upon the response it will change the profile
            if (output != null) {
                output = output.trim().toLowerCase();
                Toast.makeText(getApplicationContext(), output, Toast.LENGTH_LONG).show();
                if (output.equals("silent")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                    } else {

                        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    }
                } else if (output.equals("vibrate")) {

                    //for newer versions of android from marshmallow need new api call method to put device into vibrate mode
                    //this mode is usually call DO NOT DISTURB mode
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS);
//                    } else {
                        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
//                    }
                } else if (output.equals("normal")) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                    }
                        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

                }
            }
        }
    }
}