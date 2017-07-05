
package boris.mambap.pedometer_apps;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.util.Config;
import android.util.Log;
import android.widget.TextView;

import java.util.Vector;

import boris.mambap.pedometer_apps.R;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

/**
 * Created by mambap on 30/06/17.
 */


public class StepDetectionCountHandler implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor stepDetectSensor;

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount;
    }

    private int stepCount = 0;


    public int getStepCount() {
        return stepCount;
    }


    public StepDetectionCountHandler(SensorManager sensorM) {

        this.mSensorManager = sensorM;
        stepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
    }




    public void start() {
        mSensorManager.registerListener(this, stepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    public void stop() {
        mSensorManager.unregisterListener(this, stepDetectSensor);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        stepCount++;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}
