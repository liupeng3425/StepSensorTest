package edu.uestc.peng.stepsensortest;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView textViewCounter;
    private BroadcastReceiver broadcastReceiver;
    private String TAG = "MainActivity";
    private IntentFilter intentFilter;

    private LinearLayout linearLayoutChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "onCreate");

        textViewCounter = (TextView) findViewById(R.id.tvCounter);

        linearLayoutChart = (LinearLayout) findViewById(R.id.linearLayoutChart);

        linearLayoutChart.addView(getGraphicalView(),
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT);


        intentFilter = new IntentFilter(Constants.ACTION_UPDATE_STEPS);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                textViewCounter.setText(intent.getIntExtra("mStepCount", 0) + "");
                Log.e(TAG, "onReceive");
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
        Intent intent = new Intent(MainActivity.this, StepCountService.class);
        startService(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private GraphicalView getGraphicalView() {
        TimeSeries series = new TimeSeries(Constants.XY_SERIES);
        XYMultipleSeriesDataset seriesDataset = new XYMultipleSeriesDataset();


        for (int i = 7; i > 0; i--) {
            Date date = new Date(new Date().getTime() - TimeChart.DAY * i);
            series.add(date, getStep(getDateString(date)));
        }

        seriesDataset.addSeries(series);

        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

        XYSeriesRenderer simpleSeriesRenderer;
        simpleSeriesRenderer = new XYSeriesRenderer();
        simpleSeriesRenderer.setColor(Color.CYAN);
        simpleSeriesRenderer.setLineWidth(10);
        simpleSeriesRenderer.setDisplayChartValues(true);
        simpleSeriesRenderer.setChartValuesTextSize(50);
        renderer.addSeriesRenderer(simpleSeriesRenderer);
        renderer.setMarginsColor(Color.WHITE);
        renderer.setShowLegend(false);
        renderer.setLabelsTextSize(20);
        renderer.setPanEnabled(true, false);
        renderer.setZoomEnabled(false);
        return ChartFactory.getTimeChartView(this, seriesDataset, renderer,"yyyy年MM月dd日");
    }

    private int getStep(String date) {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.APP_NAME, MODE_PRIVATE);
        return sharedPreferences.getInt(date, 0);
    }

    private String getDateString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(date);
    }
}
