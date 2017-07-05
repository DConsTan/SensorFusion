package boris.mambap.pedometer_apps.pdr;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by mambap on 03/07/17.
 */

public class DeviceAttitudeHandle implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private float [] orientation = new float[3];
    private  float []mRotationMatrix = new float[16];

    public float[] getOrientation() {

        orientation[0] = (float) Math.toDegrees(orientation[0]);
        orientation[1] = (float) Math.toDegrees(orientation[1]);
        orientation[2] = (float) Math.toDegrees(orientation[2]);
        return orientation;
    }




    public DeviceAttitudeHandle(SensorManager sensorM){
        this.sensorManager = sensorM;
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

    }

    public void start() {
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void stop() {
        sensorManager.unregisterListener(this, rotationSensor);

    }
    public void onSensorChanged(SensorEvent event) {
        float[] rotationMatrix = new float[16];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
        sensorManager.remapCoordinateSystem(rotationMatrix,
                SensorManager.AXIS_X, SensorManager.AXIS_Z,
                mRotationMatrix);
        SensorManager.getOrientation(mRotationMatrix, orientation);

    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
