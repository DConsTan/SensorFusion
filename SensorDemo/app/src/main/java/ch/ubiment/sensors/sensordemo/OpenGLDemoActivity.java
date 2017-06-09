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
import static android.icu.lang.UCharacter.getType;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;


//https://github.com/barbeau/gpstest/blob/master/GPSTest/src/main/java/com/android/gpstest/GpsTestActivity.java#L565

public class OpenGLDemoActivity extends Activity implements SensorEventListener {


    private SensorManager sensorManager;
    private SensorManager mSensorManager;

    //definition of all vectors
    private float[] gravityVector = new float[3];
    private float[] magneticVector = new float[3];
    private float[] rotateVector = new float[3];
    private float[] accelVector = new float[3];

    //Definition of calibrate boolean
    private boolean calibrMAG = FALSE;
    private boolean calibrGRAV = FALSE;
    private boolean calibrACC = FALSE;
    private boolean calibrROTV = FALSE;

    //Definition of rotation Matrix
    private float[] gravMagnMatrix = new float[16];   // compute with  Sensor magnetic and gravity field
    private float [] rotateMatrixFromRotateVector = new float[16];    //compute with only sensor Rotation Vector
    private float [] accelMagMatrix = new float[16];

    //Definiton of Orientation Vectors
    private float [] rotateOrientation = new float[3]; //compute with Sensor Rotation Vector
    private float [] gravityOrientation = new float[3];
    private float [] magneticOrientation = new float[3];
    private float [] accelOrientation = new float[3];
    private float [] accMagOrientation = new float[3];
    private float [] gravMagnOrientation = new float[3];




    private String IP_Dest = "160.98.114.71"; //adresse Ip serveur ordianteur
    private int Port_Dest = 46000;         // Port d'écoute du serveur
    UdpClientSend sender = new UdpClientSend(IP_Dest, Port_Dest);

    private SntpClient sntpClient;      // parameter for the SNTP protocol
    private boolean isSntpTimeSet = false;  //parameter to determine if the time is already SET
    private String TAG = "OpenGLDemoActivity";
    private float[] Q = new float[4]; //déclaration du quaternion


