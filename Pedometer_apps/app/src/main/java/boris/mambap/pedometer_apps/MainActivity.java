package boris.mambap.pedometer_apps;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import boris.mambap.pedometer_apps.pdr.DeviceAttitudeHandle;

import static android.R.attr.rotation;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends Activity {


    private SensorManager mSensorManager;
    //private Sensor stepCountSensor;
    private Sensor stepDetectSensor;
    private TextView mStep;
    private TextView OrientationRT_x;
    private TextView OrientationRT_y;
    private TextView OrientationRT_z;
    private StepDetectionCountHandler Step;
    private DeviceAttitudeHandle Rotation;
    private boolean isStep = FALSE;
    private int stepCount = 0;
    //private SensorEventListener mSensorListener;
    //private TextView text = new TextView(this);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStep = (TextView) findViewById(R.id.stepssincereboot);
        OrientationRT_x = (TextView) findViewById(R.id.PitchView);
        OrientationRT_y = (TextView) findViewById(R.id.RollView);
        OrientationRT_z = (TextView) findViewById(R.id.AzimuthView);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Rotation = new DeviceAttitudeHandle(mSensorManager);
        Step = new StepDetectionCountHandler(mSensorManager);

        stepCount = Step.getStepCount();
        mStep.setText(String.valueOf(Step.getStepCount()));
    }

    public void toReset(View view) {
        //stepCount = 0;
        Step.setStepCount(0);
        mStep.setText(String.valueOf(stepCount));
    }

    public void toStart(View view) {
        //stepCount = 0;
        Step.setStepCount(0);
        mStep.setText(String.valueOf(stepCount));
    }

    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
        Step.stop();
        Rotation.stop();
    }

    protected void onResume() {
        super.onResume();
        Step.start();
        Rotation.start();
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
        //stepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        //stepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        //mSensorManager.registerListener(this,stepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL );
        //mSensorManager.registerListener(this,stepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL );

        //Step.onSensorChanged(event);
        //mStep.setText(String.valueOf(stepCount));
        //stepCount++;

    }

    SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            mStep.setText(String.valueOf(Step.getStepCount()));
            float []temp = (Rotation.getOrientation()).clone();
            OrientationRT_z.setText(String.valueOf(temp[0]));
            OrientationRT_x.setText(String.valueOf(temp[1]));
            OrientationRT_y.setText(String.valueOf(temp[2]));
        }

    };
}
