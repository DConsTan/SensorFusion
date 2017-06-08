package ch.ubiment.sensors.sensordemo;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import static android.hardware.SensorManager.getRotationMatrix;

/**
 * Created by mambap on 30/05/17.
 *
 * Classe de test permettant d'afficher les informations relatives au champ magnétique
 * Cette classe est obselete car elle a été implémenté sous forme de méthode dans la classe OpenGLDActivity
 */

 public class SensorMagnetometerActivity extends Activity implements SensorEventListener {
    SensorManager sensorManager;
    String TAG = "MagnetometerActivity";
    Sensor magnetometer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

    }

    @Override
    protected void onPause() {
        // unregister the sensor (désenregistrer le capteur)
        sensorManager.unregisterListener(this, magnetometer);
        super.onPause();
    }

    @Override
    protected void onResume() {
        magnetometer =sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        super.onResume();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {


    // Cette instruction permet de s'assurer que l'on écoute bien au capteur relatif au champ magnétique

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            // Valeur du vecteur du champ magnétique (x,y,z)

            float xMagnetic = event.values[0];
            float yMagnetic = event.values[1];
            float zMagnetic = event.values[2];
            float[]MagneticVector = {xMagnetic, yMagnetic, zMagnetic};
            float[] MagnMatrix=new float[16];
            float pitch, roll, azimuth;
            //
            // Instensité du vectuer champ magnétique

            float magneticStrenght=(float)Math.sqrt(
                    (xMagnetic*xMagnetic + yMagnetic*yMagnetic + zMagnetic*zMagnetic));

            // contruction de la chaine de caractere contenannt les valeurs du champ magnétique suivant differentes directions
            //Ainsi que de l'intensité
            String StrMAG=" xMagnetic= "+xMagnetic+" ; yMagnetic= "+yMagnetic+" ; zMagnetic= "+zMagnetic+  " ; Instensité= "+magneticStrenght;

                Toast.makeText(this, StrMAG, Toast.LENGTH_SHORT).show();

            SensorManager.getRotationMatrixFromVector(MagnMatrix, MagneticVector);
        }
        }
    }
