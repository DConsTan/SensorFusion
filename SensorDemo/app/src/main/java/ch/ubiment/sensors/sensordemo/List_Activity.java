package ch.ubiment.sensors.sensordemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;


/**
 * Created by mambap on 30/05/17.
 * objectifs;  permet de lister l'ensemble des capteurs disponible sur un mobile
 *
 */

public class List_Activity extends Activity {
    SensorManager sensorManager;
    String TAG = "List_Activity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        listSensor();
    }

    private void listSensor() {
        // Trouver tous les capteurs de l'appareil :
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        // La chaîne descriptive de chaque capteur
        StringBuffer sensorDesc = new StringBuffer();
        // pour chaque capteur trouvé, construire sa chaîne descriptive

        for (Sensor sensor : sensors) {
            sensorDesc.append("New sensor detected : \r\n");
            sensorDesc.append("\tName: " + sensor.getName() + "\r\n");
            sensorDesc.append("\tType: " + getType(sensor.getType()) + "\r\n");
            sensorDesc.append("Version: " + sensor.getVersion() + "\r\n");
            sensorDesc.append("Resolution (in the sensor unit): " + sensor.getResolution() + "\r\n");
            sensorDesc.append("Power in mA used by this sensor while in use" + sensor.getPower() + "\r\n");
            sensorDesc.append("Vendor: " + sensor.getVendor() + "\r\n");
            sensorDesc.append("Maximum range of the sensor in the sensor's unit." + sensor.getMaximumRange() + "\r\n");
            sensorDesc.append("Minimum delay allowed between two events in microsecond" + " or zero if this sensor only returns a value when the data it's measuring changes" + sensor.getMinDelay() + "\r\n");
        }

        // Faire quelque chose de cette liste: permet de maintenir les informations affichées à l'ecran
        //On peu enlever la boucle for dans ce cas les texte s'effacera après une durée de 5sec
        for(int i=0;i<1000;i++) {
            Toast.makeText(this, sensorDesc.toString(), Toast.LENGTH_LONG).show();
        }

    }

    private String getType(int type) {
        String strType;
        switch (type) {
            case Sensor.TYPE_ACCELEROMETER:
                strType = "TYPE_ACCELEROMETER";
                break;
            case Sensor.TYPE_GRAVITY:
                strType = "TYPE_GRAVITY";
                break;
            case Sensor.TYPE_GYROSCOPE:
                strType = "TYPE_GYROSCOPE";
                break;
            case Sensor.TYPE_LIGHT:
                strType = "TYPE_LIGHT";
                break;
            case Sensor.TYPE_LINEAR_ACCELERATION:
                strType = "TYPE_LINEAR_ACCELERATION";
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                strType = "TYPE_MAGNETIC_FIELD";
                break;
            case Sensor.TYPE_ORIENTATION:
                strType = "TYPE_ORIENTATION";
                break;
            case Sensor.TYPE_PRESSURE:
                strType = "TYPE_PRESSURE";
                break;
            case Sensor.TYPE_PROXIMITY:
                strType = "TYPE_PROXIMITY";
                break;
            case Sensor.TYPE_ROTATION_VECTOR:
                strType = "TYPE_ROTATION_VECTOR";
                break;
            case Sensor.TYPE_TEMPERATURE:
                strType = "TYPE_TEMPERATURE";
                break;
            default:
                strType = "TYPE_UNKNOW";
                break;
        }
        // ce instruction permet d'afficher des logs à sur la console du PC
        Log.v(TAG, "information=" + strType);
        return strType;

    }

}
