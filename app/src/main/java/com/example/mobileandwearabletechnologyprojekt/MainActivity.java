package com.example.mobileandwearabletechnologyprojekt;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private TextView proximityStatus;
    private TextView textView2;

    // Timer variables
    private boolean nearState = false;
    private long nearStartTime = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        proximityStatus = findViewById(R.id.proximityStatus);
        textView2 = findViewById(R.id.textView2);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (proximitySensor == null) {
            proximityStatus.setText("Proximity sensor not available on this device");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float proximityValue = sensorEvent.values[0];

        /*
        if (proximityValue < proximitySensor.getMaximumRange()) {
            // Object is near the proximity sensor
            proximityStatus.setText("Near");
        } else {
            // Object is far from the proximity sensor
            proximityStatus.setText("Far");
        }

         */
        if (proximityValue < proximitySensor.getMaximumRange()) {
            if (!nearState) {
                // Object is near the proximity sensor, start the timer
                nearState = true;
                nearStartTime = System.currentTimeMillis();
            } else {
                // Object is still near, check if it's been near for 2 seconds
                long currentTime = System.currentTimeMillis();
                if (currentTime - nearStartTime >= 2000) {
                    // Object has been near for 2 seconds, change the color of textView2 to green
                    textView2.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                }
            }
        } else {
            // Object is far from the proximity sensor
            nearState = false;
            nearStartTime = 0;
        }
    }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used in this example
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
