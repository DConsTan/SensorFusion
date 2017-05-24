package ch.ubiment.sensors.sensordemo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;


//https://github.com/barbeau/gpstest/blob/master/GPSTest/src/main/java/com/android/gpstest/GpsTestActivity.java#L565

public class OpenGLDemoActivity extends Activity implements SensorEventListener{
    SensorManager sensorManager;
    OpenGLRenderer openGLRenderer = new OpenGLRenderer();
    String TAG = "OpenGLDemoActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //initialize sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Go fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        GLSurfaceView view = new GLSurfaceView(this);
        view.setRenderer(openGLRenderer);
        setContentView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        Sensor vectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, vectorSensor, 16000); // ~60hz
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //get rotation matrix from rotation vector
        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        openGLRenderer.setRotationMatrix(rotationMatrix);

        //get orientation angles from rotation matrix and log it
        float[] orientation = new float[3];
        SensorManager.getOrientation(rotationMatrix, orientation);

        double pitch = orientation[0];
        double roll = orientation[1];
        double azimuth = orientation[2];
        Log.v(TAG,"pitch: " + pitch + ", roll: " + roll + ", azimuth: " + azimuth);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}