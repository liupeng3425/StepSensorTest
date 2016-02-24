package edu.uestc.peng.stepsensortest;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class StepCountService extends Service {

    private int mStepCount = 0;
//    private int mPreviousStep = 0;
    private SensorEventListener sensorEventListener;
    private int maxDelay = 1000;
    private String TAG = "StepCountService";
    private Sensor stepSensor;
    private SensorManager sensorManager;
    private BroadcastReceiver broadcastReceiver;

    public StepCountService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate");

        sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);

        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                    mStepCount += event.values.length;
                    SharedPreferences sharedPreferences =
                            StepCountService.this.getSharedPreferences(Constants.APP_NAME, MODE_APPEND);
                    sharedPreferences.edit().putInt(getDateString(), mStepCount)
                            .apply();
                    sendBroadcast();
                }
            }

//        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

//        sensorEventListener = new SensorEventListener() {
//            @Override
//            public void onSensorChanged(SensorEvent event) {
//                if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
//                    if (mPreviousStep == 0) {
//                        mPreviousStep = (int) event.values[0];
//                        SharedPreferences sharedPreferences =
//                                StepCountService.this.getSharedPreferences(Constants.APP_NAME, MODE_APPEND);
//                        sharedPreferences.edit()
//                                .putInt(getDateString(), mPreviousStep)
//                                .apply();
//
//                        Log.e(TAG, "" + sharedPreferences.getInt(getDateString(), 0) + getDateString());
//
//                    }
//                    mStepCount = (int) (event.values[0]) - mPreviousStep;
//                    sendBroadcast();
//                }
//            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        SharedPreferences sharedPreferences = StepCountService.this.getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);
        mStepCount = sharedPreferences.getInt(getDateString(), 0);

        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_NEW_DAY);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mStepCount = 0;
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);

        scheduleServiceRestart();
    }

    private void scheduleServiceRestart() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DATE, 1);

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(this, 0, new Intent(Constants.ACTION_NEW_DAY), 0);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), 24 * 3600 * 1000, pendingIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        SharedPreferences sharedPreferences =
//              StepCountService.this.getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);
//        mStepCount = sharedPreferences.getInt(getDateString(), 0);
//        mPreviousStep = sharedPreferences.getInt(getDateString(), 0);
        Log.e(TAG, "onStartCommand " + mStepCount);

        sensorManager.registerListener(sensorEventListener, stepSensor,
                SensorManager.SENSOR_DELAY_NORMAL, maxDelay);

        sendBroadcast();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        sensorManager.unregisterListener(sensorEventListener);
        unregisterReceiver(broadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendBroadcast() {
        Intent intent = new Intent(Constants.ACTION_UPDATE_STEPS);
        intent.putExtra("mStepCount", mStepCount);
        sendBroadcast(intent);
        Log.e(TAG, "sendBroadCast");
    }

    private String getDateString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(new Date());
    }
}
