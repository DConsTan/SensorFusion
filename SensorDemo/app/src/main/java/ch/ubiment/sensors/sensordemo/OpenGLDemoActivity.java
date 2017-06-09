package ch.ubiment.sensors.sensordemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.nfc.Tag;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.List;

import static android.hardware.SensorManager.getQuaternionFromVector;
import static android.hardware.SensorManager.getRotationMatrix;
import static android.hardware.SensorManager.getRotationMatrixFromVector;
import static android.icu.lang.UCharacter.getType;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


//https://github.com/barbeau/gpstest/blob/master/GPSTest/src/main/java/com/android/gpstest/GpsTestActivity.java#L565

public class OpenGLDemoActivity extends Activity implements SensorEventListener {


    private SensorManager sensorManager;
    private SensorManager mSensorManager;
    private float[] GravityVector = new float[3];
    private float[] MagneticVector = new float[3];
    private float[] RotateVector = new float[3];
    private String IP_Dest = "160.98.101.98"; //adresse Ip serveur ordianteur
    private int Port_Dest = 46000;         // Port d'écoute du serveur
    UdpClientSend sender = new UdpClientSend(IP_Dest, Port_Dest);
    private boolean calibrMAG = FALSE;
    private boolean calibrGRAV = FALSE;
    private SntpClient sntpClient;      // parameter for the SNTP protocol
    private boolean isSntpTimeSet = false;  //parameter to determine if the time is already SET
    private String TAG = "OpenGLDemoActivity";
    private long ElapsedTime;

    private Scene scene = new Scene();
    OpenGLRenderer openGLRenderer = new OpenGLRenderer(scene);

    int gravityVectorID;
    int magneticVectorID;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //initialize sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sntpClient = new SntpClient();

        // Go fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        GLSurfaceView view = new GLSurfaceView(this);
        view.setRenderer(openGLRenderer);
        setContentView(view);

        float[] one4f = {1.0f, 1.0f, 1.0f, 1.0f};
        float[] one3f = {1.0f, 1.0f, 1.0f};
        float[] zeros3f = {0.0f, 0.0f, 0.0f};
        gravityVectorID = scene.newVector(zeros3f, one4f);
        magneticVectorID = scene.newVector(zeros3f, one4f);




    }

    @Override
    public void onResume() {
        super.onResume();
        //Sensor vectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //sensorManager.registerListener(this, vectorSensor, 16000); // ~60hz

        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);

    }


    SensorEventListener mSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            float[] I = new float[16];
            float[] Q = new float[4]; //déclaration du quaternion
            float[] rotationMatrix = new float[16];

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                MagneticVector = (float[]) getMagneticVector(event).clone();
                calibrMAG = TRUE;
                //SensorManager.getRotationMatrixFromVector(rotationMatrix, MagneticVector);--> Permet de calculer le vecteur de rotation à l'aide du vecteur champ magnetique

                scene.setVectorXYZ(magneticVectorID, MagneticVector);

                Log.v(TAG, "Magnetic");
            } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {

                GravityVector = (float[]) getGravityVector(event).clone();
                calibrGRAV = TRUE;

                scene.setVectorXYZ(gravityVectorID, GravityVector);

                //SensorManager.getRotationMatrixFromVector(rotationMatrix, GravityVector);--> permet de calculer le vecteur de rotation à l'aide du vecteur de Gravité
                Log.v(TAG, "Gravity");
            } else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
                //RotateVector[0] = event.values[0];
                //RotateVector[1] = event.values[1];
                //RotateVector[2] = event.values[2];
                getRotationMatrixFromVector(rotationMatrix, event.values);
                openGLRenderer.setRotationMatrix_Gyro(rotationMatrix);
                Log.v(TAG, "rotation vector");
            }

            //Cette méthode permet de calculer le vecteur de rotation grâce à la fois au vecteur de champ magnétique et au vecteur gravité

            if (calibrGRAV && calibrMAG) {


                long ElapsedTime = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();


                getRotationMatrix(rotationMatrix, I, GravityVector, MagneticVector);
                calibrMAG = FALSE;
                calibrGRAV = FALSE;

                //convertir la matrice de rotation en quaternion
                getQuaternionFromVector(Q, rotationMatrix);

                String msg =
                        "{\"ElapsedTime\":\""+ ElapsedTime +
                        "\",\"Q0\":\"" + Q[0] +
                        "\",\"Q1\":\"" + Q[1] +
                        "\",\"Q2\":\"" + Q[2] +
                        "\",\"Q3\":\"" + Q[3] + "\"}";


                try {
                    Log.v(TAG, "Break point try");
                    sender.send(msg);
                } catch (Exception e) {
                    Log.v(TAG, "Break point catch");
                    e.printStackTrace();

                }

                openGLRenderer.setRotationMatrix_Magn(rotationMatrix);


                float[] orientation = new float[3];
                SensorManager.getOrientation(rotationMatrix, orientation);

                // Permet de calculer les valeurs des angles d'orientation du vecteurs résultant associée à la matrice de rotation

                double pitch = orientation[0];
                double roll = orientation[1];
                double azimuth = orientation[2];
                //Log.v(TAG, "pitch: " + pitch + ", roll: " + roll + ", azimuth: " + azimuth);
            }
        }
    };
/*
    // this method is use to calculate rotation Matrix from rotate vector
    public void computeRotationMatrix(){
        SensorManager.getRotationMatrixFromVector(rotationMatrixfromrotateVect, RotateVector);
    }
*/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        //sender.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        sender.close();
    }


    //Methode qui rencoie le vecteur champ magnetique//

    public float[] getMagneticVector(SensorEvent e) {
        //get rotation vector from Magnetic Sensor

        float xMagnetic = e.values[0];
        float yMagnetic = e.values[1];
        float zMagnetic = e.values[2];
        float magneticStrenght = (float) Math.sqrt(
                (xMagnetic * xMagnetic + yMagnetic * yMagnetic + zMagnetic * zMagnetic));
        //  float magneticStrenght =1;

        //Normalisation du vecteur champ magnétique

        xMagnetic = xMagnetic / magneticStrenght;
        yMagnetic = yMagnetic / magneticStrenght;
        zMagnetic = zMagnetic / magneticStrenght;

        // vecteur champ magnetique normalisé

        float[] MagneticVector = {xMagnetic, yMagnetic, zMagnetic};

        return MagneticVector;
    }

    //Methode qui renvoie le vecteur Gravite

    public float[] getGravityVector(SensorEvent e) {

        //Get rotation vector from Gravity Sensor
        float xgravity = e.values[0];
        float ygravity = e.values[1];
        float zgravity = e.values[2];
        float gravityStrenght = (float) Math.sqrt(
                (xgravity * xgravity + ygravity * ygravity + zgravity * zgravity));
        //test
        // float gravityStrenght =1;

        //Normalisation du vecteur champ magnétique

        xgravity = xgravity / gravityStrenght;
        ygravity = ygravity / gravityStrenght;
        zgravity = zgravity / gravityStrenght;

        // vecteur champ magnetique normalisé

        float[] GravityVector = {xgravity, ygravity, zgravity};

        return GravityVector;

    }
}


