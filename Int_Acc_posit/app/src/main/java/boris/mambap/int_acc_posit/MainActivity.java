package boris.mambap.int_acc_posit;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class MainActivity extends Activity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accSensor;
    private Sensor rotationSensor;
    private TextView Text_px;
    private TextView Text_py;
    private TextView Text_pz;
    static final float NS2S = 1.0f / 1000000000.0f;
    float last_timestamp = 0;
    private float[] acceleration = new float[3];
    private float[] velocity = new float[3];
    private float[] position = new float[3];
    private float[] last_values = new float[3];
    private float[] rotation_vector_values = null;
    private float[] earthAcc;
    private float[] Rm;
    private float[] I;
    private String IP_Dest = "160.98.114.16"; //adresse Ip serveur ordinteur
    private int Port_Dest = 46000;         // Port d'écoute du serveur
    private SntpClient sntpClient;
    private boolean isSntpTimeSet = false;
    UdpClientSend sender;
    static final float offset_x = 0.0f;//0.0276256066565f;
    static final float offset_y = 0.0f;//0.0837804637445f ;
    static final float offset_z = 0.0f; //-0.0858009118772f;
    private float offset = 0;
    private boolean firstEntry =FALSE;
    private boolean ifStop = FALSE;
    private boolean ifStart = FALSE;
    private float timeTamp = 0;
    static final float ALPHA = 0.8f; //the smoothing factor
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Text_px = (TextView) findViewById(R.id.px);
        Text_py = (TextView) findViewById(R.id.py);
        Text_pz = (TextView) findViewById(R.id.pz);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sntpClient = new SntpClient();
        velocity[0] = 0;
        velocity[1] = 0;
        velocity[2] = 0;
        position[0] = 0;
        position[1] = 0;
        position[2] = 0;
        last_values[0] = 0;
        last_values[1] = 0;
        last_values[2] = 0;



    }

    public void  tostart(View view){


        ifStart = TRUE;
        firstEntry = FALSE;

        if(ifStop){
            this.onResume();
            ifStop = FALSE;
            last_timestamp = timeTamp;

        }
        if(ifStart) {
            String initial = String.valueOf(0);
            velocity[0] = 0;
            velocity[1] = 0;
            velocity[2] = 0;
            position[0] = 0;
            position[1] = 0;
            position[2] = 0;
            last_values[0] = 0;
            last_values[1] = 0;
            last_values[2] = 0;
            Text_px.setText(initial);
            Text_py.setText(initial);
            Text_pz.setText(initial);


        }

    }
    public void tostop(View view){
        this.onPause();
        ifStop = TRUE;
        Text_px.setText(String.valueOf(position[0]));
        Text_py.setText(String.valueOf(position[1]));
        Text_pz.setText(String.valueOf(position[2]));
    }


    public void onSensorChanged(SensorEvent event) {

        if((firstEntry && ifStart)) {


            firstEntry = TRUE;
            if ((rotation_vector_values != null) && (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION)){

                Log.v("test1","ppppppppppppppppppppppppppppppppppppppppppppppppppppppp");
                float[] deviceRelativeAcceleration = new float[4];
                deviceRelativeAcceleration[0] = event.values[0];
                deviceRelativeAcceleration[1] = event.values[1];
                deviceRelativeAcceleration[2] = event.values[2];
                deviceRelativeAcceleration[3] = 0;
                float[] tampEartAcc = new float[4];
                float[] tampEartAccLPF = new float[4];
                 Rm = new float[16]; I = new float[16]; earthAcc = new float[16];
                float[] inv = new float[16];
                //SensorManager.getRotationMatrix(Rm, I, gravityValues, magneticValues);
                SensorManager.getRotationMatrixFromVector(Rm, rotation_vector_values);
                android.opengl.Matrix.transposeM(inv, 0, Rm, 0);
                android.opengl.Matrix.multiplyMV(earthAcc, 0, inv, 0, deviceRelativeAcceleration, 0);
                Log.d("Acceleration", "Values: (" + earthAcc[0] + ", " + earthAcc[1] + ", " + earthAcc[2] + ")");
                tampEartAcc[0] = earthAcc[0];
                tampEartAcc[1] = earthAcc[1];
                tampEartAcc[2] = earthAcc[2];

                float dt = (event.timestamp - last_timestamp) * NS2S;
                timeTamp = event.timestamp;
                if (!isSntpTimeSet) {

                    new Thread(new Runnable() {
                        public void run() {
                            if (sntpClient.requestTime("time2.ethz.ch", 60000)) {
                                isSntpTimeSet = true;
                            } else {
                                //Log.v("echec sntp");
                                return;
                            }
                        }
                    }).start();

                }

                tampEartAccLPF = lowPass( tampEartAcc, tampEartAccLPF);

                for (int index = 0; index < 3; ++index) {


                    acceleration[index] = tampEartAccLPF[index] ;



                    float last_velocity = velocity[index];
                    velocity[index] += ((acceleration[index] + last_values[index]) / 2) * dt;
                    position[index] += ((velocity[index] + last_velocity) / 2) * dt;

                    last_values[index] = acceleration[index];
                }

                last_timestamp = event.timestamp;

                long ElapsedTime = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();

                Log.v("acceleration", "ax = " + acceleration[0] + " ay = " + acceleration[1] + " az = " + acceleration[2]);
                Log.v("Vélocity", "vx = " + velocity[0] + " vy = " + velocity[1] + " vz = " + velocity[2]);
                Log.v("position", "px = " + position[0] + " py = " + position[1] + " pz = " + position[2]);

                float dx = 0;
                float dy = 0;
                float dz = 0;
                String msg = "{\"ElapsedTime\":\"" + ElapsedTime +
                        "\",\"px\":\"" + position[0] +
                        "\",\"py\":\"" + position[1] +
                        "\",\"pz\":\"" + position[2] + "\"}";

                Text_px.setText(String.valueOf(position[0]));
                Text_py.setText(String.valueOf(position[1]));
                Text_pz.setText(String.valueOf(position[2]));

                try {
                    sender.send(msg);
                } catch (Exception e) {

                    e.printStackTrace();
                }



            } else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                Log.v("test1","rtrtrtrtrtrtrtrtrtrtrtrtrtrtrtrttrtrtrtrtrtrtrtrtrtrtrtrtrtrtrtrtrtrtr");
                rotation_vector_values = (float[])event.values.clone();

            }



        }
        else{
            last_timestamp  = event.timestamp;
            firstEntry = TRUE;

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        sender.close();
    }

    protected void onResume() {
        super.onResume();
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sender = new UdpClientSend(IP_Dest, Port_Dest);
    }

    public void onPause() {

        super.onPause();

        sensorManager.unregisterListener(this);
        sender.close();

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected float[] lowPass( float[] input, float[] output ){
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}