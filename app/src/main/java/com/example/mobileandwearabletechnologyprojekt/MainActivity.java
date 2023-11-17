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
<<<<<<< Updated upstream
import android.media.AudioManager;
=======
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
>>>>>>> Stashed changes

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
import android.widget.TextView;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


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

    AssetManager assetManager = this.getAssets();


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

    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelFileName) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelFileName);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Example of how to use the Interpreter with your model file
    private void runInference() {
        try {
            // Replace "your_model.tflite" with the actual name of your model file
            String modelFileName = "model.tflite";

            // Get the AssetManager from your application context
            AssetManager assetManager = getApplicationContext().getAssets();

            // Load the TensorFlow Lite model
            Interpreter interpreter = new Interpreter(loadModelFile(assetManager, modelFileName));

            // Now you can use the 'interpreter' to run inference with your model
            // ...

        } catch (IOException e) {
            // Handle exceptions, such as file not found or invalid model format
            e.printStackTrace();
        }
    }
}
