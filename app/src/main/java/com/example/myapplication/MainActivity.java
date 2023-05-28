package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    MobileIMUData mIMU;
    TextView tv;
    Timer textUpdateTimer = new Timer();
    int delay = 0;
    int period = 100;
    // call the text update function every 100ms

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.IMUVAL);
        // mIMU = new MobileIMUData(this);
    }

    public void buttonClickFunc(View view) {
        mIMU.close();
        Log.i("DESTROYING", "Window is destroyed");
    }

    public void buttonClickFuncStart(View view) {
        Log.i("CREATING", "Window is created");
        try {
            textUpdateTimer.scheduleAtFixedRate(new TimerTask()
            {
                public void run()
                {
                    updateSensorValText();
                }
            }, delay, period);
            mIMU = new MobileIMUData(this);
        } catch (Exception e) {
            Log.i("ERR_START", String.valueOf(e));
        }
    }

    @SuppressLint("DefaultLocale")
        public void updateSensorValText() {
        try {
            String st = String.format(
                    "\nX: %f\nY: %f\nZ: %f\nts: %d",
                    mIMU.accValues.get(0),
                    mIMU.accValues.get(1),
                    mIMU.accValues.get(2),
                    mIMU.startTime
            );
            Log.i("SENS_VAL", st);
            tv.setText(st);
        } catch (Exception e) {
            Log.i("ERR", String.valueOf(e));
            // set the frequency to 1000ms if we haven't started
        }

    }

//    Callback cb = new Callback() {
//        @Override
//        public int hashCode() {
//            return super.hashCode();
//        }
//    };
}