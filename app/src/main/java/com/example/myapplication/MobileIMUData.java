package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class MobileIMUData implements SensorEventListener {
    android.hardware.SensorManager mSensorManager;
    double curTime = System.currentTimeMillis();
    double oldTime = curTime;
    double startTime = curTime;

    double timeAlive = curTime - startTime;

    public double dT = curTime - oldTime;
    Context context;
    private Callback callback;
    ArrayList<Double> accUnitVec = new ArrayList<>();
    ArrayList<Double> accX = new ArrayList<>();
    ArrayList<Double> accY = new ArrayList<>();
    ArrayList<Double> accZ = new ArrayList<>();
    public ArrayList<Double> accValues = new ArrayList<>();
    public ArrayList<Double> velValues = new ArrayList<>();
    public ArrayList<Double> posValues = new ArrayList<>();
    public ArrayList<Double> jerkValues = new ArrayList<>();

    public double smoothMagAcc = 0.0d;
    double minFilterValue = 9999;
    int stationaryDuration = 0;
    int calSteps = 0;

    double meanX, meanY, meanZ;

    ArrayList<Double> magAcc = new ArrayList<>();

    String TAG = "MobileIMU";
    Sensor accelerometer;

    public MobileIMUData(Activity activity) {
        this.callback = callback;

        jerkValues.add(0.0d);
        jerkValues.add(0.0d);
        jerkValues.add(0.0d);

        accValues.add(0.0d);
        accValues.add(0.0d);
        accValues.add(0.0d);

        velValues.add(0.0d);
        velValues.add(0.0d);
        velValues.add(0.0d);

        posValues.add(0.0d);
        posValues.add(0.0d);
        posValues.add(0.0d);

        magAcc.add(0.0d);
        magAcc.add(0.0d);
        magAcc.add(0.0d);

        accX.add(0.0d);
        accX.add(0.0d);
        accX.add(0.0d);

        accY.add(0.0d);
        accY.add(0.0d);
        accY.add(0.0d);

        accZ.add(0.0d);
        accZ.add(0.0d);
        accZ.add(0.0d);

        context = activity.getApplicationContext();
        mSensorManager = (android.hardware.SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Log.i(TAG, "Constructur called");
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        Log.i(TAG, "" + sensorEvent.values[0] + " " + sensorEvent.sensor.getType());
        curTime = System.currentTimeMillis();
        timeAlive = curTime - startTime;

//        if (timeAlive % 10 == 0) {
//            posValues.set(0, 0.0d);
//            posValues.set(1, 0.0d);
//            posValues.set(2, 0.0d);
//        }

        while (calSteps < 20) {
            Log.i("CAL", "");
            accX.add((double) sensorEvent.values[0]);
            accY.add((double) sensorEvent.values[1]);
            accZ.add((double) sensorEvent.values[2]);

            meanX = rollingAverage(accX, 20);
            meanY = rollingAverage(accY, 20);
            meanZ = rollingAverage(accZ, 20);
            calSteps++;
        }

        accX.add(roundToPrecision(sensorEvent.values[0], 5) - meanX);
        accY.add(roundToPrecision(sensorEvent.values[1], 5) - meanY);
        accZ.add(roundToPrecision(sensorEvent.values[2], 5) - meanZ);

        accValues.set(0, rollingAverage(accX, 15));
        accValues.set(1, rollingAverage(accY, 15));
        accValues.set(2, rollingAverage(accZ, 15));

//        accUnitVec = getUnitVector(accValues);

        magAcc.add(magnitudeOf(accValues));
        double term1 = magAcc.get(magAcc.size() - 1);
        double term2 = magAcc.get(magAcc.size() - 2);
        double term3 = magAcc.get(magAcc.size() - 3);

        smoothMagAcc = rollingAverage(magAcc, 10);
//        minFilterValue = Math.min((term1 + term2 + term3) / 3, minFilterValue);
        minFilterValue = 0.02;
//        Log.i("MAG", "" + (minFilterValue < term1 - minFilterValue));

        dT = (curTime - oldTime) / 1000.0;

        if (term1 < minFilterValue) {
            stationaryDuration++;
            if (stationaryDuration >= 4) {
                velValues.set(0, 0.0d);
                velValues.set(1, 0.0d);
                velValues.set(2, 0.0d);
                stationaryDuration = 0;
            }
        } else {
            stationaryDuration = 0;
        }

        velValues = integrate(velValues, accValues, dT);
        posValues = integrate(posValues, velValues, dT);

        oldTime = curTime;
//        String data = "";
//        for (int x = 0; x < sensorEvent.values.length; x++) {
//            data += sensorEvent.values[x] + ",";
//        }
//        data += startTime + "\n";
//        velocityValues.add(data);
    }

    public double rollingAverage(ArrayList<Double> magAcc, int window) {
        double sum = 0.0d;
        window = Math.min(window, magAcc.size() - 1);
        for (int i = 1; i < window; i++) {
            sum += magAcc.get(magAcc.size() - i);
        }
        return sum / window;
    }
    public double magnitudeOf(ArrayList<Double> pVec) {
        return Math.sqrt(Math.pow(pVec.get(0), 2) + Math.pow(pVec.get(1), 2) + Math.pow(pVec.get(2), 2));
    }

    public ArrayList<Double> getUnitVector(ArrayList<Double> pVec) {
        double magVec = magnitudeOf(pVec);
        pVec.set(0, pVec.get(0) / magVec);
        pVec.set(1, pVec.get(1) / magVec);
        pVec.set(2, pVec.get(2) / magVec);
        return pVec;
    }

    public ArrayList<Double> integrate(ArrayList<Double> pVal, ArrayList<Double> iVal, double dT) {
        double dampFac = 1;
        pVal.set(0, (pVal.get(0) + (iVal.get(0) * dT)) * dampFac);
        pVal.set(1, (pVal.get(1) + (iVal.get(1) * dT)) * dampFac);
        pVal.set(2, (pVal.get(2) + (iVal.get(2) * dT)) * dampFac);
        return pVal;
    }

    public ArrayList<Double> differentiate(ArrayList<Double> oldV, ArrayList<Double> newV, double dT) {
        ArrayList<Double> t = new ArrayList<>();
        t.add((newV.get(0) - oldV.get(0)) / 2);
        t.add((newV.get(1) - oldV.get(1)) / 2);
        t.add((newV.get(2) - oldV.get(2)) / 2);
        return t;
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
