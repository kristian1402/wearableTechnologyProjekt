package com.example.mobileandwearabletechnologyprojekt;

import android.Manifest;
import androidx.core.app.ActivityCompat;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;

import java.util.Collections;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private static final int CAMERA_PERMISSION_REQUEST = 1;
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private TextView proximityStatus;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private TextureView textureView;
    private AudioManager audioManager;

    // Timer variables
    private boolean nearState = false;
    private long nearStartTime = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        proximityStatus = findViewById(R.id.proximityStatus);
        textureView = findViewById(R.id.textureView);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (proximitySensor == null) {
            proximityStatus.setText("Proximity sensor not available on this device");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float proximityValue = sensorEvent.values[0];
        if (proximityValue < proximitySensor.getMaximumRange()) {
            proximityStatus.setText("Near");
            if (!nearState) {
                // Object is near the proximity sensor, start the timer
                nearState = true;
                nearStartTime = System.currentTimeMillis();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - nearStartTime >= 1000) {
                            openCameraForGestureRecognition();
                            togglePlayPause();
                        }
                    }
                }, 2000); // 2000 milliseconds = 2 seconds
            }
        } else {
            // Object is far from the proximity sensor
            proximityStatus.setText("Far");
            nearState = false;
            nearStartTime = 0;
            // Remove any pending callbacks to ensure the timer is not triggered when the object is far
            handler.removeCallbacksAndMessages(null);
        }
    }

    private void openCameraForGestureRecognition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted; request it from the user.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }
        try {
            String cameraId = cameraManager.getCameraIdList()[1]; // You may need to choose a specific camera here

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;

                    try {
                        Surface surface = new Surface(textureView.getSurfaceTexture());
                        CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        captureRequestBuilder.addTarget(surface);

                        cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                cameraCaptureSession = session;

                                try {
                                    cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                // Handle configuration failure
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    cameraDevice.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    // Handle camera device error
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void togglePlayPause() {
        if (audioManager.isMusicActive()) {
            // Music is playing, pause it
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE));
        } else {
            // Music is not playing, play it
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY));
            audioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PLAY));
        }
    }

    private void increaseMediaVolume() {
        // Get the current media volume
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        // Get the maximum media volume
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        // Check if the current volume is less than the maximum volume
        if (currentVolume < maxVolume) {
            // Increase the volume by a specific amount (you can adjust this value)
            int newVolume = Math.min(currentVolume + 1, maxVolume);

            // Set the new volume
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        }
    }

    private void decreaseMediaVolume() {
        // Get the current media volume
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        // Check if the current volume is greater than the minimum volume
        if (currentVolume > 0) {
            // Decrease the volume by a specific amount (you can adjust this value)
            int newVolume = Math.max(currentVolume - 1, 0);

            // Set the new volume
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
        }
    }

    private void increaseMediaVolumeWithDelay() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                increaseMediaVolume();
            }
        }, 1000); // 1000 milliseconds (1 second) delay
    }

    private void decreaseMediaVolumeWithDelay() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                decreaseMediaVolume();
            }
        }, 1000); // 1000 milliseconds (1 second) delay
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

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
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
}
