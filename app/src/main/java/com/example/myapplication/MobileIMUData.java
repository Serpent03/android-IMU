package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class MobileIMUData implements SensorEventListener {
    android.hardware.SensorManager mSensorManager;
    public long startTime = 0;
    Context context;
    private Callback callback;
    public ArrayList<Double> accValues = new ArrayList<Double>();
    ArrayList<String> velocityValues;
    String TAG = "MobileIMU";
    Sensor accelerometer;

    public MobileIMUData(Activity activity) {
        this.callback = callback;
        accValues.add(0.0d);
        accValues.add(0.0d);
        accValues.add(0.0d);
        context = activity.getApplicationContext();
        velocityValues = new ArrayList<>();
        velocityValues.add("a_x,a_y,a_z,TimeMillisecond \n");
        mSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.i(TAG, "Constructur called");
        mSensorManager.registerListener(this, accelerometer, android.hardware.SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        Log.i(TAG, "" + sensorEvent.values[0] + " " + sensorEvent.sensor.getType());
        accValues.set(0, roundToPrecision(sensorEvent.values[0], 4));
        accValues.set(1, roundToPrecision(sensorEvent.values[1], 4));
        accValues.set(2, roundToPrecision(sensorEvent.values[2], 4));
        startTime = System.currentTimeMillis();
        String data = "";
        for (int x = 0; x < sensorEvent.values.length; x++) {
            data += sensorEvent.values[x] + ",";
        }
        data += startTime + "\n";
        velocityValues.add(data);
    }

    public double roundToPrecision(double num, int precision) {
        return (double) Math.round(num * Math.pow(10d, precision)) / Math.pow(10d, precision);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void wirteCSV(ArrayList<String> name, String fileName) {
        try {

            File file = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                file = new File(context.getExternalFilesDir(null).getAbsolutePath() + fileName);
                Log.i(TAG, " path" + file.getAbsolutePath());
            } else {
                file = new File(context.getExternalFilesDir(null).getAbsolutePath() + fileName);
            }
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (int x = 0; x < name.size(); x++) {
                bw.write(name.get(x));
            }
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean close() {
        // wirteCSV(velocityValues, "wakingWithMovile.csv");
        try {
            mSensorManager.unregisterListener(this);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
