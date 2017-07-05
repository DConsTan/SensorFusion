package boris.mambap.pedometer_apps.pdr;
import android.location.Location;
import android.util.Log;
/**
 * Created by mambap on 03/07/17.
 */

public class StepPositionningHandler {

    private Location PositionActuel = new Location("");
    public final int RayonT = 6378100;


    public void setPostionActuel(Location pos) {
        this.PositionActuel.setLatitude(pos.getLatitude());
        this.PositionActuel.setLongitude(pos.getLongitude());
    }

    public Location getPositionActuel(){
        return PositionActuel;
    }

    public Location computeNextStep(float stepSize, float angle){
        //initialisation de la nouvelle position
        Location newLocation = new Location("");

        double pasEnRadian = Math.toRadians(angle);
        double latitude = Math.toRadians(PositionActuel.getLatitude());
        double longitude = Math.toRadians(PositionActuel.getLongitude());

        double nextLatitude = Math.asin( Math.sin(latitude)*Math.cos(stepSize/RayonT) + Math.cos(latitude)*Math.sin(stepSize/RayonT)*Math.cos(pasEnRadian));
        double nextLongitude = longitude + Math.atan2(Math.sin(pasEnRadian)*Math.sin(stepSize/RayonT)*Math.cos(latitude), Math.cos(stepSize/RayonT)-Math.sin(latitude)*Math.sin(nextLatitude));

        newLocation.setLatitude(Math.toDegrees(nextLatitude));
        newLocation.setLongitude(Math.toDegrees(nextLongitude));

        return newLocation;

    }
}
