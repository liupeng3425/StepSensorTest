package edu.uestc.peng.stepsensortest;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView textViewCounter, textViewDetector;
    private int mStepCount = 0;
    private int mPreviousStep = 0;
    private SensorEventListener sensorEventListener;
    private int maxDelay = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCounter = (TextView) findViewById(R.id.tvCounter);
        textViewDetector = (TextView) findViewById(R.id.tvDetector);

        SensorManager sensorManager = (SensorManager) this.getSystemService(Activity.SENSOR_SERVICE);

        final Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                    if (mPreviousStep == 0) {
                        mPreviousStep = (int) event.values[0];
                    }
                    mStepCount = (int) (event.values[0]) - mPreviousStep;
                    textViewCounter.setText(String.format("%d", mStepCount));
                } else if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {
                    textViewDetector.setText(event.values.toString());
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        sensorManager.registerListener(sensorEventListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL, maxDelay);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SensorManager sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        sensorManager.unregisterListener(sensorEventListener);
    }
}
