package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import javax.security.auth.callback.Callback;

public class MainActivity extends AppCompatActivity {

    MobileIMUData mIMU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIMU = new MobileIMUData(this);
    }

    public void buttonClickFunc(View view) {
        mIMU.close();
        Log.i("DESTROYING", "Window is destroyed");
    }

//    Callback cb = new Callback() {
//        @Override
//        public int hashCode() {
//            return super.hashCode();
//        }
//    };
}