    OpenGLRenderer openGLRenderer = new OpenGLRenderer();


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
        initMatrix();
        initVector();

    }

    @Override
    public void onResume() {
        super.onResume();
        //Sensor vectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //sensorManager.registerListener(this, vectorSensor, 16000); // ~60hz

        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);


    }


    SensorEventListener mSensorListener = new SensorEventListener() {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {


            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

                magneticVector = (float[]) getMagneticVector(event).clone();
                calibrMAG = TRUE;
                calculategravMagnOrientationAndMatrix();
                calculateAccMagOrientationAndMatrix();
                //getRotationMatrix(accelMagMatrix, null, accelVector, magneticVector);
                Log.v(TAG, "Magnetic");
            } else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {

                gravityVector = (float[]) getGravityVector(event).clone();
                calibrGRAV = TRUE;
                //SensorManager.getRotationMatrixFromVector(gravMagnMatrix, GravityVector);--> permet de calculer le vecteur de rotation à l'aide du vecteur de Gravité
                Log.v(TAG, "Gravity");
            }
            else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){

                rotateVector = (float []) getRotateVector(event).clone();
                calibrROTV = TRUE;
                calculateRotateOrientationAndMatrix();
                //SensorManager.getRotationMatrixFromVector(rotateMatrixFromRotateVector, rotateVector);
                Log.v(TAG, "rotation vector");
            }
            else if(event.sensor.getType() ==  Sensor.TYPE_ACCELEROMETER){

                accelVector = (float [])getAccLinearVec(event).clone();
                calibrACC = TRUE;
                Log.v(TAG, "linear acceleration vector");
            }

            //Cette méthode permet de calculer le vecteur de rotation grâce à la fois au vecteur de champ magnétique et au vecteur gravité
            if ((calibrGRAV && calibrMAG) || (calibrACC && calibrMAG)) {
                if(!isSntpTimeSet) {

                    new Thread(new Runnable() {
                        public void run() {
                            if (sntpClient.requestTime("time2.ethz.ch",60000)) {
                                isSntpTimeSet = true;
                            } else {
                                Log.i(TAG, "sntpTimeNotSetYet");
                                return;
                            }
                        }
                    }).start();

                }

                long ElapsedTime = sntpClient.getNtpTime() + SystemClock.elapsedRealtime() - sntpClient.getNtpTimeReference();
                //getRotationMatrix(gravMagnMatrix, null, gravityVector, magneticVector);
                calibrMAG = FALSE;
                calibrGRAV = FALSE;
                calibrACC = FALSE;

                //convertir la matrice de rotation en quaternion
                getQuaternionFromVector(Q, gravMagnMatrix);
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

                openGLRenderer.setRotationMatrix(rotateMatrixFromRotateVector);
                float[] orientation = new float[3];
                SensorManager.getOrientation(gravMagnMatrix, orientation);

                // Permet de calculer les valeurs des angles d'orientation du vecteurs résultant associée à la matrice de rotation
                double pitch = orientation[0];
                double roll = orientation[1];
                double azimuth = orientation[2];
                //Log.v(TAG, "pitch: " + pitch + ", roll: " + roll + ", azimuth: " + azimuth);
            }
        }
    };

    //Compute acceleration and Magnetic field Orientation and rotation matrix
    public void calculateAccMagOrientationAndMatrix() {
        if(SensorManager.getRotationMatrix(accelMagMatrix, null, accelVector, magneticVector)) {
            SensorManager.getOrientation(accelMagMatrix, accMagOrientation);
        }
    }

    //Compute Rotation orientation and rotation Matrix from rotate Vector
    public void calculateRotateOrientationAndMatrix(){
        SensorManager.getRotationMatrixFromVector(rotateMatrixFromRotateVector, rotateVector);
        SensorManager.getOrientation(rotateMatrixFromRotateVector, rotateOrientation);
    }

    //Compute Gravity and Magnetic orientation and Matrix
    public void calculategravMagnOrientationAndMatrix(){
        SensorManager.getRotationMatrix(gravMagnMatrix, null, gravityVector, magneticVector);
        SensorManager.getOrientation(gravMagnMatrix, gravityOrientation);
    }



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

    //Methode qui renvoie le vecteur champ de rotation NON normalisé
    public float[] getRotateVector(SensorEvent e) {
        //get rotation vector from Magnetic Sensor

        //Get rotation vector from Gravity Sensor
        float Vx = e.values[0];
        float Vy = e.values[1];
        float Vz = e.values[2];
        float VStrenght = (float) Math.sqrt(
                (Vx * Vx + Vy * Vy + Vz * Vz));

        /*Normalisation du vecteur champ magnétique
        rotateVector[0] = Vx / VStrenght;
        rotateVector[1] = Vy / VStrenght;
        rotateVector[2] = Vz / VStrenght;*/
        rotateVector[0] = Vx ;
        rotateVector[1] = Vy ;
        rotateVector[2] = Vz ;
        return rotateVector;
    }

    //Methode qui renvoie le vecteur champ Acceleration Lineaire Normalisé
    public float [] getAccLinearVec(SensorEvent e) {

        //Get rotation vector from Gravity Sensor
        float Ax = e.values[0];
        float Ay = e.values[1];
        float Az = e.values[2];
        float AStrenght = (float) Math.sqrt(
                (Ax * Ax + Ay * Ay + Az * Az));

        //Normalisation du vecteur Accélération lineaire Normalisé
        accelVector[0] = Ax / AStrenght;
        accelVector[1] = Ay / AStrenght;
        accelVector[2] = Az / AStrenght;
        return accelVector;
    }

    //Methode qui renvoie le vecteur champ magnetique Normalisé
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

        float[] magneticVector = {xMagnetic, yMagnetic, zMagnetic};

        return magneticVector;
    }

    //Methode qui renvoie le vecteur Gravite Normalisé

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

        float[] gravityVector = {xgravity, ygravity, zgravity};

        return gravityVector;

    }

    //this method is use to initialse matrix
    public void initMatrix(){
        for (int i = 0 ; i<16; i++){
            gravMagnMatrix[i] = 0.0f;
            rotateMatrixFromRotateVector[i] = 0.0f;
            accelMagMatrix[i] = 0.0f;
        }
    }

    public void initVector(){
        for (int i = 0; i<3; i++){
            gravityVector[i] =0;
            magneticVector[i]  = 0;
            rotateVector[i] = 0;
            accelVector[i] = 0;
        }
    }

}


